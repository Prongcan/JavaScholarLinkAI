from __future__ import annotations

import os
import math
from typing import Any, Dict, List, Optional

# Load .env if present
try:
    from dotenv import load_dotenv  # type: ignore
    for _p in [
        os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..")),  # project root
        os.path.abspath(os.path.join(os.path.dirname(__file__), "..")),          # backend
        os.path.abspath(os.path.join(os.path.dirname(__file__), ".")),           # service
    ]:
        env_path = os.path.join(_p, ".env")
        if os.path.isfile(env_path):
            load_dotenv(env_path)
            break
    else:
        load_dotenv()
except Exception:
    pass

# Optional YAML support
try:
    import yaml  # type: ignore
except Exception:
    yaml = None

try:
    from openai import OpenAI  # >=1.0.0 new SDK
except Exception as e:
    raise RuntimeError("未安装 openai SDK，请先执行: pip install 'openai>=1.0.0'\n原始错误: %s" % e)

# httpx for proxy-capable client


def _project_root_candidates() -> List[str]:
    base = os.path.dirname(__file__)
    return [
        os.path.abspath(os.path.join(base, "..", "..")),
        os.path.abspath(os.path.join(base, "..")),
        os.path.abspath(os.path.join(base, ".")),
    ]


def _load_yaml_config() -> Optional[Dict[str, Any]]:
    for root in _project_root_candidates():
        cfg = os.path.join(root, "config.yaml")
        if os.path.isfile(cfg):
            if yaml is None:
                raise RuntimeError("检测到 config.yaml，但未安装 PyYAML。请先执行: pip install PyYAML")
            with open(cfg, "r", encoding="utf-8") as f:
                return yaml.safe_load(f) or {}
    return None


def _resolve_openai_conf(
    override: Optional[Dict[str, Any]] = None,
) -> Dict[str, Any]:
    # Defaults
    conf: Dict[str, Any] = {
        "api_key": os.getenv("OPENAI_API_KEY", ""),
        "base_url": os.getenv("OPENAI_API_BASE", ""),
        "model": os.getenv("OPENAI_EMBEDDING_MODEL", "text-embedding-3-small"),
        "timeout": 30,
    }

    # YAML overrides if available
    try:
        data = _load_yaml_config()
        if data and isinstance(data, dict):
            # Preferred: openai section
            oa = data.get("openai") or {}
            if isinstance(oa, dict):
                if oa.get("api_key"):
                    conf["api_key"] = oa.get("api_key")
                if oa.get("base_url"):
                    conf["base_url"] = oa.get("base_url")
                if oa.get("embedding_model"):
                    conf["model"] = oa.get("embedding_model")
                if oa.get("timeout"):
                    conf["timeout"] = int(oa.get("timeout"))
            # Also support: secret_key.openai_api
            sk = data.get("secret_key") or {}
            if isinstance(sk, dict):
                api_from_secret = sk.get("openai_api") or sk.get("openai_api_key")
                if api_from_secret:
                    conf["api_key"] = api_from_secret
    except Exception as e:
        print(f"[WARN] 读取 config.yaml 失败，将使用环境变量: {e}")

    # Runtime override
    if override:
        conf.update({k: v for k, v in override.items() if v not in (None, "")})

    if not conf["api_key"]:
        raise RuntimeError(
            "未找到 OPENAI_API_KEY。请在环境变量/.env 或 config.yaml 的 openai.api_key 中配置。"
        )

    return conf


def _resolve_proxy_url() -> Optional[str]:
    """Resolve proxy URL from config.yaml (preferred) or environment.

    Supported YAML structure:
    proxy:
      enable: true
      host: 127.0.0.1
      port: 7890
      scheme: http   # optional, default http
      url: http://127.0.0.1:7890  # optional, overrides host/port/scheme
    """
    # 1) YAML
    try:
        data = _load_yaml_config()
        if isinstance(data, dict):
            pxy = data.get("proxy") or {}
            if isinstance(pxy, dict):
                enable = pxy.get("enable")
                # If enable explicitly False -> no proxy
                if enable is False:
                    return None
                # url has highest priority inside proxy section
                if pxy.get("url"):
                    return str(pxy["url"]).strip()
                host = str(pxy.get("host", "")).strip()
                port = pxy.get("port")
                scheme = str(pxy.get("scheme", "http")).strip() or "http"
                if host and port:
                    return f"{scheme}://{host}:{int(port)}"
    except Exception as e:
        print(f"[WARN] 读取代理配置失败: {e}")

    # 2) Environment fallbacks
    for key in ("HTTPS_PROXY", "HTTP_PROXY", "ALL_PROXY"):
        val = os.getenv(key)
        if val:
            return val

    return None


def _l2_normalize(vec: List[float]) -> List[float]:
    s = sum(v * v for v in vec)
    if s <= 0:
        return vec
    norm = math.sqrt(s)
    if norm == 0:
        return vec
    return [v / norm for v in vec]


def _apply_proxy_env():
    """Apply proxy via environment variables so that OpenAI SDK picks it up.
    Priority: config.yaml proxy.url or host/port -> set HTTPS_PROXY/HTTP_PROXY/ALL_PROXY
    """
    proxy_url = _resolve_proxy_url()
    if not proxy_url:
        return
    # Set or override common proxy env vars
    os.environ["HTTPS_PROXY"] = proxy_url
    os.environ["HTTP_PROXY"] = proxy_url
    os.environ["ALL_PROXY"] = proxy_url


class Embedding:
    """Wrapper for OpenAI embedding API.

    - Default model: text-embedding-3-small (dim=1536)
    - Set OPENAI_API_KEY in env/.env or config.yaml
    - Proxy: 从 config.yaml 的 proxy 节点或环境变量中读取，通过环境变量生效
    """

    def __init__(
        self,
        model: Optional[str] = None,
        api_key: Optional[str] = None,
        base_url: Optional[str] = None,
        timeout: Optional[int] = None,
    ) -> None:
        conf = _resolve_openai_conf(
            {
                "model": model,
                "api_key": api_key,
                "base_url": base_url,
                "timeout": timeout,
            }
        )
        self.model = conf["model"]
        self.timeout = conf["timeout"]

        # Apply proxy via env vars only (no http_client param)
        _apply_proxy_env()

        # Instantiate client (new SDK)
        if conf["base_url"]:
            self.client = OpenAI(api_key=conf["api_key"], base_url=conf["base_url"])  # type: ignore
        else:
            self.client = OpenAI(api_key=conf["api_key"])  # type: ignore

    def embed_text(self, text: str, normalize: bool = False) -> List[float]:
        if not isinstance(text, str):
            raise TypeError("text must be a string")
        text = text.strip()
        if not text:
            return []
        resp = self.client.embeddings.create(model=self.model, input=text)
        vec = resp.data[0].embedding  # type: ignore
        if normalize:
            vec = _l2_normalize(vec)
        return vec

    def embed_texts(self, texts: List[str], normalize: bool = False, batch_size: int = 128) -> List[List[float]]:
        if not texts:
            return []
        results: List[List[float]] = []
        # OpenAI embeddings API 支持批量输入，但也限制单次输入长度/数量；按 batch 处理更稳妥
        start = 0
        n = len(texts)
        while start < n:
            batch = [t.strip() if isinstance(t, str) else "" for t in texts[start : start + batch_size]]
            # 过滤全空的项，保留占位
            to_call = [t if t else " " for t in batch]
            resp = self.client.embeddings.create(model=self.model, input=to_call)
            vecs = [d.embedding for d in resp.data]  # type: ignore
            if normalize:
                vecs = [_l2_normalize(v) for v in vecs]
            results.extend(vecs)
            start += batch_size
        return results



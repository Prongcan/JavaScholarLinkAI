"""
Simple runner for BlogGenerator moved out from service/Generate_blogs.py.
Usage:
  - From project root:
      python -m backend.test.test_generate_blogs
  - Or run directly:
      python backend/test/test_generate_blogs.py
Edit PDF_URL below to your target paper URL.
"""
from __future__ import annotations

import os
import sys

# Ensure we can import from backend/service when run directly
CURRENT_DIR = os.path.dirname(__file__)
BACKEND_DIR = os.path.abspath(os.path.join(CURRENT_DIR, ".."))
if BACKEND_DIR not in sys.path:
    sys.path.insert(0, BACKEND_DIR)

from service.Generate_blogs import BlogGenerator  # noqa: E402


def main():
    # 直接写参数（不使用 argparse）
    PDF_URL = "https://arxiv.org/pdf/1706.03762.pdf"  # TODO: 修改为你的 PDF 链接
    generator = BlogGenerator()  # 从 config.yaml/.env 读取模型/语言/代理
    blog_markdown = generator.generate_from_pdf_url(PDF_URL)
    print(blog_markdown)


if __name__ == "__main__":
    main()


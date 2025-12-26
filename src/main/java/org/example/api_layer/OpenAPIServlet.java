package org.example.api_layer;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "openAPIServlet", urlPatterns = {"/v3/api-docs"})
public class OpenAPIServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String openApiJson = """
            {
                "openapi": "3.0.3",
                "info": {
                    "title": "JavaFinal API",
                    "version": "1.0",
                    "description": "JavaFinal 项目后端 API 文档",
                    "contact": {
                        "name": "开发团队",
                        "email": "dev@example.com"
                    },
                    "license": {
                        "name": "MIT",
                        "url": "https://opensource.org/licenses/MIT"
                    }
                },
                "servers": [
                    {
                        "url": "http://localhost:8080",
                        "description": "本地开发服务器"
                    }
                ],
                "paths": {
                    "/api/hello": {
                        "get": {
                            "summary": "获取 Hello World 消息",
                            "description": "返回一个简单的 Hello World 消息",
                            "responses": {
                                "200": {
                                    "description": "成功响应",
                                    "content": {
                                        "text/plain": {
                                            "schema": {
                                                "type": "string",
                                                "example": "Hello World"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "/api/users/list": {
                        "get": {
                            "summary": "获取用户列表",
                            "description": "分页获取所有用户列表",
                            "parameters": [
                                {
                                    "name": "page",
                                    "in": "query",
                                    "schema": {
                                        "type": "integer",
                                        "default": 1
                                    }
                                },
                                {
                                    "name": "page_size",
                                    "in": "query",
                                    "schema": {
                                        "type": "integer",
                                        "default": 10
                                    }
                                }
                            ],
                            "responses": {
                                "200": {
                                    "description": "成功获取用户列表",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "type": "object",
                                                "properties": {
                                                    "status": {"type": "string", "example": "success"},
                                                    "message": {"type": "string", "example": "获取用户列表成功"},
                                                    "data": {
                                                        "type": "object",
                                                        "properties": {
                                                            "users": {
                                                                "type": "array",
                                                                "items": {
                                                                    "type": "object",
                                                                    "properties": {
                                                                        "user_id": {"type": "integer"},
                                                                        "username": {"type": "string"},
                                                                        "interest": {"type": "string"}
                                                                    }
                                                                }
                                                            },
                                                            "page": {"type": "integer"},
                                                            "page_size": {"type": "integer"}
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "/api/users/{userId}": {
                        "get": {
                            "summary": "获取用户信息",
                            "description": "根据用户 ID 获取用户信息",
                            "parameters": [
                                {
                                    "name": "userId",
                                    "in": "path",
                                    "required": true,
                                    "schema": {"type": "integer"}
                                }
                            ],
                            "responses": {
                                "200": {"description": "成功获取用户信息"},
                                "404": {"description": "用户不存在"}
                            }
                        },
                        "delete": {
                            "summary": "删除用户",
                            "description": "删除指定用户",
                            "parameters": [
                                {
                                    "name": "userId",
                                    "in": "path",
                                    "required": true,
                                    "schema": {"type": "integer"}
                                }
                            ],
                            "responses": {
                                "200": {"description": "删除成功"},
                                "404": {"description": "用户不存在"}
                            }
                        }
                    },
                    "/api/users/register": {
                        "post": {
                            "summary": "用户注册",
                            "description": "注册新用户账号",
                            "requestBody": {
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "required": ["username", "password"],
                                            "properties": {
                                                "username": {"type": "string", "description": "用户名"},
                                                "password": {"type": "string", "description": "密码"},
                                                "interest": {"type": "string", "description": "兴趣"}
                                            }
                                        }
                                    }
                                },
                                "required": true
                            },
                            "responses": {
                                "200": {"description": "注册成功"},
                                "409": {"description": "用户名已存在"}
                            }
                        }
                    },
                    "/api/users/{userId}/interest": {
                        "get": {
                            "summary": "获取用户兴趣",
                            "description": "获取指定用户的兴趣",
                            "parameters": [
                                {
                                    "name": "userId",
                                    "in": "path",
                                    "required": true,
                                    "schema": {"type": "integer"}
                                }
                            ],
                            "responses": {
                                "200": {"description": "成功获取用户兴趣"},
                                "404": {"description": "用户不存在"}
                            }
                        },
                        "put": {
                            "summary": "更新用户兴趣",
                            "description": "更新指定用户的兴趣",
                            "parameters": [
                                {
                                    "name": "userId",
                                    "in": "path",
                                    "required": true,
                                    "schema": {"type": "integer"}
                                }
                            ],
                            "requestBody": {
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "required": ["interest"],
                                            "properties": {
                                                "interest": {"type": "string", "description": "用户兴趣"}
                                            }
                                        }
                                    }
                                },
                                "required": true
                            },
                            "responses": {
                                "200": {"description": "更新成功"},
                                "404": {"description": "用户不存在"}
                            }
                        }
                    },
                    "/api/papers/list": {
                        "get": {
                            "summary": "获取论文列表",
                            "description": "分页获取所有论文列表",
                            "parameters": [
                                {
                                    "name": "page",
                                    "in": "query",
                                    "schema": {
                                        "type": "integer",
                                        "default": 1
                                    }
                                },
                                {
                                    "name": "page_size",
                                    "in": "query",
                                    "schema": {
                                        "type": "integer",
                                        "default": 10
                                    }
                                }
                            ],
                            "responses": {
                                "200": {
                                    "description": "成功获取论文列表",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "type": "object",
                                                "properties": {
                                                    "status": {"type": "string", "example": "success"},
                                                    "message": {"type": "string", "example": "获取论文列表成功"},
                                                    "data": {
                                                        "type": "object",
                                                        "properties": {
                                                            "papers": {
                                                                "type": "array",
                                                                "items": {
                                                                    "type": "object",
                                                                    "properties": {
                                                                        "paper_id": {"type": "integer"},
                                                                        "title": {"type": "string"},
                                                                        "author": {"type": "string"},
                                                                        "abstract": {"type": "string"},
                                                                        "pdf_url": {"type": "string"}
                                                                    }
                                                                }
                                                            },
                                                            "page": {"type": "integer"},
                                                            "page_size": {"type": "integer"},
                                                            "total": {"type": "integer"}
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    "/api/papers/{paperId}": {
                        "get": {
                            "summary": "获取论文详情",
                            "description": "根据论文 ID 获取论文详细信息",
                            "parameters": [
                                {
                                    "name": "paperId",
                                    "in": "path",
                                    "required": true,
                                    "schema": {"type": "integer"}
                                }
                            ],
                            "responses": {
                                "200": {
                                    "description": "成功获取论文详情",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "type": "object",
                                                "properties": {
                                                    "status": {"type": "string", "example": "success"},
                                                    "message": {"type": "string", "example": "获取论文详情成功"},
                                                    "data": {
                                                        "type": "object",
                                                        "properties": {
                                                            "paper_id": {"type": "integer"},
                                                            "title": {"type": "string"},
                                                            "author": {"type": "string"},
                                                            "abstract": {"type": "string"},
                                                            "pdf_url": {"type": "string"}
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                "404": {"description": "论文不存在"}
                            }
                        }
                    },
                    "/api/papers/fetch": {
                        "post": {
                            "summary": "从arXiv抓取论文",
                            "description": "从arXiv获取并添加新的论文到数据库",
                            "requestBody": {
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "required": ["arxiv_id"],
                                            "properties": {
                                                "arxiv_id": {"type": "string", "description": "arXiv论文ID", "example": "2301.00001"}
                                            }
                                        }
                                    }
                                },
                                "required": true
                            },
                            "responses": {
                                "200": {
                                    "description": "论文抓取成功",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "type": "object",
                                                "properties": {
                                                    "status": {"type": "string", "example": "success"},
                                                    "message": {"type": "string", "example": "论文抓取成功"},
                                                    "data": {
                                                        "type": "object",
                                                        "properties": {
                                                            "paper_id": {"type": "integer"}
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                "400": {"description": "请求参数错误"},
                                "500": {"description": "服务器内部错误"}
                            }
                        }
                    },
                    "/api/papers/{paperId}/generate-blog": {
                        "post": {
                            "summary": "生成博客",
                            "description": "根据论文内容生成博客文章",
                            "parameters": [
                                {
                                    "name": "paperId",
                                    "in": "path",
                                    "required": true,
                                    "schema": {"type": "integer"},
                                    "description": "论文ID"
                                }
                            ],
                            "requestBody": {
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "required": ["user_id"],
                                            "properties": {
                                                "user_id": {"type": "integer", "description": "用户ID"}
                                            }
                                        }
                                    }
                                },
                                "required": true
                            },
                            "responses": {
                                "200": {
                                    "description": "博客生成成功",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "type": "object",
                                                "properties": {
                                                    "status": {"type": "string", "example": "success"},
                                                    "message": {"type": "string", "example": "博客生成成功"},
                                                    "data": {
                                                        "type": "object",
                                                        "properties": {
                                                            "blog_content": {"type": "string", "description": "生成的博客内容"}
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                "400": {"description": "请求参数错误"},
                                "404": {"description": "论文不存在"},
                                "500": {"description": "服务器内部错误"}
                            }
                        }
                    },
                    "/api/index/paper": {
                        "post": {
                            "summary": "为论文创建向量索引",
                            "description": "将指定论文的摘要转换为向量并存储到向量数据库中",
                            "requestBody": {
                                "content": {
                                    "application/json": {
                                        "schema": {
                                            "type": "object",
                                            "required": ["paper_id"],
                                            "properties": {
                                                "paper_id": {"type": "integer", "description": "论文ID"}
                                            }
                                        }
                                    }
                                },
                                "required": true
                            },
                            "responses": {
                                "200": {
                                    "description": "索引创建成功",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "type": "object",
                                                "properties": {
                                                    "status": {"type": "string", "example": "success"},
                                                    "message": {"type": "string", "example": "论文索引创建成功"},
                                                    "data": {
                                                        "type": "object",
                                                        "properties": {
                                                            "paper_id": {"type": "integer"}
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                "400": {"description": "请求参数错误"},
                                "404": {"description": "论文不存在"},
                                "409": {"description": "论文已建立索引"},
                                "500": {"description": "服务器内部错误"}
                            }
                        }
                    },
                    "/api/index/paper/{paperId}/status": {
                        "get": {
                            "summary": "检查论文索引状态",
                            "description": "检查指定论文是否已建立向量索引",
                            "parameters": [
                                {
                                    "name": "paperId",
                                    "in": "path",
                                    "required": true,
                                    "schema": {"type": "integer"},
                                    "description": "论文ID"
                                }
                            ],
                            "responses": {
                                "200": {
                                    "description": "查询成功",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "type": "object",
                                                "properties": {
                                                    "status": {"type": "string", "example": "success"},
                                                    "message": {"type": "string", "example": "索引状态查询成功"},
                                                    "data": {
                                                        "type": "object",
                                                        "properties": {
                                                            "paper_id": {"type": "integer"},
                                                            "indexed": {"type": "boolean", "description": "是否已建立索引"}
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                "500": {"description": "服务器内部错误"}
                            }
                        }
                    },
                    "/api/orchestrator/fetch": {
                        "post": {
                            "summary": "运行Fetch Orchestrator",
                            "description": "启动Fetch Orchestrator，从arXiv获取最新的计算机科学论文，自动抓取论文信息并创建向量索引",
                            "responses": {
                                "200": {
                                    "description": "Orchestrator启动成功",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "type": "object",
                                                "properties": {
                                                    "status": {"type": "string", "example": "success"},
                                                    "message": {"type": "string", "example": "Fetch Orchestrator started successfully"},
                                                    "data": {
                                                        "type": "object",
                                                        "properties": {
                                                            "note": {"type": "string", "description": "处理说明"}
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                "500": {"description": "Orchestrator启动失败"}
                            }
                        }
                    },
                    "/api/orchestrator/recommend": {
                        "post": {
                            "summary": "运行Recommendation Orchestrator",
                            "description": "启动Recommendation Orchestrator，基于用户兴趣计算论文相似度并生成个性化推荐博客",
                            "responses": {
                                "200": {
                                    "description": "Orchestrator启动成功",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "type": "object",
                                                "properties": {
                                                    "status": {"type": "string", "example": "success"},
                                                    "message": {"type": "string", "example": "Recommendation Orchestrator started successfully"},
                                                    "data": {
                                                        "type": "object",
                                                        "properties": {
                                                            "note": {"type": "string", "description": "处理说明"}
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                "500": {"description": "Orchestrator启动失败"}
                            }
                        }
                    },
                    "/api/mock": {
                        "get": {
                            "summary": "模拟接口",
                            "description": "根据不同的 action 参数执行相应的模拟操作",
                            "parameters": [
                                {
                                    "name": "action",
                                    "in": "query",
                                    "required": true,
                                    "schema": {
                                        "type": "string",
                                        "enum": ["readPaper", "uploadPaper", "searchPaper", "deletePaper"]
                                    },
                                    "description": "操作类型"
                                }
                            ],
                            "responses": {
                                "200": {
                                    "description": "操作成功",
                                    "content": {
                                        "application/json": {
                                            "schema": {
                                                "type": "object",
                                                "properties": {
                                                    "status": {"type": "string", "example": "success"},
                                                    "message": {"type": "string"}
                                                }
                                            }
                                        }
                                    }
                                },
                                "400": {"description": "缺少 action 参数或未知操作"},
                                "500": {"description": "服务器内部错误"}
                            }
                        }
                    }
                }
            }
            """;

        PrintWriter out = response.getWriter();
        out.print(openApiJson);
        out.flush();
    }
}
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Mock API Interface</title>
    <style>
        body { font-family: sans-serif; }
        .container { margin: 20px; }
        .btn {
            background-color: #4CAF50;
            color: white;
            padding: 10px 15px;
            margin: 5px;
            border: none;
            cursor: pointer;
            border-radius: 4px;
        }
        .btn:hover { background-color: #45a049; }
        #api-result {
            margin-top: 20px;
            padding: 10px;
            border: 1px solid #ccc;
            border-radius: 4px;
            background-color: #f9f9f9;
        }
    </style>
    <script>
        // 最终调试：为“阅读论文”按钮创建一个完全硬编码的函数
        function callReadPaperApi() {
            const resultDiv = document.getElementById('api-result');
            resultDiv.innerText = '正在请求 \'readPaper\'...';

            const url = '<%= request.getContextPath() %>/api/mock?action=readPaper&_=' + new Date().getTime();

            fetch(url)
                .then(function(response) {
                    if (!response.ok) {
                        throw new Error('HTTP 错误! 状态码: ' + response.status + ' - ' + response.statusText);
                    }
                    return response.json();
                })
                .then(function(data) {
                    let responseText = '状态: ' + data.status + '\n消息: ' + data.message;
                    if (data.error_details) {
                        responseText += '\n\n错误详情:\n' + data.error_details;
                    }
                    resultDiv.innerText = responseText;
                    resultDiv.style.color = data.status === 'error' ? 'red' : 'green';
                })
                .catch(function(error) {
                    resultDiv.innerText = '请求失败: ' + error;
                    resultDiv.style.color = 'red';
                });
        }

        // 其他按钮的通用函数
        function callMockApi(action) {
            const resultDiv = document.getElementById('api-result');
            resultDiv.innerText = '正在请求 \'' + action + '\'...';
            
            const url = '<%= request.getContextPath() %>/api/mock?action=' + action + '&_=' + new Date().getTime();
            
            fetch(url)
                .then(function(response) {
                    if (!response.ok) {
                        throw new Error('HTTP 错误! 状态码: ' + response.status + ' - ' + response.statusText);
                    }
                    return response.json();
                })
                .then(function(data) {
                    let responseText = '状态: ' + data.status + '\n消息: ' + data.message;
                    if (data.error_details) {
                        responseText += '\n\n错误详情:\n' + data.error_details;
                    }
                    resultDiv.innerText = responseText;
                    resultDiv.style.color = data.status === 'error' ? 'red' : 'green';
                })
                .catch(function(error) {
                    resultDiv.innerText = '请求失败: ' + error;
                    resultDiv.style.color = 'red';
                });
        }
    </script>
</head>
<body>

<div class="container">
    <h1>学术论文链接AI - 模拟接口</h1>
    <p>点击下面的按钮来测试对应的后端 Mock API。</p>

    <div>
        <button class="btn" onclick="callReadPaperApi()">阅读论文 (最终测试)</button>
        <button class="btn" onclick="callMockApi('uploadPaper')">上传论文</button>
        <button class="btn" onclick="callMockApi('searchPaper')">搜索论文</button>
        <button class="btn" onclick="callMockApi('deletePaper')">删除论文</button>
        <button class="btn" onclick="callMockApi('unknownAction')">测试未知操作</button>
    </div>

    <h2>API 响应:</h2>
    <pre id="api-result">请点击一个按钮以获取结果。</pre>
</div>

</body>
</html>

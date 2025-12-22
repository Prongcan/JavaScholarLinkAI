<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>JSP - Hello World</title>
    <script>
        // 定义一个简单的 JS 函数来请求 API
        function fetchHello() {
            fetch('api/hello')
                .then(response => response.text()) // 假设返回的是文本
                .then(data => {
                    document.getElementById("api-result").innerText = "结果: " + data;
                    // 或者用 alert(data); 弹窗显示
                })
                .catch(error => console.error('Error:', error));
        }
    </script>
</head>
<body>
<h1><%= "Hello World!" %></h1>

<br/><br/>

<p><a href="api/hello">直接访问 API 链接</a></p>

<p>
    <a href="javascript:void(0)" onclick="fetchHello()">点击获取 API 结果 (不跳转)</a>
</p>
<div id="api-result" style="color: blue; font-weight: bold;"></div>

</body>
</html>
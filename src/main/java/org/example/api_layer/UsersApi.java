package org.example.api_layer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data_access_layer.Dbmanager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "usersApi", value = "/api/users/*")
@Tag(name = "用户管理", description = "用户相关的 API 操作")
public class UsersApi extends HttpServlet {
    private Dbmanager dbManager;
    private ObjectMapper objectMapper;
    
    @Override
    public void init() throws ServletException {
        super.init();
        dbManager = new Dbmanager();
        objectMapper = new ObjectMapper();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/list")) {
                // GET /api/users/list - 获取用户列表
                handleListUsers(request, response, out);
            } else if (pathInfo.startsWith("/")) {
                String[] pathParts = pathInfo.substring(1).split("/");
                if (pathParts.length == 1) {
                    // GET /api/users/{userId} - 获取用户信息
                    try {
                        int userId = Integer.parseInt(pathParts[0]);
                        handleGetUser(userId, response, out);
                    } catch (NumberFormatException e) {
                        sendError(response, out, 400, "Invalid user ID format");
                    }
                } else if (pathParts.length == 2 && pathParts[1].equals("interest")) {
                    // GET /api/users/{userId}/interest - 获取用户兴趣
                    try {
                        int userId = Integer.parseInt(pathParts[0]);
                        handleGetUserInterest(userId, response, out);
                    } catch (NumberFormatException e) {
                        sendError(response, out, 400, "Invalid user ID format");
                    }
                } else {
                    sendError(response, out, 404, "Not Found");
                }
            } else {
                sendError(response, out, 404, "Not Found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(response, out, 500, "Database error: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/register")) {
                // POST /api/users/register - 用户注册
                handleRegisterUser(request, response, out);
            } else {
                sendError(response, out, 404, "Not Found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(response, out, 500, "Database error: " + e.getMessage());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo != null && pathInfo.startsWith("/")) {
                String[] pathParts = pathInfo.substring(1).split("/");
                if (pathParts.length == 2 && pathParts[1].equals("interest")) {
                    // PUT /api/users/{userId}/interest - 更新用户兴趣
                    try {
                        int userId = Integer.parseInt(pathParts[0]);
                        handleUpdateUserInterest(userId, request, response, out);
                    } catch (NumberFormatException e) {
                        sendError(response, out, 400, "Invalid user ID format");
                    }
                } else {
                    sendError(response, out, 404, "Not Found");
                }
            } else {
                sendError(response, out, 404, "Not Found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(response, out, 500, "Database error: " + e.getMessage());
        }
    }
    
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();
        
        try {
            if (pathInfo != null && pathInfo.startsWith("/")) {
                String[] pathParts = pathInfo.substring(1).split("/");
                if (pathParts.length == 1) {
                    // DELETE /api/users/{userId} - 删除用户
                    try {
                        int userId = Integer.parseInt(pathParts[0]);
                        handleDeleteUser(userId, response, out);
                    } catch (NumberFormatException e) {
                        sendError(response, out, 400, "Invalid user ID format");
                    }
                } else {
                    sendError(response, out, 404, "Not Found");
                }
            } else {
                sendError(response, out, 404, "Not Found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(response, out, 500, "Database error: " + e.getMessage());
        }
    }
    
    /**
     * 处理用户注册
     */
    @Operation(
        summary = "用户注册",
        description = "注册新用户账号",
        requestBody = @RequestBody(
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "object"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "注册成功"),
            @ApiResponse(responseCode = "409", description = "用户名已存在"),
            @ApiResponse(responseCode = "400", description = "请求参数错误")
        }
    )
    private void handleRegisterUser(HttpServletRequest request, HttpServletResponse response,
                                    PrintWriter out) throws IOException, SQLException {
        BufferedReader reader = request.getReader();
        StringBuilder jsonBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBody.append(line);
        }
        
        Map<String, String> requestData;
        try {
            requestData = objectMapper.readValue(jsonBody.toString(), Map.class);
        } catch (Exception e) {
            sendError(response, out, 400, "Invalid JSON format");
            return;
        }
        
        String username = requestData.get("username");
        String password = requestData.get("password");
        String interest = requestData.get("interest");
        
        if (username == null || username.isEmpty()) {
            sendError(response, out, 400, "Username is required");
            return;
        }
        
        if (password == null || password.isEmpty()) {
            sendError(response, out, 400, "Password is required");
            return;
        }
        
        // 检查用户名是否已存在
        Map<String, Object> existingUser = dbManager.getUserByUsername(username);
        if (existingUser != null) {
            sendError(response, out, 409, "Username already exists");
            return;
        }
        
        int userId = dbManager.insertUser(username, password, interest != null ? interest : "");
        
        if (userId > 0) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "用户注册成功");
            Map<String, Object> data = new HashMap<>();
            data.put("user_id", userId);
            data.put("username", username);
            result.put("data", data);
            
            out.print(objectMapper.writeValueAsString(result));
        } else {
            sendError(response, out, 500, "Failed to register user");
        }
        out.flush();
    }
    
    /**
     * 处理获取用户信息
     */
    @Operation(
        summary = "获取用户信息",
        description = "根据用户 ID 获取用户信息",
        parameters = {
            @Parameter(name = "userId", description = "用户 ID", required = true, schema = @Schema(type = "integer"))
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "成功获取用户信息"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
        }
    )
    private void handleGetUser(int userId, HttpServletResponse response,
                               PrintWriter out) throws SQLException, IOException {
        Map<String, Object> user = dbManager.getUserById(userId);
        
        if (user == null) {
            sendError(response, out, 404, "User not found");
            return;
        }
        
        // 不返回密码
        user.remove("password");
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "获取用户信息成功");
        result.put("data", user);
        
        out.print(objectMapper.writeValueAsString(result));
        out.flush();
    }
    
    /**
     * 处理获取用户列表
     */
    @Operation(
        summary = "获取用户列表",
        description = "分页获取所有用户列表",
        parameters = {
            @Parameter(name = "page", description = "页码", schema = @Schema(type = "integer", defaultValue = "1")),
            @Parameter(name = "page_size", description = "每页大小", schema = @Schema(type = "integer", defaultValue = "10"))
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "成功获取用户列表")
        }
    )
    private void handleListUsers(HttpServletRequest request, HttpServletResponse response,
                                 PrintWriter out) throws SQLException, IOException {
        String pageParam = request.getParameter("page");
        String pageSizeParam = request.getParameter("page_size");
        
        int page = pageParam != null ? Integer.parseInt(pageParam) : 1;
        int pageSize = pageSizeParam != null ? Integer.parseInt(pageSizeParam) : 10;
        
        List<Map<String, Object>> users = dbManager.getAllUsers(page, pageSize);
        
        // 移除密码字段
        for (Map<String, Object> user : users) {
            user.remove("password");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "获取用户列表成功");
        Map<String, Object> data = new HashMap<>();
        data.put("users", users);
        data.put("page", page);
        data.put("page_size", pageSize);
        result.put("data", data);
        
        out.print(objectMapper.writeValueAsString(result));
        out.flush();
    }
    
    /**
     * 处理获取用户兴趣
     */
    private void handleGetUserInterest(int userId, HttpServletResponse response, 
                                       PrintWriter out) throws SQLException, IOException {
        Map<String, Object> user = dbManager.getUserById(userId);
        
        if (user == null) {
            sendError(response, out, 404, "User not found");
            return;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "获取用户兴趣成功");
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);
        data.put("interest", user.get("interest"));
        result.put("data", data);
        
        out.print(objectMapper.writeValueAsString(result));
        out.flush();
    }
    
    /**
     * 处理更新用户兴趣
     */
    @Operation(
        summary = "更新用户兴趣",
        description = "更新指定用户的兴趣",
        parameters = {
            @Parameter(name = "userId", description = "用户 ID", required = true, schema = @Schema(type = "integer"))
        },
        requestBody = @RequestBody(
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(
                    type = "object"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在"),
            @ApiResponse(responseCode = "400", description = "请求参数错误")
        }
    )
    private void handleUpdateUserInterest(int userId, HttpServletRequest request,
                                         HttpServletResponse response, PrintWriter out)
            throws IOException, SQLException {
        BufferedReader reader = request.getReader();
        StringBuilder jsonBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBody.append(line);
        }
        
        Map<String, String> requestData;
        try {
            requestData = objectMapper.readValue(jsonBody.toString(), Map.class);
        } catch (Exception e) {
            sendError(response, out, 400, "Invalid JSON format");
            return;
        }
        
        String interest = requestData.get("interest");
        
        if (interest == null) {
            sendError(response, out, 400, "Interest is required");
            return;
        }
        
        boolean updated = dbManager.updateUserInterest(userId, interest);
        
        if (updated) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "更新用户兴趣成功");
            Map<String, Object> data = new HashMap<>();
            data.put("user_id", userId);
            data.put("interest", interest);
            result.put("data", data);
            
            out.print(objectMapper.writeValueAsString(result));
        } else {
            sendError(response, out, 404, "User not found");
        }
        out.flush();
    }
    
    /**
     * 处理删除用户
     */
    @Operation(
        summary = "删除用户",
        description = "删除指定用户",
        parameters = {
            @Parameter(name = "userId", description = "用户 ID", required = true, schema = @Schema(type = "integer"))
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "404", description = "用户不存在")
        }
    )
    private void handleDeleteUser(int userId, HttpServletResponse response,
                                 PrintWriter out) throws SQLException, IOException {
        boolean deleted = dbManager.deleteUser(userId);
        
        if (deleted) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "删除用户成功");
            
            out.print(objectMapper.writeValueAsString(result));
        } else {
            sendError(response, out, 404, "User not found");
        }
        out.flush();
    }
    
    /**
     * 发送错误响应
     */
    private void sendError(HttpServletResponse response, PrintWriter out, 
                          int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        Map<String, Object> error = new HashMap<>();
        error.put("status", "error");
        error.put("message", message);
        out.print(objectMapper.writeValueAsString(error));
        out.flush();
    }
    
    @Override
    public void destroy() {
        super.destroy();
        if (dbManager != null) {
            dbManager.closeConnection();
        }
    }
}


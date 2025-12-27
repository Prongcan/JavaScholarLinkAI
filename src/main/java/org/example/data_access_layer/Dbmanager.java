package org.example.data_access_layer;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 数据库管理类
 * 负责数据库连接和基本操作
 */
public class Dbmanager {
    private String DB_URL;
    private String DB_USER;
    private String DB_PASSWORD;

    private Connection connection;

    /**
     * 构造函数：从配置文件加载数据库连接信息
     */
    public Dbmanager() {
        loadDatabaseConfig();
    }

    /**
     * 从配置文件加载数据库配置
     */
    private void loadDatabaseConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
                DB_URL = props.getProperty("db.url");
                DB_USER = props.getProperty("db.user");
                DB_PASSWORD = props.getProperty("db.password");
            } else {
                // 如果配置文件不存在，使用默认值
                DB_URL = "jdbc:mysql://localhost:3306/scholarlink_ai?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
                DB_USER = "root";
                DB_PASSWORD = "Sehun19940412";
            }
        } catch (Exception e) {
            // 如果加载失败，使用默认值
            System.err.println("Warning: Failed to load db.properties, using default values");
            DB_URL = "jdbc:mysql://localhost:3306/scholarlink_ai?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true";
            DB_USER = "root";
            DB_PASSWORD = "Sehun19940412";
        }
    }

    /**
     * 获取数据库连接
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver not found", e);
            }
        }
        return connection;
    }

    /**
     * 关闭数据库连接
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ========== Papers 表操作 ==========

    /**
     * 插入论文
     */
    public int insertPaper(String title, String author, String abstractText, String pdfUrl) throws SQLException {
        String sql = "INSERT INTO papers (title, author, abstract, pdf_url) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, title);
            stmt.setString(2, author);
            stmt.setString(3, abstractText);
            stmt.setString(4, pdfUrl);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * 根据ID获取论文
     */
    public Map<String, Object> getPaperById(int paperId) throws SQLException {
        String sql = "SELECT paper_id, title, author, abstract, pdf_url FROM papers WHERE paper_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, paperId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> paper = new HashMap<>();
                    paper.put("paper_id", rs.getInt("paper_id"));
                    paper.put("title", rs.getString("title"));
                    paper.put("author", rs.getString("author"));
                    paper.put("abstract", rs.getString("abstract"));
                    paper.put("pdf_url", rs.getString("pdf_url"));
                    return paper;
                }
            }
        }
        return null;
    }

    /**
     * 获取所有论文（支持分页）
     */
    public List<Map<String, Object>> getAllPapers(int page, int pageSize) throws SQLException {
        List<Map<String, Object>> papers = new ArrayList<>();
        String sql = "SELECT paper_id, title, author, abstract, pdf_url FROM papers ORDER BY paper_id DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> paper = new HashMap<>();
                    paper.put("paper_id", rs.getInt("paper_id"));
                    paper.put("title", rs.getString("title"));
                    paper.put("author", rs.getString("author"));
                    paper.put("abstract", rs.getString("abstract"));
                    paper.put("pdf_url", rs.getString("pdf_url"));
                    papers.add(paper);
                }
            }
        }
        return papers;
    }

    /**
     * 获取论文总数
     */
    public int getPaperCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM papers";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }

    // ========== Users 表操作 ==========

    /**
     * 插入用户
     */
    public int insertUser(String username, String password, String interest) throws SQLException {
        String sql = "INSERT INTO users (username, password, interest, frequency) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, interest);
            stmt.setInt(4, 24); // 默认推荐频率为24小时（一天一次）
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * 根据ID获取用户
     */
    public Map<String, Object> getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, username, password, interest, frequency FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("user_id", rs.getInt("user_id"));
                    user.put("username", rs.getString("username"));
                    user.put("password", rs.getString("password"));
                    user.put("interest", rs.getString("interest"));
                    user.put("frequency", rs.getInt("frequency"));
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * 根据用户名获取用户
     */
    public Map<String, Object> getUserByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, password, interest, frequency FROM users WHERE username = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("user_id", rs.getInt("user_id"));
                    user.put("username", rs.getString("username"));
                    user.put("password", rs.getString("password"));
                    user.put("interest", rs.getString("interest"));
                    user.put("frequency", rs.getInt("frequency"));
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * 根据用户名和密码获取用户（用于登录验证）
     */
    public Map<String, Object> getUserByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, password, interest, frequency FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("user_id", rs.getInt("user_id"));
                    user.put("username", rs.getString("username"));
                    user.put("password", rs.getString("password"));
                    user.put("interest", rs.getString("interest"));
                    user.put("frequency", rs.getInt("frequency"));
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * 获取所有用户（支持分页）
     */
    public List<Map<String, Object>> getAllUsers(int page, int pageSize) throws SQLException {
        List<Map<String, Object>> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password, interest, frequency FROM users ORDER BY user_id DESC LIMIT ? OFFSET ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, pageSize);
            stmt.setInt(2, (page - 1) * pageSize);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("user_id", rs.getInt("user_id"));
                    user.put("username", rs.getString("username"));
                    user.put("password", rs.getString("password"));
                    user.put("interest", rs.getString("interest"));
                    user.put("frequency", rs.getInt("frequency"));
                    users.add(user);
                }
            }
        }
        return users;
    }

    /**
     * 更新用户兴趣
     */
    public boolean updateUserInterest(int userId, String interest) throws SQLException {
        String sql = "UPDATE users SET interest = ? WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setString(1, interest);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * 更新用户推荐频率
     */
    public boolean updateUserFrequency(int userId, int frequency) throws SQLException {
        String sql = "UPDATE users SET frequency = ? WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, frequency);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * 删除用户
     */
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    // ========== Recommendations 表操作 ==========

    /**
     * 插入推荐记录
     */
    public int insertRecommendation(int userId, int paperId, String blog) throws SQLException {
        String sql = "INSERT INTO recommendations (user_id, paper_id, blog) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE blog = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, paperId);
            stmt.setString(3, blog);
            stmt.setString(4, blog);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * 获取用户的推荐记录, 可根据兴趣进行筛选
     */
    public List<Map<String, Object>> getRecommendationsByUserId(int userId, String interests) throws SQLException {
        List<Map<String, Object>> recommendations = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder(
            "SELECT r.id, r.user_id, r.paper_id, r.blog, r.created_at, p.title, p.author " +
            "FROM recommendations r " +
            "JOIN papers p ON r.paper_id = p.paper_id " +
            "WHERE r.user_id = ?"
        );

        List<String> interestKeywords = new ArrayList<>();
        if (interests != null && !interests.trim().isEmpty()) {
            String[] keywords = interests.split("[,\\s]+"); // 按逗号或空格分割
            
            if (keywords.length > 0) {
                sql.append(" AND (");
                for (int i = 0; i < keywords.length; i++) {
                    String keyword = keywords[i].trim();
                    if (!keyword.isEmpty()) {
                        interestKeywords.add(keyword);
                        if (i > 0) {
                            sql.append(" OR ");
                        }
                        sql.append("p.title LIKE ? OR p.abstract LIKE ?");
                    }
                }
                sql.append(")");
            }
        }
        
        sql.append(" ORDER BY r.created_at DESC");

        try (PreparedStatement stmt = getConnection().prepareStatement(sql.toString())) {
            int paramIndex = 1;
            stmt.setInt(paramIndex++, userId);
            
            for (String keyword : interestKeywords) {
                String searchPattern = "%" + keyword + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> rec = new HashMap<>();
                    rec.put("id", rs.getInt("id"));
                    rec.put("user_id", rs.getInt("user_id"));
                    rec.put("paper_id", rs.getInt("paper_id"));
                    rec.put("blog", rs.getString("blog"));
                    rec.put("created_at", rs.getTimestamp("created_at"));
                    rec.put("paper_title", rs.getString("title"));
                    rec.put("paper_author", rs.getString("author"));
                    recommendations.add(rec);
                }
            }
        }
        return recommendations;
    }

    /**
     * 插入论文向量嵌入
     * @param paperId 论文ID
     * @param embeddingJson 向量JSON字符串
     * @param dimension 向量维度
     * @return 是否成功
     * @throws SQLException 如果数据库操作失败
     */
    public boolean insertPaperEmbedding(int paperId, String embeddingJson, int dimension) throws SQLException {
        String sql = "INSERT INTO paper_embeddings (paper_id, embedding, dimension, created_at) " +
                     "VALUES (?, ?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE embedding = ?, dimension = ?, updated_at = NOW()";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, paperId);
            stmt.setString(2, embeddingJson);
            stmt.setInt(3, dimension);
            stmt.setString(4, embeddingJson);
            stmt.setInt(5, dimension);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * 检查论文是否已建立向量索引
     * @param paperId 论文ID
     * @return 是否已建立索引
     * @throws SQLException 如果数据库查询失败
     */
    public boolean isPaperIndexed(int paperId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM paper_embeddings WHERE paper_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, paperId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    /**
     * 获取论文向量嵌入
     * @param paperId 论文ID
     * @return 向量数据Map，包含embedding和dimension
     * @throws SQLException 如果数据库查询失败
     */
    public Map<String, Object> getPaperEmbedding(int paperId) throws SQLException {
        String sql = "SELECT embedding, dimension, created_at FROM paper_embeddings WHERE paper_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, paperId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> embedding = new HashMap<>();
                    embedding.put("embedding", rs.getString("embedding"));
                    embedding.put("dimension", rs.getInt("dimension"));
                    embedding.put("created_at", rs.getTimestamp("created_at"));
                    return embedding;
                }
            }
        }
        return null;
    }

    // ========== Interest Embeddings 表操作 ==========

    /**
     * 插入或更新用户兴趣向量嵌入
     * @param userId 用户ID
     * @param embeddingJson 向量JSON字符串
     * @param dimension 向量维度
     * @return 是否成功
     * @throws SQLException 如果数据库操作失败
     */
    public boolean insertOrUpdateInterestEmbedding(int userId, String embeddingJson, int dimension) throws SQLException {
        String sql = "INSERT INTO interest_embeddings (user_id, embedding, dimension, created_at) " +
                     "VALUES (?, ?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE embedding = ?, dimension = ?, updated_at = NOW()";

        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, embeddingJson);
            stmt.setInt(3, dimension);
            stmt.setString(4, embeddingJson);
            stmt.setInt(5, dimension);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    /**
     * 检查用户兴趣是否已建立向量索引
     * @param userId 用户ID
     * @return 是否已建立索引
     * @throws SQLException 如果数据库查询失败
     */
    public boolean isUserInterestIndexed(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM interest_embeddings WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        return false;
    }

    /**
     * 获取用户兴趣向量嵌入
     * @param userId 用户ID
     * @return 向量数据Map，包含embedding和dimension
     * @throws SQLException 如果数据库查询失败
     */
    public Map<String, Object> getUserInterestEmbedding(int userId) throws SQLException {
        String sql = "SELECT embedding, dimension, created_at FROM interest_embeddings WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> embedding = new HashMap<>();
                    embedding.put("embedding", rs.getString("embedding"));
                    embedding.put("dimension", rs.getInt("dimension"));
                    embedding.put("created_at", rs.getTimestamp("created_at"));
                    return embedding;
                }
            }
        }
        return null;
    }

    /**
     * 删除用户兴趣向量嵌入
     * @param userId 用户ID
     * @return 是否成功
     * @throws SQLException 如果数据库操作失败
     */
    public boolean deleteUserInterestEmbedding(int userId) throws SQLException {
        String sql = "DELETE FROM interest_embeddings WHERE user_id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }
}

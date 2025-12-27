-- 用户兴趣历史记录迁移脚本
-- 此脚本用于修改interest_embeddings表结构，支持保存用户兴趣的历史记录

-- 1. 移除user_id上的UNIQUE约束
-- 注意：MySQL不支持直接移除UNIQUE约束，需要重建表或者删除索引
-- 这里我们选择重建表的方式

-- 创建临时表保存现有数据
CREATE TABLE temp_interest_embeddings AS
SELECT * FROM interest_embeddings;

-- 删除原有表
DROP TABLE interest_embeddings;

-- 重新创建表，移除UNIQUE约束，添加created_at索引
CREATE TABLE interest_embeddings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    embedding JSON NOT NULL, -- 存储OpenAI向量嵌入的JSON格式
    dimension INT NOT NULL,   -- 向量维度 (text-embedding-3-small: 1536)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
);

-- 恢复数据
INSERT INTO interest_embeddings (user_id, embedding, dimension, created_at, updated_at)
SELECT user_id, embedding, dimension, created_at, updated_at
FROM temp_interest_embeddings;

-- 删除临时表
DROP TABLE temp_interest_embeddings;

COMMIT;

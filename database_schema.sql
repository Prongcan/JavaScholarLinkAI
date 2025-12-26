-- ScholarLinkAI 论文向量嵌入表
-- 用于存储论文摘要的向量表示

CREATE TABLE IF NOT EXISTS paper_embeddings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    paper_id INT NOT NULL UNIQUE,
    embedding JSON NOT NULL, -- 存储OpenAI向量嵌入的JSON格式
    dimension INT NOT NULL,   -- 向量维度 (text-embedding-3-small: 1536)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (paper_id) REFERENCES papers(paper_id),
    INDEX idx_paper_id (paper_id)
);

-- 创建索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_paper_embeddings_paper ON paper_embeddings(paper_id);

COMMIT;

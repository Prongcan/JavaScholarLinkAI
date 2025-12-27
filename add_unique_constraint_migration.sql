-- 迁移脚本：为interest_embeddings表的user_id字段添加唯一约束
-- 执行前请确保没有重复的user_id数据，否则会报错
-- 如果有重复数据，脚本会自动删除旧记录，只保留最新的

-- 删除重复的user_id记录，只保留最新的（按created_at排序）
DELETE t1 FROM interest_embeddings t1
INNER JOIN interest_embeddings t2
WHERE t1.user_id = t2.user_id
AND t1.created_at < t2.created_at;

-- 为user_id字段添加唯一约束
ALTER TABLE interest_embeddings ADD UNIQUE (user_id);

COMMIT;

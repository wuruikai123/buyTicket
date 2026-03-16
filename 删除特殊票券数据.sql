-- 删除所有特殊票券数据
-- 注意：此操作不可恢复，请谨慎执行！

-- 1. 查看当前票券数量
SELECT 
    COUNT(*) as total_count,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as unused_count,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as used_count
FROM special_ticket;

-- 2. 删除所有特殊票券（包括已使用和未使用的）
DELETE FROM special_ticket;

-- 3. 重置自增ID（可选，如果想从1开始）
ALTER TABLE special_ticket AUTO_INCREMENT = 1;

-- 4. 验证删除结果
SELECT COUNT(*) as remaining_count FROM special_ticket;

-- 说明：
-- 执行完成后，special_ticket表将为空
-- 下次生成票券时，ID将从1开始（如果执行了步骤3）
-- 新的票券编号将使用随机字符格式：ST + 10位随机字母数字

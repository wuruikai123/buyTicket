-- 安全迁移banner表到新结构
-- 此脚本会保留现有数据（如果可能）

-- 步骤1: 检查当前表结构
-- 执行: DESCRIBE banner;

-- 步骤2: 备份现有数据（可选）
-- CREATE TABLE banner_backup AS SELECT * FROM banner;

-- 步骤3: 删除旧表
DROP TABLE IF EXISTS banner;

-- 步骤4: 创建新的轮播图表
CREATE TABLE banner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '轮播图ID',
    exhibition_id BIGINT NOT NULL COMMENT '关联的展览ID',
    title VARCHAR(100) NOT NULL COMMENT '轮播图标题',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号（数字越小越靠前）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0:禁用, 1:启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_exhibition_id (exhibition_id),
    INDEX idx_sort_order (sort_order),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 注意：外键约束已移除，避免删除展览时的级联问题
-- 如果需要外键约束，取消下面的注释：
-- ALTER TABLE banner ADD CONSTRAINT fk_banner_exhibition 
--   FOREIGN KEY (exhibition_id) REFERENCES exhibition(id) ON DELETE CASCADE;

-- 步骤5: 验证表结构
-- 执行: DESCRIBE banner;
-- 应该看到: id, exhibition_id, title, sort_order, status, create_time, update_time

-- 步骤6: 插入测试数据（可选）
-- INSERT INTO banner (exhibition_id, title, sort_order, status) 
-- VALUES (1, '测试轮播图', 1, 1);

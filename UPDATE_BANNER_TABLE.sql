-- 重新设计轮播图表结构
-- 轮播图关联展览，通过展览ID、标题和排序号管理

-- 删除旧表（如果需要保留数据，请先备份）
DROP TABLE IF EXISTS banner;

-- 创建新的轮播图表
CREATE TABLE banner (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '轮播图ID',
    exhibition_id BIGINT NOT NULL COMMENT '关联的展览ID',
    title VARCHAR(100) NOT NULL COMMENT '轮播图标题',
    sort_order INT NOT NULL DEFAULT 0 COMMENT '排序号（数字越小越靠前）',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态（0:禁用, 1:启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (exhibition_id) REFERENCES exhibition(id) ON DELETE CASCADE,
    INDEX idx_sort_order (sort_order),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 插入示例数据（可选）
-- INSERT INTO banner (exhibition_id, title, sort_order, status) VALUES (1, '逐梦星辰：中国航天成就展', 1, 1);

-- 创建"关于展厅"配置表
CREATE TABLE IF NOT EXISTS about_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(10) NOT NULL COMMENT '标题（不超过10个字）',
    content TEXT NOT NULL COMMENT '正文内容',
    background_image TEXT COMMENT '背景图片URL',
    intro_image1 TEXT COMMENT '介绍图1 URL',
    intro_image2 TEXT COMMENT '介绍图2 URL',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关于展厅配置表';

-- 插入默认数据
INSERT INTO about_config (title, content, background_image, intro_image1, intro_image2) 
VALUES (
    '关于展厅',
    '这里是展厅介绍内容，可以在管理端进行编辑。展厅致力于为观众提供优质的艺术展览体验，汇聚国内外优秀艺术作品，打造文化交流的重要平台。',
    '',
    '',
    ''
);

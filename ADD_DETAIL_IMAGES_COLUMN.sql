-- 添加介绍插图字段到展览表
-- 用于存储展览详情页的介绍图片（JSON数组格式）

-- 添加字段（如果已存在会报错，可以忽略）
ALTER TABLE exhibition 
ADD COLUMN detail_images TEXT COMMENT '介绍插图(JSON数组)' AFTER cover_image;

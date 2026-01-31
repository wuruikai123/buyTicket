-- 修改 about_config 表的图片字段类型，支持Base64编码的大图片
-- 将 VARCHAR 改为 LONGTEXT 以支持Base64编码

ALTER TABLE about_config 
MODIFY COLUMN background_image LONGTEXT COMMENT '背景图片(Base64或URL)',
MODIFY COLUMN intro_image1 LONGTEXT COMMENT '介绍图1(Base64或URL)',
MODIFY COLUMN intro_image2 LONGTEXT COMMENT '介绍图2(Base64或URL)';

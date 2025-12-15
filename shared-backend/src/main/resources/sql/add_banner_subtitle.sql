-- 为 banner 表添加 subtitle 字段

ALTER TABLE banner ADD COLUMN subtitle VARCHAR(50) COMMENT '次标题' AFTER title;

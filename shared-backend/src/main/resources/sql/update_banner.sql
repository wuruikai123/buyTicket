-- 更新轮播图数据，使用展览的封面图片

-- 清空现有轮播图数据
DELETE FROM banner;

-- 插入新的轮播图数据（使用展览的封面图片）
INSERT INTO banner (title, image_url, link_type, link_id, sort_order, status) VALUES
('2025年当代艺术双年展', '/images/exhibition_current.jpg', 1, 1, 1, 1),
('印象派大师作品展', '/images/exhibition_1.jpg', 1, 2, 2, 1),
('未来科技互动展', '/images/exhibition_2.jpg', 1, 3, 3, 1);

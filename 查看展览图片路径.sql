-- 查看展览表中的图片路径
SELECT 
    id,
    name,
    cover_image,
    detail_images
FROM exhibition
ORDER BY id DESC
LIMIT 10;

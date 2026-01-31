-- 请在MySQL中执行这个查询，查看展览的封面图路径
SELECT 
    id,
    name,
    cover_image,
    detail_images
FROM exhibition
ORDER BY id DESC
LIMIT 5;

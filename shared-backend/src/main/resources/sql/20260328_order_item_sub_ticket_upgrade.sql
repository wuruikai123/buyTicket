-- 子票模式升级（每张票独立购票人、状态、退款流转）
-- 执行前请先备份数据库

ALTER TABLE `order_item`
  ADD COLUMN `buyer_name` varchar(64) NULL COMMENT '购票人姓名' AFTER `price`,
  ADD COLUMN `buyer_id_card` varchar(32) NULL COMMENT '购票人证件号' AFTER `buyer_name`,
  ADD COLUMN `ticket_status` int NULL DEFAULT 1 COMMENT '子票状态(1待使用,2已使用,5退款中,6已退款)' AFTER `buyer_id_card`,
  ADD COLUMN `refund_request_time` datetime NULL COMMENT '子票退款申请时间' AFTER `ticket_status`,
  ADD COLUMN `refund_time` datetime NULL COMMENT '子票退款完成时间' AFTER `refund_request_time`;

-- 历史数据回填（旧数据无子票状态时，按主订单状态回填）
UPDATE `order_item` oi
JOIN `ticket_order` o ON o.id = oi.order_id
SET oi.ticket_status = CASE
  WHEN o.status = 2 THEN 2
  WHEN o.status = 5 THEN 5
  WHEN o.status = 6 THEN 6
  ELSE 1
END
WHERE oi.ticket_status IS NULL;

-- 兜底回填购票人信息
UPDATE `order_item` oi
JOIN `ticket_order` o ON o.id = oi.order_id
SET oi.buyer_name = COALESCE(oi.buyer_name, o.contact_name),
    oi.buyer_id_card = COALESCE(oi.buyer_id_card, o.contact_phone)
WHERE oi.buyer_name IS NULL OR oi.buyer_id_card IS NULL;

-- 索引
CREATE INDEX `idx_order_item_order_ticket_status` ON `order_item` (`order_id`, `ticket_status`);
CREATE INDEX `idx_order_item_ticket_date_status` ON `order_item` (`ticket_date`, `ticket_status`);

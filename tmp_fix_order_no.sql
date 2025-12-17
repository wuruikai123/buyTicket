USE buy_ticket;

SET @col := (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ticket_order' AND COLUMN_NAME = 'order_no');
SET @s := IF(@col = 0, 'ALTER TABLE ticket_order ADD COLUMN order_no VARCHAR(32) NOT NULL DEFAULT '''' COMMENT ''订单号（唯一）'' AFTER id;', 'SELECT 1;');
PREPARE stmt FROM @s; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE ticket_order
SET order_no = CONCAT(''T'', DATE_FORMAT(create_time, ''%Y%m%d''), LPAD(id, 8, ''0''))
WHERE order_no IS NULL OR order_no = '' OR order_no = ''0'';

SET @idx := (SELECT COUNT(*) FROM information_schema.statistics WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ticket_order' AND INDEX_NAME = 'uk_order_no');
SET @s2 := IF(@idx = 0, 'ALTER TABLE ticket_order ADD UNIQUE INDEX uk_order_no (order_no);', 'SELECT 1;');
PREPARE stmt2 FROM @s2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;

SELECT id, order_no, user_id, create_time
FROM ticket_order
ORDER BY id DESC
LIMIT 3;

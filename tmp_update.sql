UPDATE ticket_order SET order_no = CONCAT('T', DATE_FORMAT(create_time, '%Y%m%d'), LPAD(id, 8, '0')) WHERE order_no IS NULL OR order_no = '' OR order_no = '0';
ALTER TABLE ticket_order ADD UNIQUE INDEX uk_order_no (order_no);
SELECT id, order_no FROM ticket_order ORDER BY id DESC LIMIT 3;

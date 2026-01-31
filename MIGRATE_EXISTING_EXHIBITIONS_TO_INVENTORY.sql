-- 为现有展览初始化库存数据
-- 注意：此脚本会为所有现有展览生成库存记录

-- 1. 首先确保库存表已创建
-- 如果还没创建，请先执行 CREATE_TIME_SLOT_INVENTORY_TABLE.sql

-- 2. 为现有展览生成库存记录
-- 此脚本使用存储过程来生成库存数据

DELIMITER $$

DROP PROCEDURE IF EXISTS init_exhibition_inventory$$

CREATE PROCEDURE init_exhibition_inventory()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_exhibition_id BIGINT;
    DECLARE v_start_date DATE;
    DECLARE v_end_date DATE;
    DECLARE v_morning_tickets INT;
    DECLARE v_afternoon_tickets INT;
    DECLARE v_current_date DATE;
    
    -- 声明游标
    DECLARE exhibition_cursor CURSOR FOR 
        SELECT id, start_date, end_date, morning_tickets, afternoon_tickets 
        FROM exhibition 
        WHERE start_date IS NOT NULL 
          AND end_date IS NOT NULL;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 打开游标
    OPEN exhibition_cursor;
    
    read_loop: LOOP
        FETCH exhibition_cursor INTO v_exhibition_id, v_start_date, v_end_date, v_morning_tickets, v_afternoon_tickets;
        
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 为每个展览的每一天生成库存记录
        SET v_current_date = v_start_date;
        
        WHILE v_current_date <= v_end_date DO
            -- 上午场 09:00-12:00
            IF v_morning_tickets IS NOT NULL AND v_morning_tickets > 0 THEN
                INSERT IGNORE INTO exhibition_time_slot_inventory 
                    (exhibition_id, ticket_date, time_slot, total_tickets, sold_tickets, available_tickets, version)
                VALUES 
                    (v_exhibition_id, v_current_date, '09:00-12:00', v_morning_tickets, 0, v_morning_tickets, 0);
            END IF;
            
            -- 下午场 14:00-17:00
            IF v_afternoon_tickets IS NOT NULL AND v_afternoon_tickets > 0 THEN
                INSERT IGNORE INTO exhibition_time_slot_inventory 
                    (exhibition_id, ticket_date, time_slot, total_tickets, sold_tickets, available_tickets, version)
                VALUES 
                    (v_exhibition_id, v_current_date, '14:00-17:00', v_afternoon_tickets, 0, v_afternoon_tickets, 0);
            END IF;
            
            -- 下一天
            SET v_current_date = DATE_ADD(v_current_date, INTERVAL 1 DAY);
        END WHILE;
        
    END LOOP;
    
    CLOSE exhibition_cursor;
    
    SELECT '库存初始化完成' AS message;
END$$

DELIMITER ;

-- 3. 执行存储过程
CALL init_exhibition_inventory();

-- 4. 验证结果
SELECT 
    e.id AS exhibition_id,
    e.name AS exhibition_name,
    COUNT(i.id) AS inventory_records
FROM exhibition e
LEFT JOIN exhibition_time_slot_inventory i ON e.id = i.exhibition_id
GROUP BY e.id, e.name
ORDER BY e.id;

-- 5. 查看某个展览的库存详情（修改exhibition_id）
-- SELECT * FROM exhibition_time_slot_inventory WHERE exhibition_id = 1 ORDER BY ticket_date, time_slot;

-- 6. 清理存储过程（可选）
-- DROP PROCEDURE IF EXISTS init_exhibition_inventory;

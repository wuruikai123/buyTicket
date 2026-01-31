-- 创建展览时间段库存表
CREATE TABLE exhibition_time_slot_inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    exhibition_id BIGINT NOT NULL COMMENT '展览ID',
    ticket_date DATE NOT NULL COMMENT '门票日期',
    time_slot VARCHAR(50) NOT NULL COMMENT '时间段，如：09:00-12:00',
    total_tickets INT NOT NULL DEFAULT 0 COMMENT '总票数',
    sold_tickets INT NOT NULL DEFAULT 0 COMMENT '已售票数',
    available_tickets INT NOT NULL DEFAULT 0 COMMENT '可售票数',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_exhibition_date_slot (exhibition_id, ticket_date, time_slot),
    INDEX idx_exhibition_id (exhibition_id),
    INDEX idx_ticket_date (ticket_date)
) COMMENT='展览时间段库存表';

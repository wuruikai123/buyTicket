-- 创建特殊票券表
CREATE TABLE IF NOT EXISTS special_ticket (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  ticket_code VARCHAR(50) UNIQUE NOT NULL COMMENT '票券编号（唯一）',
  qr_code_url VARCHAR(500) COMMENT '二维码图片URL',
  status TINYINT DEFAULT 0 COMMENT '状态：0=未使用，1=已使用',
  verify_time DATETIME COMMENT '核销时间',
  verify_admin_id BIGINT COMMENT '核销管理员ID',
  verify_admin_name VARCHAR(100) COMMENT '核销管理员姓名',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  remark VARCHAR(500) COMMENT '备注',
  INDEX idx_ticket_code (ticket_code),
  INDEX idx_status (status),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='特殊票券表';

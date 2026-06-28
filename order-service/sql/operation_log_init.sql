CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `action` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '操作名称',
  `operator_id` BIGINT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(50) NULL COMMENT '操作人用户名',
  `operator_role` VARCHAR(20) NULL COMMENT '操作人角色',
  `method_name` VARCHAR(150) NOT NULL DEFAULT '' COMMENT '方法名',
  `params` VARCHAR(500) NULL COMMENT '参数摘要',
  `result` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '执行结果',
  `error_message` VARCHAR(255) NULL COMMENT '错误信息',
  `duration_ms` BIGINT NOT NULL DEFAULT 0 COMMENT '耗时毫秒',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE DATABASE IF NOT EXISTS `trade_message_db` DEFAULT CHARACTER SET utf8mb4;

USE `trade_message_db`;

CREATE TABLE IF NOT EXISTS `private_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `from_user_id` BIGINT NOT NULL COMMENT '发送方用户ID',
  `from_username` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '发送方用户名',
  `from_nickname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '发送方昵称',
  `to_user_id` BIGINT NOT NULL COMMENT '接收方用户ID',
  `to_username` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '接收方用户名',
  `to_nickname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '接收方昵称',
  `content` VARCHAR(500) NOT NULL COMMENT '消息内容',
  `read_status` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读 1已读',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_from_user_id` (`from_user_id`),
  KEY `idx_to_user_id` (`to_user_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户私信表';

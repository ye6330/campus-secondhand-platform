CREATE TABLE IF NOT EXISTS `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` VARCHAR(50) NOT NULL COMMENT '商品标题',
  `description` VARCHAR(500) NOT NULL COMMENT '商品描述',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
  `cover_image` VARCHAR(255) NOT NULL COMMENT '商品封面图',
  `seller_id` BIGINT NOT NULL COMMENT '卖家用户ID',
  `seller_name` VARCHAR(50) NOT NULL COMMENT '卖家用户名',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1在售 0下架',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

ALTER TABLE `product`
ADD COLUMN IF NOT EXISTS `cover_image` VARCHAR(255) NOT NULL DEFAULT '' COMMENT '商品封面图' AFTER `price`;

INSERT INTO `product` (`title`, `description`, `price`, `cover_image`, `seller_id`, `seller_name`, `status`)
SELECT '二手机械键盘', '青轴，宿舍自用，成色较新。', 129.00, 'https://via.placeholder.com/600x400.png?text=Keyboard', 1, '校园卖家A', 1
WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `title` = '二手机械键盘');

INSERT INTO `product` (`title`, `description`, `price`, `cover_image`, `seller_id`, `seller_name`, `status`)
SELECT '考研数学资料', '包含真题和笔记，适合基础阶段复习。', 35.00, 'https://via.placeholder.com/600x400.png?text=Books', 2, '校园卖家B', 1
WHERE NOT EXISTS (SELECT 1 FROM `product` WHERE `title` = '考研数学资料');

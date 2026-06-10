ALTER TABLE `orders`
ADD COLUMN IF NOT EXISTS `buyer_phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '买家手机号' AFTER `buyer_name`,
ADD COLUMN IF NOT EXISTS `seller_phone` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '卖家手机号' AFTER `seller_name`,
ADD COLUMN IF NOT EXISTS `trade_image` VARCHAR(255) NULL COMMENT '交易现场图' AFTER `note`,
ADD COLUMN IF NOT EXISTS `trade_confirmed_at` DATETIME NULL COMMENT '成交确认时间' AFTER `trade_image`;

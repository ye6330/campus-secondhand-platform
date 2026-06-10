UPDATE `user`
SET `phone` = CONCAT('13', LPAD(MOD(`id`, 1000000000), 9, '0'))
WHERE `phone` IS NULL OR TRIM(`phone`) = '';

ALTER TABLE `user`
MODIFY COLUMN `phone` VARCHAR(20) NOT NULL COMMENT '手机号';

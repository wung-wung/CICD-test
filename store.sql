-- --------------------------------------------------------
-- 1. 创建数据库
-- --------------------------------------------------------
CREATE DATABASE IF NOT EXISTS `store` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `store`;

-- --------------------------------------------------------
-- 2. 创建商品表
-- --------------------------------------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '商品主键ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `price` DECIMAL(10,2) NOT NULL DEFAULT '0.00' COMMENT '商品价格',
  `stock` INT(11) NOT NULL DEFAULT '0' COMMENT '剩余库存量 (超卖测试核心字段)',
  
  -- 以下为架构优化预留字段
  `version` INT(11) NOT NULL DEFAULT '0' COMMENT '乐观锁版本号 (用于第四课时代码重构)',
  
  -- 审计字段
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_product_name` (`product_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品库存表';

-- --------------------------------------------------------
-- 3. 插入测试数据
-- --------------------------------------------------------
BEGIN;
-- 插入一个爆款商品，库存仅设为 100，用于诱发超卖 Bug
INSERT INTO `product` (`id`, `product_name`, `price`, `stock`, `version`) 
VALUES (1, '【双十一特供】限量版机械键盘', 299.00, 100, 0);

-- 插入一个对照组商品，库存充足
INSERT INTO `product` (`id`, `product_name`, `price`, `stock`, `version`) 
VALUES (2, '普通办公鼠标', 49.90, 10000, 0);
COMMIT;
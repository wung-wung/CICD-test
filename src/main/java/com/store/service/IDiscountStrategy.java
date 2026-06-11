package com.store.service;

import java.math.BigDecimal;

/**
 * 折扣策略接口
 * 定义大促折扣计算的统一规范
 */
public interface IDiscountStrategy {
    
    /**
     * 计算折扣后的价格
     * 
     * @param originalPrice 原始价格
     * @param context 折扣计算上下文参数（如商品类型、会员等级、是否新品等）
     * @return 折扣后的价格
     */
    BigDecimal calculateDiscount(BigDecimal originalPrice, DiscountContext context);
}
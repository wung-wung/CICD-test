package com.store.service.impl;

import com.store.service.DiscountContext;
import com.store.service.IDiscountStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 服装折扣策略
 * type = 2
 */
@Service
public class ClothingDiscountStrategy implements IDiscountStrategy {

    @Override
    public BigDecimal calculateDiscount(BigDecimal originalPrice, DiscountContext context) {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        double price = originalPrice.doubleValue();
        boolean isNew = context.getIsNewProduct() != null && context.getIsNewProduct();
        int vLevel = context.getMemberLevel() != null ? context.getMemberLevel() : 0;
        boolean isHol = context.getIsHolidayPromotion() != null && context.getIsHolidayPromotion();
        
        double fPrice = price;
        
        // 服装逻辑：节假日促销或非节假日会员折扣
        if (isHol) {
            if (price >= 200) {
                fPrice = price - 40;
            } else {
                fPrice = price * 0.8;
            }
        } else {
            if (vLevel >= 2) { fPrice = price * 0.88; }
        }
        
        // 新品且会员等级>=1，额外95折
        if (isNew && vLevel >= 1) {
            fPrice = fPrice * 0.95;
        }
        
        return BigDecimal.valueOf(Math.max(fPrice, 0)).setScale(2, RoundingMode.HALF_UP);
    }
}

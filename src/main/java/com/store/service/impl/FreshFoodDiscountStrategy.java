package com.store.service.impl;

import com.store.service.DiscountContext;
import com.store.service.IDiscountStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 生鲜折扣策略
 * type = 3
 */
@Service
public class FreshFoodDiscountStrategy implements IDiscountStrategy {

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
        
        // 生鲜逻辑：当日采摘、节假日促销或会员折扣（互斥）
        if (isNew) {
            fPrice = price * 0.7;
            if (fPrice > 100) { fPrice = fPrice - 5; }
        } else if (isHol) {
            if (price >= 50) {
                fPrice = price - 8;
            }
        } else {
            if (vLevel == 1) { fPrice = price * 0.98; }
            else if (vLevel == 2) { fPrice = price * 0.97; }
            else if (vLevel >= 3) { fPrice = price * 0.95; }
        }
        
        return BigDecimal.valueOf(Math.max(fPrice, 0)).setScale(2, RoundingMode.HALF_UP);
    }
}

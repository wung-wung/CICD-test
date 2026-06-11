package com.store.service.impl;

import com.store.service.DiscountContext;
import com.store.service.IDiscountStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 图书折扣策略
 * type = 4
 */
@Service
public class BookDiscountStrategy implements IDiscountStrategy {

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
        
        // 图书逻辑：非新书才有折扣
        if (!isNew) {
            if (isHol) {
                if (price >= 100) {
                    fPrice = price - 15;
                } else {
                    fPrice = price * 0.9;
                }
                if (vLevel >= 2) {
                    fPrice = fPrice * 0.98;
                }
            } else {
                if (vLevel >= 2) { fPrice = price * 0.95; }
                else if (vLevel == 1) { fPrice = price * 0.98; }
            }
        }
        
        // 节假日且价格>=200，再减20
        if (isHol && fPrice >= 200) {
            fPrice = fPrice - 20;
        }
        
        return BigDecimal.valueOf(Math.max(fPrice, 0)).setScale(2, RoundingMode.HALF_UP);
    }
}

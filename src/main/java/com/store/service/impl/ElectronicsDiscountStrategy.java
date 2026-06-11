package com.store.service.impl;

import com.store.service.DiscountContext;
import com.store.service.IDiscountStrategy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 电子产品折扣策略
 * type = 1
 */
@Service
public class ElectronicsDiscountStrategy implements IDiscountStrategy {

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
        
        // 电子产品逻辑：新用户减50，否则按会员等级打折
        if (isNew) {
            fPrice = price - 50;
            if (fPrice < 0) fPrice = 0;
        } else {
            if (vLevel > 0) {
                if (vLevel == 1) { fPrice = price * 0.95; }
                else if (vLevel == 2) { fPrice = price * 0.90; }
                else if (vLevel >= 3) { fPrice = price * 0.85; }
            }
        }
        
        // 节假日且价格>500，再减30
        if (isHol && fPrice > 500) {
            fPrice = fPrice - 30;
        }
        
        return BigDecimal.valueOf(Math.max(fPrice, 0)).setScale(2, RoundingMode.HALF_UP);
    }
}

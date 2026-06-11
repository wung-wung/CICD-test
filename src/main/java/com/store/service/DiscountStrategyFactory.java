package com.store.service;

import com.store.service.impl.BookDiscountStrategy;
import com.store.service.impl.ClothingDiscountStrategy;
import com.store.service.impl.ElectronicsDiscountStrategy;
import com.store.service.impl.FreshFoodDiscountStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 折扣策略工厂
 * 根据商品类型、是否新用户、是否节假日、会员等级等条件返回对应的策略实现
 */
@Slf4j
@Component
public class DiscountStrategyFactory {

    private final Map<Integer, IDiscountStrategy> strategyMap = new HashMap<>();

    public DiscountStrategyFactory(ElectronicsDiscountStrategy electronicsStrategy,
                                   ClothingDiscountStrategy clothingStrategy,
                                   FreshFoodDiscountStrategy freshFoodStrategy,
                                   BookDiscountStrategy bookStrategy) {
        // 注册各商品类型的折扣策略
        strategyMap.put(1, electronicsStrategy);   // 电子产品
        strategyMap.put(2, clothingStrategy);      // 服装
        strategyMap.put(3, freshFoodStrategy);     // 生鲜
        strategyMap.put(4, bookStrategy);          // 图书
        
        log.info("折扣策略工厂初始化完成，已注册 {} 个策略", strategyMap.size());
    }

    /**
     * 根据商品类型获取对应的折扣策略
     * 
     * @param productType 商品类型 (1:电子产品, 2:服装, 3:生鲜, 4:图书)
     * @return 对应的折扣策略，如果不存在则返回 null
     */
    public IDiscountStrategy getStrategy(Integer productType) {
        IDiscountStrategy strategy = strategyMap.get(productType);
        if (strategy == null) {
            log.warn("未找到商品类型 {} 对应的折扣策略", productType);
        }
        return strategy;
    }

    /**
     * 根据完整上下文条件获取并执行折扣计算
     * 该方法会根据商品类型自动选择对应策略，并应用通用规则
     * 
     * @param originalPrice 原始价格
     * @param context 折扣上下文（包含商品类型、是否新品、会员等级、是否节假日等）
     * @return 最终折扣价格
     */
    public BigDecimal calculateWithCommonRules(BigDecimal originalPrice, DiscountContext context) {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            log.debug("原始价格无效: {}", originalPrice);
            return BigDecimal.ZERO;
        }

        Integer productType = context.getProductType();
        if (productType == null) {
            log.warn("商品类型为空，返回原始价格");
            return originalPrice.setScale(2, java.math.RoundingMode.HALF_UP);
        }

        // 记录折扣计算的关键条件
        log.debug("开始计算折扣 - 商品类型: {}, 是否新品: {}, 会员等级: {}, 是否节假日: {}",
                productType,
                context.getIsNewProduct(),
                context.getMemberLevel(),
                context.getIsHolidayPromotion());

        // 1. 根据商品类型获取对应策略
        IDiscountStrategy strategy = getStrategy(productType);
        if (strategy == null) {
            log.warn("未找到商品类型 {} 的策略，返回原始价格", productType);
            return originalPrice.setScale(2, java.math.RoundingMode.HALF_UP);
        }

        // 2. 执行特定商品类型的折扣计算
        BigDecimal priceAfterTypeDiscount = strategy.calculateDiscount(originalPrice, context);
        log.debug("商品类型 {} 折扣后价格: {}", productType, priceAfterTypeDiscount);

        // 3. 应用通用规则
        double fPrice = priceAfterTypeDiscount.doubleValue();
        boolean isHol = context.getIsHolidayPromotion() != null && context.getIsHolidayPromotion();
        int vLevel = context.getMemberLevel() != null ? context.getMemberLevel() : 0;

        // 通用规则1：节假日且价格>=300，非生鲜类再打9折
        if (isHol && fPrice >= 300) {
            if (productType != 3) {
                double beforeDiscount = fPrice;
                fPrice = fPrice * 0.9;
                log.debug("应用通用规则1（节假日满300打9折）: {} -> {}", beforeDiscount, fPrice);
            }
        }

        // 通用规则2：会员等级>=3 且 节假日，再减10
        if (vLevel >= 3 && isHol) {
            double beforeDiscount = fPrice;
            fPrice = fPrice - 10;
            log.debug("应用通用规则2（高等级会员节假日减10）: {} -> {}", beforeDiscount, fPrice);
        }

        BigDecimal finalPrice = BigDecimal.valueOf(Math.max(fPrice, 0)).setScale(2, java.math.RoundingMode.HALF_UP);
        log.debug("最终折扣价格: {}", finalPrice);
        
        return finalPrice;
    }

    /**
     * 获取所有已注册的策略
     * 
     * @return 策略映射表
     */
    public Map<Integer, IDiscountStrategy> getAllStrategies() {
        return new HashMap<>(strategyMap);
    }

    /**
     * 检查是否存在指定商品类型的策略
     * 
     * @param productType 商品类型
     * @return 是否存在
     */
    public boolean hasStrategy(Integer productType) {
        return strategyMap.containsKey(productType);
    }
}

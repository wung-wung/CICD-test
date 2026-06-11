package com.store.service;

import java.math.BigDecimal;

/**
 * 折扣策略工厂使用示例
 * 
 * 该工厂类根据商品类型、是否新用户、是否节假日、会员等级等条件
 * 自动选择并执行对应的折扣策略
 */
public class DiscountStrategyFactoryExample {

    /**
     * 使用示例1：基本用法
     */
    public void example1_BasicUsage(DiscountStrategyFactory factory) {
        // 构建折扣上下文
        DiscountContext context = DiscountContext.builder()
                .productType(1)              // 电子产品
                .isNewProduct(true)          // 新用户
                .memberLevel(0)              // 非会员
                .isHolidayPromotion(false)   // 非节假日
                .build();
        
        BigDecimal originalPrice = new BigDecimal("1000");
        
        // 计算折扣价格
        BigDecimal finalPrice = factory.calculateWithCommonRules(originalPrice, context);
        
        System.out.println("原始价格: " + originalPrice);
        System.out.println("折扣后价格: " + finalPrice);
    }

    /**
     * 使用示例2：服装 - 节假日促销 - 高等级会员
     */
    public void example2_ClothingHolidayVIP(DiscountStrategyFactory factory) {
        DiscountContext context = DiscountContext.builder()
                .productType(2)              // 服装
                .isNewProduct(false)         // 非新品
                .memberLevel(3)              // 钻石会员
                .isHolidayPromotion(true)    // 节假日
                .build();
        
        BigDecimal originalPrice = new BigDecimal("500");
        BigDecimal finalPrice = factory.calculateWithCommonRules(originalPrice, context);
        
        System.out.println("服装原价: " + originalPrice);
        System.out.println("最终价格: " + finalPrice);
    }

    /**
     * 使用示例3：生鲜 - 当日采摘
     */
    public void example3_FreshFoodNewArrival(DiscountStrategyFactory factory) {
        DiscountContext context = DiscountContext.builder()
                .productType(3)              // 生鲜
                .isNewProduct(true)          // 当日采摘
                .memberLevel(1)              // 普通会员
                .isHolidayPromotion(false)   // 非节假日
                .build();
        
        BigDecimal originalPrice = new BigDecimal("150");
        BigDecimal finalPrice = factory.calculateWithCommonRules(originalPrice, context);
        
        System.out.println("生鲜原价: " + originalPrice);
        System.out.println("最终价格: " + finalPrice);
    }

    /**
     * 使用示例4：图书 - 新书 - 节假日
     */
    public void example4_BookNewReleaseHoliday(DiscountStrategyFactory factory) {
        DiscountContext context = DiscountContext.builder()
                .productType(4)              // 图书
                .isNewProduct(true)          // 新书（无折扣）
                .memberLevel(2)              // 黄金会员
                .isHolidayPromotion(true)    // 节假日
                .build();
        
        BigDecimal originalPrice = new BigDecimal("80");
        BigDecimal finalPrice = factory.calculateWithCommonRules(originalPrice, context);
        
        System.out.println("图书原价: " + originalPrice);
        System.out.println("最终价格: " + finalPrice);
    }

    /**
     * 使用示例5：检查策略是否存在
     */
    public void example5_CheckStrategy(DiscountStrategyFactory factory) {
        // 检查是否有电子产品的折扣策略
        if (factory.hasStrategy(1)) {
            System.out.println("电子产品折扣策略已注册");
        }
        
        // 获取所有已注册的策略
        System.out.println("已注册策略数量: " + factory.getAllStrategies().size());
    }

    /**
     * 使用示例6：直接获取策略（高级用法）
     */
    public void example6_GetStrategyDirectly(DiscountStrategyFactory factory) {
        // 直接获取电子产品策略
        IDiscountStrategy strategy = factory.getStrategy(1);
        
        if (strategy != null) {
            DiscountContext context = DiscountContext.builder()
                    .productType(1)
                    .isNewProduct(false)
                    .memberLevel(2)
                    .isHolidayPromotion(false)
                    .build();
            
            BigDecimal price = new BigDecimal("2000");
            BigDecimal discountedPrice = strategy.calculateDiscount(price, context);
            
            System.out.println("直接使用策略计算: " + discountedPrice);
        }
    }
}

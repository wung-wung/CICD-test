package com.store.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 折扣计算上下文
 * 封装折扣计算所需的各种参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountContext {
    
    /**
     * 商品类型 (1:电子产品, 2:服装, 3:生鲜, 4:图书等)
     */
    private Integer productType;
    
    /**
     * 会员等级 (0:非会员, 1:普通会员, 2:黄金会员, 3:钻石会员等)
     */
    private Integer memberLevel;
    
    /**
     * 是否新品
     */
    private Boolean isNewProduct;
    
    /**
     * 是否节假日促销
     */
    private Boolean isHolidayPromotion;
    
    /**
     * 其他扩展参数（可根据业务需求添加）
     */
    private Object extraParams;
}
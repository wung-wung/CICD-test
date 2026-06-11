package com.store.controller;
import com.store.service.DiscountContext;
import com.store.service.DiscountStrategyFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Tag(name = "大促折扣计算")
@RestController
@RequestMapping("/discount")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountStrategyFactory discountStrategyFactory;

    /**
     * type: 1=电子产品  2=服装  3=生鲜  4=图书
     * isNew: 电子产品=新用户  生鲜=当日采摘  图书=新书
     * vLevel: 会员等级 0/1/2/3+
     * isHol: 是否节假日
     */
    @Operation(summary = "计算大促最终成交价")
    @GetMapping("/calculate")
    public ResponseEntity<?> calculate(
            @RequestParam double price,
            @RequestParam int type,
            @RequestParam(defaultValue = "false") boolean isNew,
            @RequestParam(defaultValue = "0") int vLevel,
            @RequestParam(defaultValue = "false") boolean isHol) {
        
        // 构建折扣上下文
        DiscountContext context = DiscountContext.builder()
                .productType(type)
                .isNewProduct(isNew)
                .memberLevel(vLevel)
                .isHolidayPromotion(isHol)
                .build();
        
        // 使用策略工厂计算折扣价格
        BigDecimal finalPrice = discountStrategyFactory.calculateWithCommonRules(
                BigDecimal.valueOf(price), context);
        
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("originalPrice", price);
        result.put("finalPrice", finalPrice.doubleValue());
        result.put("discount", price > 0
                ? Math.round((1 - finalPrice.doubleValue() / price) * 10000d) / 100d : 0);
        return ResponseEntity.ok(result);
    }
}

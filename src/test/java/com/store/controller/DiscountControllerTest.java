package com.store.controller;

import com.store.service.DiscountContext;
import com.store.service.DiscountStrategyFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * DiscountController 单元测试类
 * 
 * 测试目标：验证大促折扣计算接口的正确性
 * 测试策略：使用 Mock 技术隔离外部依赖，专注于 Controller 层逻辑验证
 * 遵循原则：3A原则（Arrange-Act-Assert）
 * 
 * @author Senior Test Engineer
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DiscountController 单元测试")
class DiscountControllerTest {

    // ==================== 测试桩配置 ====================
    
    /**
     * Mock 对象：折扣策略工厂
     * 使用 @Mock 注解创建模拟对象，避免真实调用策略逻辑
     */
    @Mock
    private DiscountStrategyFactory discountStrategyFactory;

    /**
     * 被测试对象：折扣控制器
     * 使用 @InjectMocks 自动注入上面的 Mock 对象
     */
    @InjectMocks
    private DiscountController discountController;

    /**
     * 测试前置条件设置
     * 每个测试方法执行前都会运行此方法
     */
    @BeforeEach
    void setUp() {
        // 可以在这里添加通用的测试准备工作
        // 例如：重置 Mock 对象状态、准备测试数据等
    }

    // ==================== 正向路径测试用例 ====================

    /**
     * 测试用例 1：电子产品 - 新用户优惠场景
     * 
     * 测试目的：验证电子产品新用户的折扣计算逻辑
     * 业务规则：新用户立减50元
     * 
     * 3A 分析：
     * - Arrange: 准备原价1000元的电子产品，标记为新用户
     * - Act: 调用折扣计算接口
     * - Assert: 验返回结果为950元，折扣率5%
     */
    @Test
    @DisplayName("正向测试 - 电子产品新用户应享受立减优惠")
    void testCalculate_ElectronicsNewUser_ShouldApplyFixedDiscount() {
        // Arrange - 准备测试数据
        double originalPrice = 1000.0;
        int productType = 1; // 电子产品
        boolean isNew = true; // 新用户
        int memberLevel = 0; // 非会员
        boolean isHoliday = false; // 非节假日
        
        // 设定测试桩：当工厂被调用时，返回预期的折扣后价格
        BigDecimal expectedFinalPrice = new BigDecimal("950.00");
        when(discountStrategyFactory.calculateWithCommonRules(
                eq(BigDecimal.valueOf(originalPrice)),
                any(DiscountContext.class)))
            .thenReturn(expectedFinalPrice);

        // Act - 执行被测方法
        ResponseEntity<?> response = discountController.calculate(
                originalPrice, productType, isNew, memberLevel, isHoliday);

        // Assert - 验证结果
        assertNotNull(response, "响应不应为null");
        assertEquals(200, response.getStatusCodeValue(), "HTTP状态码应为200");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertNotNull(result, "响应体不应为null");
        
        assertEquals(originalPrice, (Double) result.get("originalPrice"), "原始价格应保持不变");
        assertEquals(expectedFinalPrice.doubleValue(), (Double) result.get("finalPrice"),
                0.01, "折扣后价格应与预期一致");
        
        // 验证折扣率计算：(1000 - 950) / 1000 * 100 = 5%
        double expectedDiscount = 5.0;
        assertEquals(expectedDiscount, (Double) result.get("discount"),
                0.01, "折扣率应为5%");
        
        // 验证工厂方法被调用且仅调用一次
        verify(discountStrategyFactory, times(1))
                .calculateWithCommonRules(any(BigDecimal.class), any(DiscountContext.class));
    }

    /**
     * 测试用例 2：服装 - 节假日高等级会员叠加优惠
     * 
     * 测试目的：验证多重优惠条件叠加的计算逻辑
     * 业务规则：节假日促销 + 钻石会员 + 通用规则叠加
     * 
     * 3A 分析：
     * - Arrange: 准备500元服装，钻石会员，节假日
     * - Act: 调用折扣计算接口
     * - Assert: 验证返回价格和折扣率符合预期
     */
    @Test
    @DisplayName("正向测试 - 服装节假日钻石会员应享受叠加优惠")
    void testCalculate_ClothingHolidayVIP_ShouldApplyStackedDiscounts() {
        // Arrange - 准备测试数据
        double originalPrice = 500.0;
        int productType = 2; // 服装
        boolean isNew = false; // 非新品
        int memberLevel = 3; // 钻石会员
        boolean isHoliday = true; // 节假日
        
        // 设定测试桩：模拟复杂优惠后的价格（假设经过多层折扣后为380元）
        BigDecimal expectedFinalPrice = new BigDecimal("380.00");
        when(discountStrategyFactory.calculateWithCommonRules(
                eq(BigDecimal.valueOf(originalPrice)),
                any(DiscountContext.class)))
            .thenReturn(expectedFinalPrice);

        // Act - 执行被测方法
        ResponseEntity<?> response = discountController.calculate(
                originalPrice, productType, isNew, memberLevel, isHoliday);

        // Assert - 验证结果
        assertEquals(200, response.getStatusCodeValue());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        
        assertEquals(expectedFinalPrice.doubleValue(), (Double) result.get("finalPrice"),
                0.01, "折扣后价格应与预期一致");
        
        // 验证折扣率：(500 - 380) / 500 * 100 = 24%
        double expectedDiscount = 24.0;
        assertEquals(expectedDiscount, (Double) result.get("discount"), 0.01,
                "折扣率应为24%，允许0.01的浮点误差");
    }

    /**
     * 测试用例 3：生鲜 - 当日采摘特价
     * 
     * 测试目的：验证生鲜商品的特殊折扣逻辑
     * 业务规则：当日采摘享受7折优惠
     * 
     * 3A 分析：
     * - Arrange: 准备150元生鲜，标记为当日采摘
     * - Act: 调用折扣计算接口
     * - Assert: 验证返回价格为105元（7折）
     */
    @Test
    @DisplayName("正向测试 - 生鲜当日采摘应享受7折优惠")
    void testCalculate_FreshFoodDailyPick_ShouldApplyPercentageDiscount() {
        // Arrange - 准备测试数据
        double originalPrice = 150.0;
        int productType = 3; // 生鲜
        boolean isNew = true; // 当日采摘
        int memberLevel = 1; // 普通会员
        boolean isHoliday = false; // 非节假日
        
        // 设定测试桩：7折后价格为105元
        BigDecimal expectedFinalPrice = new BigDecimal("105.00");
        when(discountStrategyFactory.calculateWithCommonRules(
                eq(BigDecimal.valueOf(originalPrice)),
                any(DiscountContext.class)))
            .thenReturn(expectedFinalPrice);

        // Act - 执行被测方法
        ResponseEntity<?> response = discountController.calculate(
                originalPrice, productType, isNew, memberLevel, isHoliday);

        // Assert - 验证结果
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        
        assertEquals(expectedFinalPrice.doubleValue(), (Double) result.get("finalPrice"),
                0.01, "折扣后价格应与预期一致");
        
        // 验证折扣率：(150 - 105) / 150 * 100 = 30%
        double expectedDiscount = 30.0;
        assertEquals(expectedDiscount, (Double) result.get("discount"),
                0.01, "折扣率应为30%");
    }

    /**
     * 测试用例 4：图书 - 新书无折扣场景
     * 
     * 测试目的：验证新书不享受折扣的业务规则
     * 业务规则：新书不参与任何折扣活动
     * 
     * 3A 分析：
     * - Arrange: 准备80元新书
     * - Act: 调用折扣计算接口
     * - Assert: 验证返回价格仍为80元，折扣率为0
     */
    @Test
    @DisplayName("正向测试 - 图书新书不应享受任何折扣")
    void testCalculate_BookNewRelease_ShouldNotApplyDiscount() {
        // Arrange - 准备测试数据
        double originalPrice = 80.0;
        int productType = 4; // 图书
        boolean isNew = true; // 新书
        int memberLevel = 2; // 黄金会员
        boolean isHoliday = true; // 节假日
        
        // 设定测试桩：新书无折扣，返回原价
        BigDecimal expectedFinalPrice = new BigDecimal("80.00");
        when(discountStrategyFactory.calculateWithCommonRules(
                eq(BigDecimal.valueOf(originalPrice)),
                any(DiscountContext.class)))
            .thenReturn(expectedFinalPrice);

        // Act - 执行被测方法
        ResponseEntity<?> response = discountController.calculate(
                originalPrice, productType, isNew, memberLevel, isHoliday);

        // Assert - 验证结果
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        
        assertEquals(originalPrice, (Double) result.get("finalPrice"),
                0.01, "新书价格应保持不变");
        assertEquals(0.0, (Double) result.get("discount"),
                0.01, "新书折扣率应为0");
    }

    // ==================== 边缘条件测试用例 ====================

    /**
     * 测试用例 5：价格为零的边缘情况
     * 
     * 测试目的：验证价格为0时的处理逻辑
     * 业务规则：价格为0时，折扣率应为0，避免除零错误
     * 
     * 3A 分析：
     * - Arrange: 准备价格为0的商品
     * - Act: 调用折扣计算接口
     * - Assert: 验证不会抛出异常，折扣率为0
     */
    @Test
    @DisplayName("边缘测试 - 价格为零时应返回零折扣率避免除零错误")
    void testCalculate_ZeroPrice_ShouldReturnZeroDiscount() {
        // Arrange - 准备测试数据
        double originalPrice = 0.0;
        int productType = 1;
        boolean isNew = false;
        int memberLevel = 0;
        boolean isHoliday = false;
        
        // 设定测试桩：价格为0时返回0
        BigDecimal expectedFinalPrice = BigDecimal.ZERO;
        when(discountStrategyFactory.calculateWithCommonRules(
                eq(BigDecimal.valueOf(originalPrice)),
                any(DiscountContext.class)))
            .thenReturn(expectedFinalPrice);

        // Act - 执行被测方法
        ResponseEntity<?> response = discountController.calculate(
                originalPrice, productType, isNew, memberLevel, isHoliday);

        // Assert - 验证结果
        assertEquals(200, response.getStatusCodeValue());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        
        assertEquals(0.0, (Double) result.get("finalPrice"),
                0.01, "价格应为0");
        assertEquals(0.0, (Double) result.get("discount"),
                0.01, "价格为0时折扣率应为0，避免除零错误");
    }

    /**
     * 测试用例 6：负数价格的异常处理
     * 
     * 测试目的：验证负数价格的容错处理
     * 业务规则：虽然前端应校验，但后端需防御性编程
     * 
     * 3A 分析：
     * - Arrange: 准备价格为-100的商品（异常情况）
     * - Act: 调用折扣计算接口
     * - Assert: 验证系统能正常处理，不抛出异常
     */
    @Test
    @DisplayName("边缘测试 - 负数价格应被正确处理而不抛出异常")
    void testCalculate_NegativePrice_ShouldHandleGracefully() {
        // Arrange - 准备异常测试数据
        double originalPrice = -100.0;
        int productType = 2;
        boolean isNew = false;
        int memberLevel = 0;
        boolean isHoliday = false;
        
        // 设定测试桩：即使价格为负，工厂也返回合理的值
        BigDecimal expectedFinalPrice = BigDecimal.ZERO;
        when(discountStrategyFactory.calculateWithCommonRules(
                eq(BigDecimal.valueOf(originalPrice)),
                any(DiscountContext.class)))
            .thenReturn(expectedFinalPrice);

        // Act - 执行被测方法（不应抛出异常）
        ResponseEntity<?> response = assertDoesNotThrow(() -> 
            discountController.calculate(originalPrice, productType, isNew, memberLevel, isHoliday),
            "负数价格不应导致异常抛出");

        // Assert - 验证结果
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        
        // 验证折扣率计算中的保护逻辑：price > 0 ? ... : 0
        assertEquals(0.0, (Double) result.get("discount"),
                0.01, "负数价格时折扣率应为0");
    }

    /**
     * 测试用例 7：极高价格的精度测试
     * 
     * 测试目的：验证大额交易时的计算精度
     * 业务规则：大额商品折扣计算应保持精度，避免浮点误差
     * 
     * 3A 分析：
     * - Arrange: 准备99999.99元的高价商品
     * - Act: 调用折扣计算接口
     * - Assert: 验证返回价格精度正确
     */
    @Test
    @DisplayName("边缘测试 - 极高价格应保持计算精度")
    void testCalculate_VeryHighPrice_ShouldMaintainPrecision() {
        // Arrange - 准备高精度测试数据
        double originalPrice = 99999.99;
        int productType = 1;
        boolean isNew = false;
        int memberLevel = 3; // 钻石会员
        boolean isHoliday = true;
        
        // 设定测试桩：模拟精确计算后的价格
        BigDecimal expectedFinalPrice = new BigDecimal("76499.99");
        when(discountStrategyFactory.calculateWithCommonRules(
                eq(BigDecimal.valueOf(originalPrice)),
                any(DiscountContext.class)))
            .thenReturn(expectedFinalPrice);

        // Act - 执行被测方法
        ResponseEntity<?> response = discountController.calculate(
                originalPrice, productType, isNew, memberLevel, isHoliday);

        // Assert - 验证结果
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        
        // 验证价格精度：保留两位小数
        assertEquals(expectedFinalPrice.doubleValue(), (Double) result.get("finalPrice"), 0.01,
                "高价商品折扣计算应保持精度");
        
        // 验证折扣率计算的准确性
        double actualDiscount = (Double) result.get("discount");
        assertTrue(actualDiscount > 0 && actualDiscount < 100,
                "折扣率应在合理范围内（0-100之间）");
    }

    // ==================== 异常路径测试用例 ====================

    /**
     * 测试用例 8：工厂返回null的异常处理
     * 
     * 测试目的：验证当策略工厂返回null时的容错能力
     * 业务场景：未注册的商品类型或系统异常
     * 
     * 3A 分析：
     * - Arrange: 模拟工厂返回null
     * - Act: 调用折扣计算接口
     * - Assert: 验证系统抛出NullPointerException或被适当处理
     */
    @Test
    @DisplayName("异常测试 - 工厂返回null时应妥善处理")
    void testCalculate_FactoryReturnsNull_ShouldHandleException() {
        // Arrange - 准备测试数据
        double originalPrice = 100.0;
        int productType = 99; // 未注册的商品类型
        boolean isNew = false;
        int memberLevel = 0;
        boolean isHoliday = false;
        
        // 设定测试桩：模拟工厂返回null（异常情况）
        when(discountStrategyFactory.calculateWithCommonRules(
                any(BigDecimal.class),
                any(DiscountContext.class)))
            .thenReturn(null);

        // Act & Assert - 验证异常处理
        // 注意：实际生产中应该在Controller层添加null检查
        // 这里测试当前实现的行为
        assertThrows(NullPointerException.class, () -> {
            discountController.calculate(originalPrice, productType, isNew, memberLevel, isHoliday);
        }, "工厂返回null时应抛出NullPointerException");
    }

    /**
     * 测试用例 9：验证DiscountContext构建的正确性
     * 
     * 测试目的：确保传递给工厂的上下文对象包含正确的参数
     * 业务规则：所有请求参数应正确映射到上下文对象
     * 
     * 3A 分析：
     * - Arrange: 准备一组特定的参数
     * - Act: 调用折扣计算接口
     * - Assert: 验证工厂接收到的上下文对象参数正确
     */
    @Test
    @DisplayName("集成测试 - 验证上下文参数正确传递到工厂")
    void testCalculate_ShouldPassCorrectContextToFactory() {
        // Arrange - 准备测试数据
        double originalPrice = 200.0;
        int productType = 2;
        boolean isNew = true;
        int memberLevel = 2;
        boolean isHoliday = true;
        
        BigDecimal mockResult = new BigDecimal("150.00");
        when(discountStrategyFactory.calculateWithCommonRules(
                any(BigDecimal.class),
                any(DiscountContext.class)))
            .thenReturn(mockResult);

        // Act - 执行被测方法
        discountController.calculate(originalPrice, productType, isNew, memberLevel, isHoliday);

        // Assert - 验证工厂被调用时传入了正确的参数
        verify(discountStrategyFactory).calculateWithCommonRules(
                eq(BigDecimal.valueOf(originalPrice)),
                argThat(context -> 
                    context != null &&
                    context.getProductType() == productType &&
                    context.getIsNewProduct() == isNew &&
                    context.getMemberLevel() == memberLevel &&
                    context.getIsHolidayPromotion() == isHoliday
                )
        );
    }

    /**
     * 测试用例 10：多次调用的一致性验证
     * 
     * 测试目的：验证相同输入产生相同输出（幂等性）
     * 业务规则：折扣计算应该是确定性的
     * 
     * 3A 分析：
     * - Arrange: 准备相同的测试数据
     * - Act: 多次调用折扣计算接口
     * - Assert: 验证每次返回结果一致
     */
    @Test
    @DisplayName("一致性测试 - 相同输入应产生相同输出")
    void testCalculate_SameInput_ShouldProduceSameOutput() {
        // Arrange - 准备测试数据
        double originalPrice = 300.0;
        int productType = 1;
        boolean isNew = false;
        int memberLevel = 1;
        boolean isHoliday = false;
        
        BigDecimal expectedPrice = new BigDecimal("285.00");
        when(discountStrategyFactory.calculateWithCommonRules(
                eq(BigDecimal.valueOf(originalPrice)),
                any(DiscountContext.class)))
            .thenReturn(expectedPrice);

        // Act - 多次调用
        ResponseEntity<?> response1 = discountController.calculate(
                originalPrice, productType, isNew, memberLevel, isHoliday);
        ResponseEntity<?> response2 = discountController.calculate(
                originalPrice, productType, isNew, memberLevel, isHoliday);

        // Assert - 验证结果一致
        @SuppressWarnings("unchecked")
        Map<String, Object> result1 = (Map<String, Object>) response1.getBody();
        @SuppressWarnings("unchecked")
        Map<String, Object> result2 = (Map<String, Object>) response2.getBody();
        
        assertEquals(result1.get("finalPrice"), result2.get("finalPrice"),
                "相同输入应产生相同的折扣后价格");
        assertEquals(result1.get("discount"), result2.get("discount"),
                "相同输入应产生相同的折扣率");
        
        // 验证工厂被调用了两次
        verify(discountStrategyFactory, times(2))
                .calculateWithCommonRules(any(BigDecimal.class), any(DiscountContext.class));
    }
}

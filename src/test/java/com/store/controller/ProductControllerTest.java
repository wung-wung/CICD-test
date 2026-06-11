package com.store.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.store.entity.Product;
import com.store.service.IProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;

/**
 * ProductController 单元测试类
 * 
 * 测试目标：验证商品管理接口的完整功能覆盖
 * 测试策略：使用 Mock 技术隔离 Service 层，专注 Controller 层逻辑验证
 * 遵循原则：3A原则（Arrange-Act-Assert）
 * 覆盖范围：7个接口方法 + 正向路径 + 边缘条件 + 异常路径
 * 
 * @author Senior Test Engineer
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController 单元测试")
class ProductControllerTest {

    // ==================== 测试桩配置 ====================

    /**
     * Mock 对象：商品服务层
     * 使用 @Mock 创建模拟对象，避免真实数据库操作
     */
    @Mock
    private IProductService productService;

    /**
     * 被测试对象：商品控制器
     * 使用 @InjectMocks 自动注入 Mock 对象
     */
    @InjectMocks
    private ProductController productController;

    /**
     * 测试数据准备
     * 每个测试方法执行前运行
     */
    @BeforeEach
    void setUp() {
        // 初始化通用测试数据
    }

    // ==================== 辅助方法：构建测试数据 ====================

    /**
     * 构建标准商品测试对象
     */
    private Product buildStandardProduct() {
        return Product.builder()
                .id(1L)
                .productName("限量版机械键盘")
                .price(new BigDecimal("299.00"))
                .stock(100)
                .version(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 构建分页结果测试对象
     */
    private IPage<Product> buildPageResult(List<Product> products, long total) {
        Page<Product> page = new Page<>(1, 10, total);
        page.setRecords(products);
        return page;
    }

    // ==================== 接口1: 分页查询商品列表 ====================

    /**
     * 测试用例 1：分页查询 - 默认参数场景
     * 
     * 测试目的：验证使用默认分页参数的查询功能
     * 业务规则：页码默认为1，每页条数默认为10，不过滤商品名称
     * 
     * 3A 分析：
     * - Arrange: 准备空的商品名称参数，使用默认页码和大小
     * - Act: 调用分页查询接口
     * - Assert: 验证Service被正确调用，返回200状态码
     */
    @Test
    @DisplayName("正向测试 - 分页查询使用默认参数应返回成功")
    void testPage_WithDefaultParameters_ShouldReturnSuccess() {
        // Arrange - 准备测试数据
        long defaultCurrent = 1;
        long defaultSize = 10;
        String productName = null;
        
        List<Product> mockProducts = Arrays.asList(
                buildStandardProduct(),
                Product.builder().id(2L).productName("无线鼠标").price(new BigDecimal("99.00")).stock(50).build()
        );
        IPage<Product> mockPage = buildPageResult(mockProducts, 2);
        
        // 设定测试桩：模拟分页查询返回结果
        when(productService.pageProduct(any(Page.class), isNull()))
                .thenReturn(mockPage);

        // Act - 执行被测方法
        ResponseEntity<?> response = productController.page(defaultCurrent, defaultSize, productName);

        // Assert - 验证结果
        assertNotNull(response, "响应不应为null");
        assertEquals(HttpStatus.OK, response.getStatusCode(), "HTTP状态码应为200");
        assertEquals(mockPage, response.getBody(), "响应体应为分页结果");
        
        // 验证Service方法被调用且参数正确
        verify(productService, times(1)).pageProduct(
                argThat(page -> 
                    page.getCurrent() == defaultCurrent && 
                    page.getSize() == defaultSize
                ),
                isNull()
        );
    }

    /**
     * 测试用例 2：分页查询 - 带商品名称过滤
     * 
     * 测试目的：验证商品名称模糊查询功能
     * 业务规则：传入商品名称参数时进行模糊匹配
     * 
     * 3A 分析：
     * - Arrange: 准备特定商品名称"机械"
     * - Act: 调用分页查询接口
     * - Assert: 验证Service接收到正确的过滤参数
     */
    @Test
    @DisplayName("正向测试 - 分页查询带商品名称过滤应正确传递参数")
    void testPage_WithProductNameFilter_ShouldPassCorrectParameter() {
        // Arrange - 准备测试数据
        long current = 2;
        long size = 20;
        String productName = "机械";
        
        List<Product> mockProducts = Collections.singletonList(buildStandardProduct());
        IPage<Product> mockPage = buildPageResult(mockProducts, 1);
        
        when(productService.pageProduct(any(Page.class), eq(productName)))
                .thenReturn(mockPage);

        // Act - 执行被测方法
        ResponseEntity<?> response = productController.page(current, size, productName);

        // Assert - 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // 验证Service方法被调用时传入了正确的商品名称
        verify(productService).pageProduct(
                argThat(page -> 
                    page.getCurrent() == current && 
                    page.getSize() == size
                ),
                eq(productName)
        );
    }

    /**
     * 测试用例 3：分页查询 - 空结果集场景
     * 
     * 测试目的：验证查询无结果时的处理逻辑
     * 业务规则：即使无结果也应返回空的分页对象，而非null
     * 
     * 3A 分析：
     * - Arrange: 模拟Service返回空分页结果
     * - Act: 调用分页查询接口
     * - Assert: 验证返回空列表而非null
     */
    @Test
    @DisplayName("边缘测试 - 分页查询无结果时应返回空列表而非null")
    void testPage_EmptyResult_ShouldReturnEmptyList() {
        // Arrange - 准备测试数据
        long current = 1;
        long size = 10;
        String productName = "不存在的商品";
        
        // 模拟空分页结果
        IPage<Product> emptyPage = buildPageResult(Collections.emptyList(), 0);
        
        when(productService.pageProduct(any(Page.class), eq(productName)))
                .thenReturn(emptyPage);

        // Act - 执行被测方法
        ResponseEntity<?> response = productController.page(current, size, productName);

        // Assert - 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        @SuppressWarnings("unchecked")
        IPage<Product> result = (IPage<Product>) response.getBody();
        assertNotNull(result, "分页结果不应为null");
        assertTrue(result.getRecords().isEmpty(), "记录列表应为空");
        assertEquals(0, result.getTotal(), "总记录数应为0");
    }

    // ==================== 接口2: 查询商品详情 ====================

    /**
     * 测试用例 4：查询详情 - 正常场景
     * 
     * 测试目的：验证根据ID查询商品详情的功能
     * 业务规则：返回完整的商品对象
     * 
     * 3A 分析：
     * - Arrange: 准备ID为1L的商品
     * - Act: 调用查询详情接口
     * - Assert: 验证返回正确的商品对象
     */
    @Test
    @DisplayName("正向测试 - 查询商品详情应返回完整商品对象")
    void testGetById_ValidId_ShouldReturnProduct() {
        // Arrange - 准备测试数据
        Long productId = 1L;
        Product expectedProduct = buildStandardProduct();
        
        when(productService.getProductById(eq(productId)))
                .thenReturn(expectedProduct);

        // Act - 执行被测方法
        ResponseEntity<?> response = productController.getById(productId);

        // Assert - 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedProduct, response.getBody());
        
        verify(productService, times(1)).getProductById(productId);
    }

    /**
     * 测试用例 5：查询详情 - 商品不存在场景（边缘条件）
     * 
     * 测试目的：验证查询不存在商品时的处理
     * 业务规则：根据Service实现可能返回null或抛出异常
     * 
     * 3A 分析：
     * - Arrange: 模拟Service返回null
     * - Act: 调用查询接口
     * - Assert: 验证返回null而非报错
     */
    @Test
    @DisplayName("边缘测试 - 查询不存在的商品应返回null")
    void testGetById_NonExistentProduct_ShouldReturnNull() {
        // Arrange - 准备测试数据
        Long productId = 999L;
        
        when(productService.getProductById(eq(productId)))
                .thenReturn(null);

        // Act - 执行被测方法
        ResponseEntity<?> response = productController.getById(productId);

        // Assert - 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody(), "不存在的商品应返回null");
        
        verify(productService).getProductById(productId);
    }

    // ==================== 接口3: 新增商品 ====================

    /**
     * 测试用例 6：新增商品 - 正常场景
     * 
     * 测试目的：验证新增商品功能
     * 业务规则：返回201 Created状态码和新增的商品对象
     * 
     * 3A 分析：
     * - Arrange: 准备不含ID的新商品对象
     * - Act: 调用新增接口
     * - Assert: 验证返回201状态码和商品对象
     */
    @Test
    @DisplayName("正向测试 - 新增商品应返回201 Created状态码")
    void testAdd_ValidProduct_ShouldReturnCreatedStatus() {
        // Arrange - 准备测试数据
        Product newProduct = Product.builder()
                .productName("新款耳机")
                .price(new BigDecimal("199.00"))
                .stock(200)
                .build();
        
        // 模拟新增后商品被填充了ID
        Product productWithId = Product.builder()
                .id(10L)
                .productName("新款耳机")
                .price(new BigDecimal("199.00"))
                .stock(200)
                .build();
        
        doNothing().when(productService).addProduct(any(Product.class));
        when(productService.getProductById(anyLong())).thenReturn(productWithId);

        // Act - 执行被测方法
        ResponseEntity<?> response = productController.add(newProduct);

        // Assert - 验证结果
        assertEquals(HttpStatus.CREATED, response.getStatusCode(), "应返回201 Created");
        assertNotNull(response.getBody(), "响应体不应为null");
        
        // 验证Service的addProduct方法被调用
        verify(productService, times(1)).addProduct(any(Product.class));
    }

    /**
     * 测试用例 7：新增商品 - 商品对象为null（异常路径）
     * 
     * 测试目的：验证传入null商品对象时的容错处理
     * 业务规则：Spring框架会在反序列化前拦截，此处测试Controller层逻辑
     * 
     * 3A 分析：
     * - Arrange: 准备null商品对象
     * - Act: 调用新增接口
     * - Assert: 验证抛出NullPointerException
     */
    @Test
    @DisplayName("异常测试 - 新增null商品对象应抛出NullPointerException")
    void testAdd_NullProduct_ShouldThrowException() {
        // Arrange - 准备异常测试数据
        Product nullProduct = null;
        
        // Act & Assert - 验证异常
        assertThrows(NullPointerException.class, () -> {
            productController.add(nullProduct);
        }, "传入null商品对象应抛出NullPointerException");
        
        // 验证Service方法未被调用
        verify(productService, never()).addProduct(any());
    }

    // ==================== 接口4: 更新商品 ====================

    /**
     * 测试用例 8：更新商品 - 正常场景
     * 
     * 测试目的：验证全量更新商品功能
     * 业务规则：使用路径中的ID覆盖商品对象的ID，返回更新后的商品
     * 
     * 3A 分析：
     * - Arrange: 准备ID=5L的更新商品对象
     * - Act: 调用更新接口
     * - Assert: 验证商品ID被正确设置，返回200状态码
     */
    @Test
    @DisplayName("正向测试 - 更新商品应使用路径ID覆盖对象ID")
    void testUpdate_ValidProduct_ShouldUsePathId() {
        // Arrange - 准备测试数据
        Long pathId = 5L;
        Product updateProduct = Product.builder()
                .productName("更新后的名称")
                .price(new BigDecimal("399.00"))
                .stock(150)
                .build();
        
        Product updatedProduct = Product.builder()
                .id(pathId)
                .productName("更新后的名称")
                .price(new BigDecimal("399.00"))
                .stock(150)
                .build();
        
        doNothing().when(productService).updateProduct(any(Product.class));
        when(productService.getProductById(eq(pathId))).thenReturn(updatedProduct);

        // Act - 执行被测方法
        ResponseEntity<?> response = productController.update(pathId, updateProduct);

        // Assert - 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        // 验证商品ID被正确设置
        assertEquals(pathId, updateProduct.getId(), "商品ID应被设置为路径参数");
        
        // 验证Service方法调用顺序
        InOrder inOrder = inOrder(productService);
        inOrder.verify(productService).updateProduct(updateProduct);
        inOrder.verify(productService).getProductById(pathId);
    }

    /**
     * 测试用例 9：更新商品 - 更新不存在的商品（边缘条件）
     * 
     * 测试目的：验证更新不存在商品的处理逻辑
     * 业务规则：getProductById可能返回null
     * 
     * 3A 分析：
     * - Arrange: 模拟getProductById返回null
     * - Act: 调用更新接口
     * - Assert: 验证返回null
     */
    @Test
    @DisplayName("边缘测试 - 更新不存在的商品应返回null")
    void testUpdate_NonExistentProduct_ShouldReturnNull() {
        // Arrange - 准备测试数据
        Long pathId = 999L;
        Product updateProduct = Product.builder()
                .productName("不存在的商品")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build();
        
        doNothing().when(productService).updateProduct(any(Product.class));
        when(productService.getProductById(eq(pathId))).thenReturn(null);

        // Act - 执行被测方法
        ResponseEntity<?> response = productController.update(pathId, updateProduct);

        // Assert - 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody(), "更新不存在的商品应返回null");
    }

    // ==================== 接口5: 删除商品 ====================

    /**
     * 测试用例 10：删除商品 - 正常场景
     * 
     * 测试目的：验证删除商品功能
     * 业务规则：返回204 No Content状态码，无响应体
     * 
     * 3A 分析：
     * - Arrange: 准备待删除的商品ID
     * - Act: 调用删除接口
     * - Assert: 验证返回204状态码，无响应体
     */
    @Test
    @DisplayName("正向测试 - 删除商品应返回204 No Content")
    void testRemove_ValidId_ShouldReturnNoContent() {
        // Arrange - 准备测试数据
        Long productId = 1L;
        
        doNothing().when(productService).removeProduct(eq(productId));

        // Act - 执行被测方法
        ResponseEntity<?> response = productController.remove(productId);

        // Assert - 验证结果
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(), "应返回204 No Content");
        assertNull(response.getBody(), "204响应不应有响应体");
        
        verify(productService, times(1)).removeProduct(productId);
    }

    /**
     * 测试用例 11：删除商品 - 删除不存在的商品（边缘条件）
     * 
     * 测试目的：验证删除不存在商品时的处理
     * 业务规则：即使商品不存在也应返回204（幂等性）
     * 
     * 3A 分析：
     * - Arrange: 模拟删除不存在的商品ID
     * - Act: 调用删除接口
     * - Assert: 验证仍返回204
     */
    @Test
    @DisplayName("边缘测试 - 删除不存在的商品应保持幂等性返回204")
    void testRemove_NonExistentId_ShouldStillReturnNoContent() {
        // Arrange - 准备测试数据
        Long nonExistentId = 9999L;
        
        doNothing().when(productService).removeProduct(eq(nonExistentId));

        // Act - 执行被测方法
        ResponseEntity<?> response = productController.remove(nonExistentId);

        // Assert - 验证结果
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(),
                "删除不存在的商品也应返回204，保持幂等性");
        
        verify(productService).removeProduct(nonExistentId);
    }

    // ==================== 接口6: 秒杀扣减库存 ====================

    /**
     * 测试用例 12：秒杀 - 库存充足场景
     * 
     * 测试目的：验证库存充足时的秒杀逻辑
     * 业务规则：扣减库存成功，返回"秒杀成功"
     * 
     * 3A 分析：
     * - Arrange: 模拟秒杀成功场景
     * - Act: 调用秒杀接口
     * - Assert: 验证返回"秒杀成功"
     */
    @Test
    @DisplayName("正向测试 - 库存充足时秒杀应返回成功")
    void testSeckill_SufficientStock_ShouldReturnSuccess() {
        // Arrange - 准备测试数据
        Long productId = 1L;
        String expectedResult = "秒杀成功";
        
        when(productService.seckill(eq(productId)))
                .thenReturn(expectedResult);

        // Act - 执行被测方法
        ResponseEntity<String> response = productController.seckill(productId);

        // Assert - 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
        
        verify(productService, times(1)).seckill(productId);
    }

    /**
     * 测试用例 13：秒杀 - 库存不足场景
     * 
     * 测试目的：验证库存不足时的秒杀逻辑
     * 业务规则：返回"库存不足"
     * 
     * 3A 分析：
     * - Arrange: 模拟库存不足场景
     * - Act: 调用秒杀接口
     * - Assert: 验证返回"库存不足"
     */
    @Test
    @DisplayName("正向测试 - 库存不足时秒杀应返回库存不足")
    void testSeckill_InsufficientStock_ShouldReturnFailure() {
        // Arrange - 准备测试数据
        Long productId = 2L;
        String expectedResult = "库存不足";
        
        when(productService.seckill(eq(productId)))
                .thenReturn(expectedResult);

        // Act - 执行被测方法
        ResponseEntity<String> response = productController.seckill(productId);

        // Assert - 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }

    // ==================== 接口7: 乐观锁秒杀 ====================

    /**
     * 测试用例 14：乐观锁秒杀 - 成功场景
     * 
     * 测试目的：验证乐观锁秒杀成功逻辑
     * 业务规则：使用@Version避免超卖，成功返回"秒杀成功"
     * 
     * 3A 分析：
     * - Arrange: 模拟乐观锁秒杀成功
     * - Act: 调用乐观锁秒杀接口
     * - Assert: 验证返回"秒杀成功"
     */
    @Test
    @DisplayName("正向测试 - 乐观锁秒杀成功应返回成功消息")
    void testSeckillOptimisticLock_Success_ShouldReturnSuccess() {
        // Arrange - 准备测试数据
        Long productId = 1L;
        String expectedResult = "秒杀成功";
        
        when(productService.seckillOptimisticLock(eq(productId)))
                .thenReturn(expectedResult);

        // Act - 执行被测方法
        ResponseEntity<String> response = productController.seckillOptimisticLock(productId);

        // Assert - 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
        
        verify(productService).seckillOptimisticLock(productId);
    }

    /**
     * 测试用例 15：乐观锁秒杀 - 并发冲突场景（边缘条件）
     * 
     * 测试目的：验证乐观锁并发冲突时的处理
     * 业务规则：重试3次后仍冲突，返回"手慢了，请重试"
     * 
     * 3A 分析：
     * - Arrange: 模拟并发冲突场景
     * - Act: 调用乐观锁秒杀接口
     * - Assert: 验证返回"手慢了，请重试"
     */
    @Test
    @DisplayName("边缘测试 - 乐观锁并发冲突应返回重试提示")
    void testSeckillOptimisticLock_Conflict_ShouldReturnRetryMessage() {
        // Arrange - 准备测试数据
        Long productId = 3L;
        String expectedResult = "手慢了，请重试";
        
        when(productService.seckillOptimisticLock(eq(productId)))
                .thenReturn(expectedResult);

        // Act - 执行被测方法
        ResponseEntity<String> response = productController.seckillOptimisticLock(productId);

        // Assert - 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody(),
                "并发冲突时应返回重试提示");
    }

    /**
     * 测试用例 16：乐观锁秒杀 - 库存不足场景
     * 
     * 测试目的：验证乐观锁秒杀库存不足的处理
     * 业务规则：即使使用乐观锁，库存不足也应返回"库存不足"
     * 
     * 3A 分析：
     * - Arrange: 模拟库存不足
     * - Act: 调用乐观锁秒杀接口
     * - Assert: 验证返回"库存不足"
     */
    @Test
    @DisplayName("正向测试 - 乐观锁秒杀库存不足应返回库存不足")
    void testSeckillOptimisticLock_InsufficientStock_ShouldReturnFailure() {
        // Arrange - 准备测试数据
        Long productId = 4L;
        String expectedResult = "库存不足";
        
        when(productService.seckillOptimisticLock(eq(productId)))
                .thenReturn(expectedResult);

        // Act - 执行被测方法
        ResponseEntity<String> response = productController.seckillOptimisticLock(productId);

        // Assert - 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }

    // ==================== 综合测试：HTTP状态码验证 ====================

    /**
     * 测试用例 17：验证所有接口的HTTP状态码正确性
     * 
     * 测试目的：确保RESTful状态码规范被正确遵循
     * 业务规则：
     * - GET/PUT 返回 200 OK
     * - POST 新增返回 201 Created
     * - DELETE 返回 204 No Content
     * 
     * 3A 分析：
     * - Arrange: 准备所有接口的Mock数据
     * - Act: 依次调用各接口
     * - Assert: 验证状态码符合RESTful规范
     */
    @Test
    @DisplayName("集成测试 - 验证RESTful状态码规范")
    void testAllEndpoints_ShouldFollowRestfulStatusCodeConvention() {
        // Arrange - 准备测试数据
        Product product = buildStandardProduct();
        Long productId = 1L;
        
        // Mock所有Service方法
        when(productService.pageProduct(any(), any())).thenReturn(buildPageResult(Collections.emptyList(), 0));
        when(productService.getProductById(anyLong())).thenReturn(product);
        doNothing().when(productService).addProduct(any());
        doNothing().when(productService).updateProduct(any());
        doNothing().when(productService).removeProduct(anyLong());
        when(productService.seckill(anyLong())).thenReturn("秒杀成功");
        when(productService.seckillOptimisticLock(anyLong())).thenReturn("秒杀成功");

        // Act & Assert - 验证各接口状态码
        // GET 应返回 200
        assertEquals(HttpStatus.OK, productController.page(1, 10, null).getStatusCode());
        assertEquals(HttpStatus.OK, productController.getById(productId).getStatusCode());
        
        // POST 新增应返回 201
        assertEquals(HttpStatus.CREATED, productController.add(product).getStatusCode());
        
        // PUT 应返回 200
        assertEquals(HttpStatus.OK, productController.update(productId, product).getStatusCode());
        
        // DELETE 应返回 204
        assertEquals(HttpStatus.NO_CONTENT, productController.remove(productId).getStatusCode());
        
        // 秒杀接口应返回 200
        assertEquals(HttpStatus.OK, productController.seckill(productId).getStatusCode());
        assertEquals(HttpStatus.OK, productController.seckillOptimisticLock(productId).getStatusCode());
    }

    // ==================== 验证Mock调用次数 ====================

    /**
     * 测试用例 18：验证Service方法调用次数（防重复调用）
     * 
     * 测试目的：确保每个接口只调用Service方法一次
     * 业务规则：避免不必要的重复查询或操作
     * 
     * 3A 分析：
     * - Arrange: 准备测试数据
     * - Act: 调用接口
     * - Assert: 验证Service方法仅被调用1次
     */
    @Test
    @DisplayName("性能测试 - 验证Service方法不被重复调用")
    void testServiceMethods_ShouldBeCalledExactlyOnce() {
        // Arrange - 准备测试数据
        Long productId = 1L;
        Product product = buildStandardProduct();
        
        when(productService.getProductById(productId)).thenReturn(product);

        // Act - 执行被测方法
        productController.getById(productId);

        // Assert - 验证调用次数
        verify(productService, times(1)).getProductById(productId);
        verify(productService, never()).pageProduct(any(), any());
        verify(productService, never()).addProduct(any());
        verify(productService, never()).updateProduct(any());
        verify(productService, never()).removeProduct(anyLong());
    }
}

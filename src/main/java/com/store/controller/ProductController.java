package com.store.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.store.entity.Product;
import com.store.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制层
 *
 * RESTful 路由设计：
 *   GET    /products          - 分页查询商品列表
 *   GET    /products/{id}     - 查询商品详情
 *   POST   /products          - 新增商品
 *   PUT    /products/{id}     - 全量更新商品
 *   DELETE /products/{id}     - 删除商品
 */
@Tag(name = "商品管理", description = "商品的增删改查接口")
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final IProductService productService;

    @Operation(summary = "分页查询商品列表", description = "支持按商品名称模糊查询，结果按创建时间降序排列")
    @GetMapping
    public ResponseEntity<?> page(
            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数", example = "10")
            @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "商品名称（模糊查询，可选）")
            @RequestParam(required = false) String productName) {
        return ResponseEntity.ok(productService.pageProduct(new Page<>(current, size), productName));
    }

    @Operation(summary = "查询商品详情", description = "根据商品 ID 查询单条商品信息")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(
            @Parameter(description = "商品ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "新增商品", description = "新增一条商品记录，id 由数据库自增生成，无需传入")
    @PostMapping
    public ResponseEntity<?> add(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "商品信息，无需传 id")
            @RequestBody Product product) {
        productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @Operation(summary = "全量更新商品", description = "根据路径中的 id 全量更新商品字段")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @Parameter(description = "商品ID", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "更新后的商品信息")
            @RequestBody Product product) {
        product.setId(id);
        productService.updateProduct(product);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "删除商品", description = "根据商品 ID 删除记录，成功返回 204 No Content")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> remove(
            @Parameter(description = "商品ID", required = true, example = "1")
            @PathVariable Long id) {
        productService.removeProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "秒杀扣减库存", description = "库存大于 0 时扣减 1 并返回\"秒杀成功\"，否则返回\"库存不足\"")
    @PostMapping("/{id}/seckill")
    public ResponseEntity<String> seckill(
            @Parameter(description = "商品ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.seckill(id));
    }

    @Operation(summary = "秒杀扣减库存（乐观锁）",
               description = "使用乐观锁（@Version）避免超卖，并发冲突时最多重试 3 次，超过后返回\"手慢了，请重试\"")
    @PostMapping("/{id}/seckill-optimistic")
    public ResponseEntity<String> seckillOptimisticLock(
            @Parameter(description = "商品ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.seckillOptimisticLock(id));
    }

}

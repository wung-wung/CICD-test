package com.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.store.entity.Product;
import com.store.mapper.ProductMapper;
import com.store.service.IProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 商品业务逻辑层实现
 */
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    /**
     * 分页查询商品列表，支持按商品名称模糊过滤
     */
    @Override
    public IPage<Product> pageProduct(Page<Product> page, String productName) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(productName), Product::getProductName, productName)
               .orderByDesc(Product::getCreateTime);
        return this.page(page, wrapper);
    }

    /**
     * 新增商品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProduct(Product product) {
        this.save(product);
    }

    /**
     * 修改商品信息（按主键更新非空字段）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(Product product) {
        if (product.getId() == null) {
            throw new IllegalArgumentException("更新商品时 id 不能为空");
        }
        this.updateById(product);
    }

    /**
     * 按主键删除商品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeProduct(Long id) {
        if (!this.removeById(id)) {
            throw new RuntimeException("删除失败，商品不存在，id=" + id);
        }
    }

    /**
     * 按主键查询商品详情，不存在时抛出异常
     */
    @Override
    public Product getProductById(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在，id=" + id);
        }
        return product;
    }

    /**
     * 秒杀扣减库存
     * 流程：查询商品 → 判断库存 → 库存 -1 并更新
     */
    @Override
    @Transactional
    public String seckill(Long id) {
        // 1. 查询商品
        Product product = getProductById(id);

        // 2. 判断库存
        if (product.getStock() <= 0) {
            return "库存不足";
        }

        // 3. 库存 -1 并更新到数据库
        product.setStock(product.getStock() - 1);
        this.updateById(product);

        return "秒杀成功";
    }

    /**
     * 秒杀扣减库存（乐观锁版）
     *
     * 原理：MyBatis-Plus 的 @Version 会将 updateById 自动改为：
     *   UPDATE product SET stock=?, version=version+1
     *   WHERE id=? AND version=?【当前版本号】
     * 若并发情况下其他线程已修改 version，此 UPDATE 影响行数=0
     * updateById 返回 false → 截断重试，直到成功或超过最大重试次数
     *
     * 最大重试次数设置为 3，超过后返回 "手慢了，请重试"
     */
    @Override
    public String seckillOptimisticLock(Long id) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            // 1. 每次重试都重新查询，获取最新的 version
            Product product = getProductById(id);

            // 2. 判断库存
            if (product.getStock() <= 0) {
                return "库存不足";
            }

            // 3. 库存 -1，MyBatis-Plus 会自动携带当前 version 到 WHERE 条件
            product.setStock(product.getStock() - 1);
            boolean success = this.updateById(product);

            if (success) {
                return "秒杀成功";
            }
            // success=false 表示 version 冲突，进入下一轮重试
        }
        return "手慢了，请重试";
    }

}

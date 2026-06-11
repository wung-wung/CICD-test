package com.store.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.store.entity.Product;

/**
 * 商品业务逻辑层接口
 */
public interface IProductService extends IService<Product> {

    /**
     * 分页查询商品列表
     *
     * @param page      分页参数（页码、每页条数）
     * @param productName 商品名称（模糊查询，传 null 则不过滤）
     * @return 分页结果
     */
    IPage<Product> pageProduct(Page<Product> page, String productName);

    /**
     * 新增商品
     *
     * @param product 商品信息
     */
    void addProduct(Product product);

    /**
     * 修改商品信息
     *
     * @param product 商品信息（必须包含 id）
     */
    void updateProduct(Product product);

    /**
     * 按主键删除商品
     *
     * @param id 商品ID
     */
    void removeProduct(Long id);

    /**
     * 按主键查询商品详情
     *
     * @param id 商品ID
     * @return 商品实体，不存在时抛出异常
     */
    Product getProductById(Long id);

    /**
     * 秒杀扣减库存
     *
     * @param id 商品ID
     * @return "秒杀成功" 或 "库存不足"
     */
    String seckill(Long id);

    /**
     * 秒杀扣减库存（乐观锁版）
     * 利用 @Version 字段避免超卖，并发冲突时自动重试
     *
     * @param id 商品ID
     * @return "秒杀成功"、"库存不足" 或 "手慢了，请重试"
     */
    String seckillOptimisticLock(Long id);

}

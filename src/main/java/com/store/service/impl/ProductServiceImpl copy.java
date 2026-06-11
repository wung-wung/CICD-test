// package com.store.service.impl;

// import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
// import com.baomidou.mybatisplus.core.metadata.IPage;
// import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// import com.store.entity.Product;
// import com.store.mapper.ProductMapper;
// import com.store.service.IProductService;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.util.StringUtils;

// /**
//  * 商品业务逻辑层实现
//  */
// @Service
// public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

//     /**
//      * 分页查询商品列表，支持按商品名称模糊过滤
//      */
//     @Override
//     public IPage<Product> pageProduct(Page<Product> page, String productName) {
//         LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
//         wrapper.like(StringUtils.hasText(productName), Product::getProductName, productName)
//                .orderByDesc(Product::getCreateTime);
//         return this.page(page, wrapper);
//     }

//     /**
//      * 新增商品
//      */
//     @Override
//     @Transactional(rollbackFor = Exception.class)
//     public void addProduct(Product product) {
//         this.save(product);
//     }

//     /**
//      * 修改商品信息（按主键更新非空字段）
//      */
//     @Override
//     @Transactional(rollbackFor = Exception.class)
//     public void updateProduct(Product product) {
//         if (product.getId() == null) {
//             throw new IllegalArgumentException("更新商品时 id 不能为空");
//         }
//         this.updateById(product);
//     }

//     /**
//      * 按主键删除商品
//      */
//     @Override
//     @Transactional(rollbackFor = Exception.class)
//     public void removeProduct(Long id) {
//         if (!this.removeById(id)) {
//             throw new RuntimeException("删除失败，商品不存在，id=" + id);
//         }
//     }

//     /**
//      * 按主键查询商品详情，不存在时抛出异常
//      */
//     @Override
//     public Product getProductById(Long id) {
//         Product product = this.getById(id);
//         if (product == null) {
//             throw new RuntimeException("商品不存在，id=" + id);
//         }
//         return product;
//     }

//     /**
//      * 秒杀扣减库存
//      * 流程：查询商品 → 判断库存 → 库存 -1 并更新
//      *
//      * ⚠️ 【教学演示】超卖 Bug 复现：
//      *   - 无事务、无锁保护
//      *   - 扣减使用 SQL：stock = stock - 1（数据库原子递减）
//      *   - 每个线程都会真实修改数据库的值，不会出现"丢失更新"
//      *   - 多线程同时通过 stock > 0 判断后，各自执行 -1
//      *   - 最终库存将变为负数，复现典型超卖问题
//      */
//     @Override
//     public String seckill(Long id) {
//         // 1. 查询商品
//         Product product = getProductById(id);

//         // 2. 判断库存（此时多个线程可能同时读到 stock > 0）
//         if (product.getStock() <= 0) {
//             return "库存不足";
//         }

//         // ⚠️ 模拟业务耗时，放大并发竞争窗口
//         try {
//             Thread.sleep(100);
//         } catch (InterruptedException e) {
//             Thread.currentThread().interrupt();
//         }

//         // 3. 使用 SQL 表达式 stock = stock - 1 直接原子递减
//         //    区别于 product.setStock(product.getStock() - 1)：
//         //    后者以查询快照值计算，多线程并发时结果互相覆盖，库存不会变负
//         //    前者让数据库每次都在当前值基础上 -1，库存会真实递减直至变负
//         LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
//         updateWrapper.eq(Product::getId, id)
//                      .setSql("stock = stock - 1");
//         this.update(updateWrapper);

//         return "秒杀成功";
//     }

// }

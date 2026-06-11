package com.store.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.store.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品数据访问层
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

}

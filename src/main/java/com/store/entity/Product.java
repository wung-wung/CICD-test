package com.store.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


/*请帮我生成Product实体类,要求：
* 1.使用 Lombok的@Data注解*/



/**
 * 秒杀商品库存表
 */
@Schema(description = "商品实体")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("product")
public class Product {

    @Schema(description = "商品主键ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "商品名称", example = "限量版机械键盘", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("product_name")
    private String productName;

    @Schema(description = "商品价格（单位：元）", example = "299.00", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "剩余库存量", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    @TableField("stock")
    private Integer stock;

    @Schema(description = "创建时间，由系统自动填充", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间，由系统自动填充", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "乐观锁版本号", example = "0", accessMode = Schema.AccessMode.READ_ONLY)
    @TableField("version")
    @Version
    private Integer version;
}

package com.store.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / OpenAPI 全局配置
 * 访问地址：http://localhost:8080/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI storeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Store 商品管理系统 API")
                        .description("基于 Spring Boot 2 + MyBatis-Plus 构建的商品管理接口文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("store")
                                .email("store@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

}

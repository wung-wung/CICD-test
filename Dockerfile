# 使用 Maven 构建 Spring Boot 应用
FROM maven:3.9.8-eclipse-temurin-11 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# 运行阶段：使用更小的基础镜像
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

EXPOSE 8080

# 健康检查（需要 spring-boot-starter-actuator 依赖）
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
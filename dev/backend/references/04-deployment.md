# 部署规范

## 打包命令

```bash
# Maven
mvn clean package -DskipTests

# Gradle
gradle clean build -x test
```

## Docker 部署

```dockerfile
FROM openjdk:21-slim as build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM openjdk:21-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml
version: "3.8"
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    restart: always
    depends_on:
      - mysql
      - redis
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: secret
  redis:
    image: redis:7-alpine
```

## 环境配置

- 使用 Spring Profile 区分环境（dev、test、prod）
- 敏感配置使用环境变量或加密配置
- 配置文件分离：`application.yml`、`application-{profile}.yml`

## 回滚策略

1. 保留上一个稳定版本的 JAR 包
2. 使用 `docker-compose pull && docker-compose up -d` 更新
3. 失败时执行 `docker-compose down && docker-compose up -d` 回滚

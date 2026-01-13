# ==================== Stage 1: Build ====================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# 复制 Maven 配置文件（利用 Docker 缓存层）
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

# 下载依赖（单独一层，便于缓存）
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建应用（跳过测试）
RUN mvn clean package -DskipTests -B

# ==================== Stage 2: Runtime ====================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="OnLineVideo Team"
LABEL description="OnLineVideo Backend Service"

WORKDIR /app

# 创建非 root 用户
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# 创建存储目录
RUN mkdir -p /app/storage/videos /app/storage/images && \
    chown -R appuser:appgroup /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /build/target/*.jar app.jar

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 设置 JVM 参数和启动命令
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --spring.profiles.active=docker"]

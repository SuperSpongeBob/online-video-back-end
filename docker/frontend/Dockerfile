# ==================== Stage 1: Build ====================
FROM node:18-alpine AS builder

WORKDIR /build

# 复制 package.json 和 lock 文件（利用 Docker 缓存层）
COPY front/onlinevideo/package.json front/onlinevideo/package-lock.json* front/onlinevideo/yarn.lock* ./

# 安装依赖
RUN npm install --registry=https://registry.npmmirror.com || yarn install

# 复制源代码
COPY front/onlinevideo/ .

# 构建生产版本
RUN npm run build || yarn build

# ==================== Stage 2: Runtime ====================
FROM nginx:alpine

LABEL maintainer="OnLineVideo Team"
LABEL description="OnLineVideo Frontend Service"

# 删除默认配置
RUN rm -rf /etc/nginx/conf.d/default.conf

# 复制自定义 Nginx 配置
COPY docker/frontend/nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制静态文件
COPY --from=builder /build/dist /usr/share/nginx/html

# 创建错误页面
RUN echo '<!DOCTYPE html><html><head><title>Service Unavailable</title></head><body><h1>503 Service Temporarily Unavailable</h1></body></html>' > /usr/share/nginx/html/50x.html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost/health || exit 1

# 启动 Nginx
CMD ["nginx", "-g", "daemon off;"]

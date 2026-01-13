# Docker 部署指南

## 快速开始

### 1. 准备环境变量

复制环境变量模板并修改配置：

```bash
cp .env.example .env
```

编辑 `.env` 文件，设置以下关键配置：
- `MYSQL_ROOT_PASSWORD`: MySQL root 密码
- `JWT_SECRET`: JWT 密钥（至少64个字符）
- `MAIL_USERNAME` / `MAIL_PASSWORD`: 邮件服务配置（可选）

### 2. 构建并启动服务

```bash
# 构建并启动所有服务（后台运行）
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 访问服务

- 前端页面: http://localhost
- 后端 API: http://localhost/api/
- Swagger 文档: http://localhost/swagger-ui/

## 服务说明

| 服务 | 端口 | 说明 |
|------|------|------|
| frontend | 80 | Nginx + Vue 3 前端 |
| backend | 8080 | Spring Boot 后端 |
| mysql | 3306 | MySQL 8.0 数据库 |
| redis | 6379 | Redis 7 缓存 |

## 常用命令

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷（清除所有数据）
docker-compose down -v

# 重新构建单个服务
docker-compose build backend
docker-compose up -d backend

# 进入容器
docker exec -it onlinevideo-backend sh
docker exec -it onlinevideo-mysql mysql -uroot -p

# 查看日志
docker-compose logs -f backend
docker-compose logs -f mysql
```

## 数据持久化

数据存储在以下 Docker 卷中：
- `onlinevideo-mysql-data`: MySQL 数据
- `onlinevideo-redis-data`: Redis 数据
- `onlinevideo-upload-data`: 上传的视频和图片

## 默认账户

系统初始化后包含以下测试账户：

| 角色 | 手机号 | 密码 |
|------|--------|------|
| 管理员 | 17806573192 | 123456 |
| VIP用户 | 18613163192 | 123456 |
| 普通用户 | 17806570000 | 123456 |

## 故障排除

### MySQL 连接失败
```bash
# 检查 MySQL 是否就绪
docker-compose logs mysql

# 等待 MySQL 完全启动后重启后端
docker-compose restart backend
```

### 前端无法访问后端 API
```bash
# 检查后端健康状态
curl http://localhost:8080/actuator/health

# 检查 Nginx 配置
docker exec onlinevideo-frontend cat /etc/nginx/conf.d/default.conf
```

### 清理并重新部署
```bash
docker-compose down -v
docker system prune -f
docker-compose up -d --build
```

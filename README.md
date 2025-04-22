# online-video
在线视频后端服务,练习专用

管理员：账号：17806573192	密码：admin123
VIP用户：账号：18613163192 密码：vip123
普通用户：账号：17806570000 密码：user123

nginx配置如下：

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;	#启用高效文件传输
    tcp_nopush     on;	#减少网络报文数量
	tcp_nodelay on;	#禁用 Nagle 算法

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #gzip  on;

    server {
        listen       80;
        #server_name  localhost;
	server_name  192.168.1.1;
 
	location /images/ {
		alias E:/mysqlStorage/images/;
		autoindex on;
		expires 1h;	#在浏览器缓存一个小时
		add_header Cache-Control "public, no-transform";
	}

	location /api/upload {
		#仅针对上传接口放宽限制
		client_max_body_size 200M;
		
		proxy_pass http://localhost:8080/api/upload;
		proxy_set_header Host $host;
		proxy_set_header X-Real-IP $remote_addr;
		proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
	}


#禁止直接访问视频目录
        location /videos/ {
            internal;	#仅允许内部重定向访问
            alias E:/mysqlStorage/videos/;
		add_header Content-Type "video/mp4";  # 强制设置类型
		#autoindex off;	#禁止目录遍历
        }

#代理后的接口请求
	location /api/{
		proxy_pass http://localhost:8080/api/;
        	proxy_set_header Host $host;
        	proxy_set_header X-Real-IP $remote_addr;
        	proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}

以下为项目运行环境
IntelliJ IDEA 2024.1 (Ultimate Edition)
Build #IU-241.14494.240, built on March 28, 2024
Licensed to kiddy inseams
You have a perpetual fallback license for this version.
Subscription is active until August 1, 2025.
Runtime version: 17.0.10+8-b1207.12 amd64
VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.
Windows 11.0
GC: G1 Young Generation, G1 Old Generation
Memory: 1024M
Cores: 22
Registry:
  ide.experimental.ui=true
Kotlin: 241.14494.240-IJ

Version: 1.98.2 (user setup)
Commit: ddc367ed5c8936efe395cffeec279b04ffd7db78
Date: 2025-03-12T13:32:45.399Z
Electron: 34.2.0
ElectronBuildId: 11161602
Chromium: 132.0.6834.196
Node.js: 20.18.2
V8: 13.2.152.36-electron.0
OS: Windows_NT x64 10.0.26100

jdk：18.0.2
MySQL：8.0
node：v20.13.1

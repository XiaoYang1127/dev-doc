<!-- @format -->

# 部署规范

> 参考来源：Vite 官方部署指南 (https://vitejs.dev/guide/static-depoly.html)

## 环境变量

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080

# .env.production
VITE_API_BASE_URL=https://api.production.com
```

## 构建命令

```bash
# 开发
npm run dev

# 构建
npm run build

# 预览
npm run preview

# 类型检查
npm run type-check

# 代码检查
npm run lint
```

## Nginx 配置

```nginx
server {
    listen 80;
    server_name example.com;
    root /var/www/html;
    index index.html;

    # 前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api {
        proxy_pass http://backend:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

## Docker 部署

```dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 打包分析

```bash
# 查看 bundle 大小
npm run build:analyze
```

# Flowable 仿真验证 Vue 前端

这是从 `src/main/web` 三个原生 HTML 页面迁移出来的 Vue 3 + Vite 前端入口。

## 开发

```powershell
npm install
npm run dev
```

开发服务器默认运行在 `http://localhost:5173`，`/api` 会代理到 `http://localhost:8001`。

## 页面

- `/flows`：原版流程管理页面（完整保留原样式和功能）
- `/simulation-history`：原版仿真运行记录和多方案对比页面
- `/viewer/:diagramId`：原版 D3 流程图、节点配置和 WebSocket 看板
- `/vue/flows`、`/vue/simulation-history`、`/vue/viewer/:diagramId`：Vue 组件迁移版本

## 构建

```powershell
npm run build
```

构建结果在 `dist/`。部署到 Spring Boot 时，将 `dist` 内容复制到 `src/main/resources/static/`。

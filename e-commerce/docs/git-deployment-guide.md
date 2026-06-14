# Git 部署指南

## 当前环境状态

检测到你的系统 **未安装 Git**。请按以下步骤完成 Git 安装和代码提交。

---

## 步骤一：安装 Git

### Windows 安装方法

1. **下载 Git for Windows**
   - 访问 https://git-scm.com/download/win
   - 下载适合你系统的版本（通常选择 64-bit）

2. **安装 Git**
   - 运行下载的安装包
   - 使用默认选项即可
   - 安装完成后，重新打开 PowerShell 验证：

```powershell
git --version
# 应该显示类似: git version 2.43.0.windows.1
```

---

## 步骤二：配置 Git 用户信息

```powershell
# 设置你的用户名和邮箱
git config --global user.name "你的名字"
git config --global user.email "你的邮箱@example.com"
```

---

## 步骤三：初始化本地仓库并提交

在项目根目录执行以下命令：

```powershell
# 进入项目目录
cd D:\e-commerce

# 初始化 Git 仓库
git init

# 添加所有文件到暂存区
git add .

# 提交到本地仓库
git commit -m "feat: 完善电商购物平台

- 实现JWT Token登录验证和拦截器
- 优化前端API自动携带Token
- 添加路由守卫验证登录状态
- 增强全局异常处理
- 编写登录验证和查询功能说明文档"

# 查看提交历史
git log --oneline
```

---

## 步骤四：创建远程仓库

### 方案A：使用 Gitee（国内推荐）

1. 访问 https://gitee.com 并登录
2. 点击右上角"+" → "新建仓库"
3. 填写仓库信息：
   - 仓库名称：`e-commerce`
   - 是否开源：私有
   - **不要**勾选"初始化仓库"
4. 点击"创建"

### 方案B：使用 GitHub

1. 访问 https://github.com 并登录
2. 点击右上角"+" → "New repository"
3. 填写仓库信息：
   - Repository name：`e-commerce`
   - Visibility：Private
   - **不要**勾选"Initialize this repository with a README"
4. 点击"Create repository"

---

## 步骤五：推送代码到远程仓库

创建仓库后，在 PowerShell 中执行：

```powershell
# 添加远程仓库（替换为你的仓库地址）
# Gitee 示例
git remote add origin https://gitee.com/你的用户名/e-commerce.git

# 或 GitHub 示例
# git remote add origin https://github.com/你的用户名/e-commerce.git

# 推送代码
git branch -M main
git push -u origin main
```

---

## 提交后截图

提交完成后，请：

1. **截取提交历史截图**
   ```powershell
   git log --oneline
   ```

2. **截取远程仓库页面截图**
   - 在浏览器中打开你的 Gitee/GitHub 仓库页面
   - 截图保存

3. **记录仓库地址**
   - Gitee: `https://gitee.com/你的用户名/e-commerce`
   - GitHub: `https://github.com/你的用户名/e-commerce`

---

## 常见问题

### Q1: 推送时提示输入用户名密码

**解决方法**：
- Gitee：使用你的 Gitee 用户名和密码
- GitHub：建议使用 Personal Access Token（PAT）
  - 访问 https://github.com/settings/tokens
  - 创建新 Token，勾选 `repo` 权限
  - 使用 Token 作为密码

### Q2: 推送失败提示 "rejected"

**原因**：远程仓库已有提交

**解决方法**：
```powershell
# 先拉取远程代码
git pull origin main --rebase

# 再推送
git push origin main
```

### Q3: 中文文件名显示乱码

**解决方法**：
```powershell
git config --global core.quotepath false
```

---

## 项目文件结构

```
e-commerce/
├── backend/                          # 后端 Spring Boot 项目
│   ├── pom.xml
│   └── src/main/java/com/ecommerce/
│       ├── config/                   # 配置类
│       │   ├── JwtAuthenticationFilter.java  # JWT拦截器
│       │   └── FilterConfig.java              # 过滤器配置
│       ├── controller/               # 控制器层
│       ├── service/                  # 业务逻辑层
│       ├── repository/               # 数据访问层
│       ├── entity/                   # 实体类
│       ├── common/                   # 公共类
│       │   ├── Result.java           # 统一响应封装
│       │   └── GlobalExceptionHandler.java  # 全局异常处理
│       └── util/                     # 工具类
│           └── UserContext.java      # 用户上下文工具
│
├── frontend/                         # 前端 Vue 3 项目
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/index.js              # API请求封装（自动携带Token）
│       ├── router/index.js           # 路由配置（含守卫）
│       └── views/                    # 页面组件
│
└── docs/                             # 文档
    ├── architecture.md               # 架构设计文档
    ├── ai-usage-experience.md        # AI使用心得
    ├── login-and-query-explanation.md # 登录验证和查询功能说明
    ├── mybatis-plus-order-crud.md    # MyBatis Plus订单CRUD说明
    └── ai-usage-experience.md         # AI使用心得
```

---

## 下一步

完成 Git 提交后，你可以：

1. 将仓库地址和截图添加到实验报告中
2. 继续完善项目功能
3. 部署到云服务器（可选）

**祝你顺利！** 🎉

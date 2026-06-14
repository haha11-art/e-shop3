-- ============================================
-- 电商购物平台数据库初始化脚本
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS ecommerce DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    address VARCHAR(255) COMMENT '默认地址',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='用户表';

-- ============================================
-- 2. 商品分类表
-- ============================================
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    description VARCHAR(255) COMMENT '分类描述',
    parent_id BIGINT COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='商品分类表';

-- ============================================
-- 3. 商品表
-- ============================================
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    original_price DECIMAL(10,2) COMMENT '原价',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    sales_count INT DEFAULT 0 COMMENT '销量',
    image_url VARCHAR(500) COMMENT '主图URL',
    images TEXT COMMENT '图片列表(JSON)',
    brand VARCHAR(50) COMMENT '品牌',
    category_id BIGINT COMMENT '分类ID',
    status VARCHAR(20) NOT NULL DEFAULT 'ON_SALE' COMMENT '状态: ON_SALE/OFF_SHELF',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='商品表';

-- ============================================
-- 4. 购物车表
-- ============================================
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    selected TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否选中',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='购物车表';

-- ============================================
-- 5. 订单表
-- ============================================
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单编号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    freight DECIMAL(10,2) DEFAULT 0.00 COMMENT '运费',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/PAID/SHIPPED/COMPLETED/CANCELLED',
    shipping_address VARCHAR(500) COMMENT '收货地址',
    receiver_name VARCHAR(50) COMMENT '收货人',
    receiver_phone VARCHAR(20) COMMENT '收货电话',
    pay_type VARCHAR(20) COMMENT '支付方式',
    pay_time DATETIME COMMENT '支付时间',
    ship_time DATETIME COMMENT '发货时间',
    complete_time DATETIME COMMENT '完成时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB COMMENT='订单表';

-- ============================================
-- 6. 订单项表
-- ============================================
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称快照',
    product_image VARCHAR(500) COMMENT '商品图片快照',
    unit_price DECIMAL(10,2) NOT NULL COMMENT '购买单价',
    quantity INT NOT NULL COMMENT '购买数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='订单项表';

-- ============================================
-- 初始化测试数据
-- ============================================

-- 插入管理员和普通用户
INSERT INTO users (username, password, nickname, email, phone, address, role) VALUES
('admin', 'admin123', '系统管理员', 'admin@ecommerce.com', '13800000000', '北京市朝阳区', 'ADMIN'),
('testuser', '123456', '测试用户', 'test@ecommerce.com', '13800000001', '上海市浦东新区', 'USER');

-- 插入商品分类
INSERT INTO categories (name, description, parent_id, sort_order) VALUES
('手机数码', '手机、平板等数码产品', NULL, 1),
('电脑办公', '笔记本、台式机等', NULL, 2),
('家用电器', '冰箱、洗衣机等', NULL, 3),
('服装鞋包', '男装、女装、鞋靴', NULL, 4),
('食品生鲜', '水果、蔬菜、零食', NULL, 5);

-- 插入子分类
INSERT INTO categories (name, description, parent_id, sort_order) VALUES
('智能手机', '各品牌智能手机', 1, 1),
('平板电脑', '各品牌平板电脑', 1, 2),
('笔记本电脑', '各品牌笔记本', 2, 1),
('台式机', '组装/品牌台式机', 2, 2),
('冰箱', '各类型冰箱', 3, 1),
('洗衣机', '各类型洗衣机', 3, 2);

-- 插入商品数据
INSERT INTO products (name, description, price, original_price, stock, sales_count, image_url, brand, category_id, status) VALUES
('iPhone 15 Pro Max 256GB', '苹果最新旗舰手机，A17 Pro芯片，钛金属设计', 9999.00, 10999.00, 100, 520, 'https://via.placeholder.com/400x400?text=iPhone15', 'Apple', 6, 'ON_SALE'),
('华为 Mate 60 Pro', '华为旗舰，麒麟9000S芯片，卫星通话', 7999.00, 8499.00, 80, 380, 'https://via.placeholder.com/400x400?text=Mate60', '华为', 6, 'ON_SALE'),
('小米14 Ultra', '骁龙8Gen3，徕卡光学镜头', 5999.00, 6499.00, 150, 260, 'https://via.placeholder.com/400x400?text=Mi14', '小米', 6, 'ON_SALE'),
('iPad Pro M4 11英寸', 'Apple M4芯片，OLED显示屏', 8999.00, 9999.00, 60, 120, 'https://via.placeholder.com/400x400?text=iPadPro', 'Apple', 7, 'ON_SALE'),
('MacBook Pro 14英寸 M3', 'Apple M3 Pro芯片，Liquid Retina XDR', 14999.00, 16999.00, 40, 85, 'https://via.placeholder.com/400x400?text=MacBook', 'Apple', 8, 'ON_SALE'),
('联想 ThinkPad X1 Carbon', '14英寸轻薄商务本，酷睿Ultra处理器', 9999.00, 11999.00, 50, 95, 'https://via.placeholder.com/400x400?text=ThinkPad', '联想', 8, 'ON_SALE'),
('戴尔 XPS 15 笔记本', '15.6英寸OLED屏，酷睿i7处理器', 12999.00, 14999.00, 30, 45, 'https://via.placeholder.com/400x400?text=XPS15', '戴尔', 8, 'ON_SALE'),
('海尔冰箱 BCD-500', '500升对开门冰箱，一级能效变频', 3999.00, 4999.00, 70, 180, 'https://via.placeholder.com/400x400?text=Haier', '海尔', 10, 'ON_SALE'),
('美的洗衣机 MD100', '10公斤洗烘一体机，智能投放', 2999.00, 3599.00, 90, 220, 'https://via.placeholder.com/400x400?text=Midea', '美的', 11, 'ON_SALE'),
('OPPO Find X7 Ultra', '哈苏影像，双潜望长焦', 5499.00, 5999.00, 100, 150, 'https://via.placeholder.com/400x400?text=OPPO', 'OPPO', 6, 'ON_SALE');

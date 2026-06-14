-- 测试用初始化数据（仅插入必需的用户记录）
INSERT IGNORE INTO users (id, username, password, nickname, email, phone, address, role) VALUES
(1, 'admin', 'admin123', '系统管理员', 'admin@ecommerce.com', '13800000000', '北京市朝阳区', 'ADMIN'),
(2, 'testuser', '123456', '测试用户', 'test@ecommerce.com', '13800000001', '上海市浦东新区', 'USER');

INSERT IGNORE INTO categories (id, name, description, parent_id, sort_order) VALUES
(1, '手机数码', '手机、平板等数码产品', NULL, 1);

INSERT IGNORE INTO products (id, name, description, price, original_price, stock, sales_count, image_url, brand, category_id, status) VALUES
(1, 'iPhone 15 Pro Max 256GB', '苹果最新旗舰手机', 9999.00, 10999.00, 100, 520, 'https://via.placeholder.com/400x400?text=iPhone15', 'Apple', 1, 'ON_SALE');

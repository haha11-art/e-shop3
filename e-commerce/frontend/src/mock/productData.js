/**
 * Mock 商品数据和分类数据
 * 用于前端独立测试，不依赖后端服务
 */

// 分类列表
export const mockCategories = [
  { id: 1, name: '手机数码', parentId: 0 },
  { id: 2, name: '电脑办公', parentId: 0 },
  { id: 3, name: '家用电器', parentId: 0 },
  { id: 4, name: '服饰鞋包', parentId: 0 },
  { id: 5, name: '食品生鲜', parentId: 0 },
  { id: 6, name: '图书文具', parentId: 0 }
]

// 商品列表（50条数据，覆盖各分类）
export const mockProducts = [
  // 手机数码 (categoryId: 1)
  { id: 1,  name: 'iPhone 15 Pro Max 256GB',     description: 'A17 Pro芯片，钛金属设计',     price: 9999,  originalPrice: 10999, categoryId: 1, imageUrl: 'https://picsum.photos/seed/p1/300/300',  salesCount: 5832 },
  { id: 2,  name: '华为 Mate 60 Pro',            description: '麒麟9000S，卫星通话',         price: 7999,  originalPrice: 8499,  categoryId: 1, imageUrl: 'https://picsum.photos/seed/p2/300/300',  salesCount: 4215 },
  { id: 3,  name: '小米14 Pro',                  description: '骁龙8 Gen3，徕卡影像',        price: 4999,  originalPrice: 5499,  categoryId: 1, imageUrl: 'https://picsum.photos/seed/p3/300/300',  salesCount: 3672 },
  { id: 4,  name: 'Samsung Galaxy S24 Ultra',    description: 'Galaxy AI，钛金属边框',       price: 8999,  originalPrice: 9999,  categoryId: 1, imageUrl: 'https://picsum.photos/seed/p4/300/300',  salesCount: 2105 },
  { id: 5,  name: 'OPPO Find X7 Ultra',          description: '双潜望长焦，哈苏影像',        price: 5999,  originalPrice: 6499,  categoryId: 1, imageUrl: 'https://picsum.photos/seed/p5/300/300',  salesCount: 1893 },
  { id: 6,  name: 'vivo X100 Pro',               description: '天玑9300，蔡司APO长焦',       price: 4999,  originalPrice: 5299,  categoryId: 1, imageUrl: 'https://picsum.photos/seed/p6/300/300',  salesCount: 1756 },
  { id: 7,  name: 'AirPods Pro 2',               description: '主动降噪，USB-C充电',         price: 1799,  originalPrice: 1999,  categoryId: 1, imageUrl: 'https://picsum.photos/seed/p7/300/300',  salesCount: 8920 },
  { id: 8,  name: '索尼 WH-1000XM5',             description: '行业领先降噪，30h续航',       price: 2299,  originalPrice: 2799,  categoryId: 1, imageUrl: 'https://picsum.photos/seed/p8/300/300',  salesCount: 3450 },

  // 电脑办公 (categoryId: 2)
  { id: 9,  name: 'MacBook Pro 14英寸 M3 Pro',    description: '18GB内存，512GB固态',         price: 14999, originalPrice: 15999, categoryId: 2, imageUrl: 'https://picsum.photos/seed/p9/300/300',  salesCount: 2341 },
  { id: 10, name: '联想 ThinkPad X1 Carbon',      description: '14英寸轻薄商务本',            price: 9999,  originalPrice: 11999, categoryId: 2, imageUrl: 'https://picsum.photos/seed/p10/300/300', salesCount: 1823 },
  { id: 11, name: '华为 MateBook X Pro',          description: '14.2英寸OLED触控屏',          price: 10999, originalPrice: 12999, categoryId: 2, imageUrl: 'https://picsum.photos/seed/p11/300/300', salesCount: 1456 },
  { id: 12, name: '戴尔 XPS 15',                  description: '15.6英寸4K OLED屏',           price: 12999, originalPrice: 14999, categoryId: 2, imageUrl: 'https://picsum.photos/seed/p12/300/300', salesCount: 982  },
  { id: 13, name: '罗技 MX Master 3S',            description: '旗舰无线鼠标，静音点击',      price: 699,   originalPrice: 799,   categoryId: 2, imageUrl: 'https://picsum.photos/seed/p13/300/300', salesCount: 6732 },
  { id: 14, name: 'Keychron K8 Pro 机械键盘',     description: '三模热插拔，Gateron轴',       price: 599,   originalPrice: 699,   categoryId: 2, imageUrl: 'https://picsum.photos/seed/p14/300/300', salesCount: 4521 },
  { id: 15, name: 'AOC U27P2U 4K显示器',          description: '27英寸IPS，Type-C 65W',       price: 2499,  originalPrice: 2999,  categoryId: 2, imageUrl: 'https://picsum.photos/seed/p15/300/300', salesCount: 2108 },
  { id: 16, name: 'iPad Air M2',                  description: '11英寸Liquid Retina屏',       price: 4799,  originalPrice: 4999,  categoryId: 2, imageUrl: 'https://picsum.photos/seed/p16/300/300', salesCount: 3215 },

  // 家用电器 (categoryId: 3)
  { id: 17, name: '戴森 V15 Detect 吸尘器',       description: '激光探测，LCD屏幕显示',       price: 4999,  originalPrice: 5999,  categoryId: 3, imageUrl: 'https://picsum.photos/seed/p17/300/300', salesCount: 2876 },
  { id: 18, name: '美的 变频空调 1.5匹',          description: '新一级能效，智能WiFi',        price: 3299,  originalPrice: 3999,  categoryId: 3, imageUrl: 'https://picsum.photos/seed/p18/300/300', salesCount: 5432 },
  { id: 19, name: '海尔 滚筒洗衣机 10kg',         description: '洗烘一体，蒸汽除菌',          price: 3999,  originalPrice: 4599,  categoryId: 3, imageUrl: 'https://picsum.photos/seed/p19/300/300', salesCount: 3210 },
  { id: 20, name: '飞利浦 空气净化器 AC3033',     description: 'HEPA滤网，除甲醛',            price: 2699,  originalPrice: 3199,  categoryId: 3, imageUrl: 'https://picsum.photos/seed/p20/300/300', salesCount: 1897 },
  { id: 21, name: '九阳 破壁机 L18-P631',         description: '智能预约，多功能料理',        price: 899,   originalPrice: 1299,  categoryId: 3, imageUrl: 'https://picsum.photos/seed/p21/300/300', salesCount: 7654 },
  { id: 22, name: '松下 微波炉 NN-GT37J',         description: '变频烧烤一体，23L',           price: 1299,  originalPrice: 1599,  categoryId: 3, imageUrl: 'https://picsum.photos/seed/p22/300/300', salesCount: 2345 },
  { id: 23, name: '格力 加湿器 SC50L',            description: '5L大容量，UV杀菌',            price: 399,   originalPrice: 499,   categoryId: 3, imageUrl: 'https://picsum.photos/seed/p23/300/300', salesCount: 4532 },
  { id: 24, name: '老板 抽油烟机 27A3H',          description: '大吸力侧吸，免拆洗',          price: 3599,  originalPrice: 4299,  categoryId: 3, imageUrl: 'https://picsum.photos/seed/p24/300/300', salesCount: 1876 },

  // 服饰鞋包 (categoryId: 4)
  { id: 25, name: '优衣库 轻薄羽绒服',            description: '90%白鸭绒，便携收纳',         price: 399,   originalPrice: 599,   categoryId: 4, imageUrl: 'https://picsum.photos/seed/p25/300/300', salesCount: 12345 },
  { id: 26, name: 'Nike Air Force 1',             description: '经典白色板鞋',               price: 799,   originalPrice: 899,   categoryId: 4, imageUrl: 'https://picsum.photos/seed/p26/300/300', salesCount: 8765 },
  { id: 27, name: '阿迪达斯 Ultraboost 23',       description: 'BOOST缓震，跑步鞋',           price: 1299,  originalPrice: 1499,  categoryId: 4, imageUrl: 'https://picsum.photos/seed/p27/300/300', salesCount: 5432 },
  { id: 28, name: 'Levi\'s 501 经典牛仔裤',        description: '直筒原色牛仔',               price: 599,   originalPrice: 799,   categoryId: 4, imageUrl: 'https://picsum.photos/seed/p28/300/300', salesCount: 6543 },
  { id: 29, name: 'Coach Tabby 单肩包',           description: '经典C字母扣，真皮',           price: 2999,  originalPrice: 3999,  categoryId: 4, imageUrl: 'https://picsum.photos/seed/p29/300/300', salesCount: 2109 },
  { id: 30, name: '新秀丽 拉杆箱 20寸',           description: 'PC材质，万向轮',              price: 899,   originalPrice: 1299,  categoryId: 4, imageUrl: 'https://picsum.photos/seed/p30/300/300', salesCount: 3421 },
  { id: 31, name: '卡西欧 G-SHOCK GA-2100',       description: '农家橡树，防震防水',          price: 899,   originalPrice: 999,   categoryId: 4, imageUrl: 'https://picsum.photos/seed/p31/300/300', salesCount: 4321 },
  { id: 32, name: 'Ray-Ban 飞行员太阳镜',         description: '经典金属框，偏光镜片',        price: 1299,  originalPrice: 1599,  categoryId: 4, imageUrl: 'https://picsum.photos/seed/p32/300/300', salesCount: 2876 },

  // 食品生鲜 (categoryId: 5)
  { id: 33, name: '三只松鼠 坚果大礼包',          description: '10袋装混合坚果',              price: 128,   originalPrice: 199,   categoryId: 5, imageUrl: 'https://picsum.photos/seed/p33/300/300', salesCount: 23456 },
  { id: 34, name: '农夫山泉 17.5°橙 3kg',         description: '赣南脐橙，新鲜直达',          price: 59,    originalPrice: 79,    categoryId: 5, imageUrl: 'https://picsum.photos/seed/p34/300/300', salesCount: 18765 },
  { id: 35, name: '蒙牛 纯甄酸奶 200g*10',        description: '原味发酵乳',                 price: 49,    originalPrice: 69,    categoryId: 5, imageUrl: 'https://picsum.photos/seed/p35/300/300', salesCount: 34567 },
  { id: 36, name: '良品铺子 猪肉脯 500g',         description: '原味蜜汁肉脯',               price: 69,    originalPrice: 89,    categoryId: 5, imageUrl: 'https://picsum.photos/seed/p36/300/300', salesCount: 15432 },
  { id: 37, name: '百草味 每日坚果 750g',         description: '30包独立小包装',              price: 99,    originalPrice: 149,   categoryId: 5, imageUrl: 'https://picsum.photos/seed/p37/300/300', salesCount: 21098 },
  { id: 38, name: '认养一头牛 纯牛奶 250ml*24',   description: '全脂灭菌乳',                 price: 79,    originalPrice: 99,    categoryId: 5, imageUrl: 'https://picsum.photos/seed/p38/300/300', salesCount: 16543 },
  { id: 39, name: '阳澄湖大闸蟹 8只装',           description: '公母各4只，鲜活发货',         price: 399,   originalPrice: 599,   categoryId: 5, imageUrl: 'https://picsum.photos/seed/p39/300/300', salesCount: 5432  },
  { id: 40, name: '奥利奥 巧克力威化 388g',       description: '夹心饼干礼盒装',              price: 29,    originalPrice: 39,    categoryId: 5, imageUrl: 'https://picsum.photos/seed/p40/300/300', salesCount: 28765 },

  // 图书文具 (categoryId: 6)
  { id: 41, name: '《深入理解Java虚拟机》第3版',  description: 'JVM经典著作',                 price: 109,   originalPrice: 149,   categoryId: 6, imageUrl: 'https://picsum.photos/seed/p41/300/300', salesCount: 8765  },
  { id: 42, name: '《算法导论》第4版',            description: 'MIT经典教材',                 price: 128,   originalPrice: 168,   categoryId: 6, imageUrl: 'https://picsum.photos/seed/p42/300/300', salesCount: 5432  },
  { id: 43, name: 'LAMY 凌美 Safari 钢笔',        description: '经典入门钢笔，EF尖',          price: 199,   originalPrice: 249,   categoryId: 6, imageUrl: 'https://picsum.photos/seed/p43/300/300', salesCount: 4321  },
  { id: 44, name: '国誉 B5活页笔记本',            description: '80张替芯+外壳',               price: 49,    originalPrice: 69,    categoryId: 6, imageUrl: 'https://picsum.photos/seed/p44/300/300', salesCount: 9876  },
  { id: 45, name: '百乐 P500 中性笔 12支装',      description: '0.5mm黑色，考试神器',         price: 59,    originalPrice: 79,    categoryId: 6, imageUrl: 'https://picsum.photos/seed/p45/300/300', salesCount: 15432 },
  { id: 46, name: '《三体》全集',                 description: '刘慈欣科幻巨著',              price: 89,    originalPrice: 129,   categoryId: 6, imageUrl: 'https://picsum.photos/seed/p46/300/300', salesCount: 23456 },
  { id: 47, name: '得力 桌面收纳盒',              description: '多格文具收纳架',              price: 39,    originalPrice: 59,    categoryId: 6, imageUrl: 'https://picsum.photos/seed/p47/300/300', salesCount: 6543  },
  { id: 48, name: '《人类简史》',                 description: '尤瓦尔·赫拉利著',             price: 49,    originalPrice: 68,    categoryId: 6, imageUrl: 'https://picsum.photos/seed/p48/300/300', salesCount: 18765 },
  { id: 49, name: '斑马 JJ15 荧光笔 5色套装',     description: '柔和色彩标记笔',              price: 29,    originalPrice: 39,    categoryId: 6, imageUrl: 'https://picsum.photos/seed/p49/300/300', salesCount: 11234 },
  { id: 50, name: '《代码整洁之道》',             description: 'Robert C. Martin著',          price: 79,    originalPrice: 99,    categoryId: 6, imageUrl: 'https://picsum.photos/seed/p50/300/300', salesCount: 7654  }
]

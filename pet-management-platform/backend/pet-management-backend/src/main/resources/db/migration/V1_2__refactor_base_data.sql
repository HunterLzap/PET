-- ==========================================
-- 基础数据表重构
-- ==========================================

-- 1. 备份旧数据
CREATE TABLE base_data_backup AS SELECT * FROM base_data;

-- 2. 创建新的基础数据类型表
CREATE TABLE base_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_code VARCHAR(50) UNIQUE NOT NULL COMMENT '字典编码：pet_species',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称：宠物种类',
    dict_level INT DEFAULT 1 COMMENT '层级：1-一级 2-二级',
    parent_code VARCHAR(50) COMMENT '父级编码',
    is_fixed TINYINT DEFAULT 0 COMMENT '是否固定不可新增：0-否 1-是',
    description VARCHAR(500) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    version INT DEFAULT 1 COMMENT '当前版本号',
    created_by VARCHAR(50) COMMENT '创建人',
    updated_by VARCHAR(50) COMMENT '修改人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_code(dict_code),
    INDEX idx_parent(parent_code)
) COMMENT='基础数据字典类型表';

-- 3. 创建基础数据值表
CREATE TABLE base_dict_value (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_code VARCHAR(50) NOT NULL COMMENT '所属字典编码',
    value_code VARCHAR(50) NOT NULL COMMENT '值编码：golden_retriever',
    value_name VARCHAR(100) NOT NULL COMMENT '值名称：金毛',
    value_order INT DEFAULT 0 COMMENT '排序',
    extra_data JSON COMMENT '附加属性{"origin":"苏格兰","size":"大型","priceRange":[50,100]}',
    color_tag VARCHAR(20) COMMENT '颜色标注 #409EFF',
    icon VARCHAR(100) COMMENT '图标URL',
    status TINYINT DEFAULT 1 COMMENT '状态：1-启用 0-禁用',
    version INT DEFAULT 1 COMMENT '当前版本号',
    created_by VARCHAR(50) COMMENT '创建人',
    updated_by VARCHAR(50) COMMENT '修改人',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dict_value(dict_code, value_code),
    INDEX idx_dict(dict_code),
    INDEX idx_status(status)
) COMMENT='基础数据字典值表';

-- 4. 修改版本表（关联新表）
ALTER TABLE base_data_version 
    ADD COLUMN dict_code VARCHAR(50) COMMENT '字典编码' AFTER base_data_id,
    ADD COLUMN value_code VARCHAR(50) COMMENT '值编码，为空表示整个字典的变更' AFTER dict_code,
    ADD INDEX idx_dict_version(dict_code, version);

-- 5. 修改日志表（关联新表）
ALTER TABLE base_data_log
    ADD COLUMN dict_code VARCHAR(50) COMMENT '字典编码' AFTER base_data_id,
    ADD COLUMN value_code VARCHAR(50) COMMENT '值编码' AFTER dict_code;

-- 6. 创建跨端映射配置表
CREATE TABLE base_dict_mapping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_code VARCHAR(50) NOT NULL,
    value_code VARCHAR(50) NOT NULL,
    terminal_type VARCHAR(20) NOT NULL COMMENT '终端类型：user_mobile/merchant_pc/admin_pc',
    display_name VARCHAR(100) COMMENT '显示名称',
    display_order INT DEFAULT 0 COMMENT '显示排序',
    is_visible TINYINT DEFAULT 1 COMMENT '是否可见：1-是 0-否',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_mapping(dict_code, value_code, terminal_type),
    INDEX idx_terminal(terminal_type)
) COMMENT='跨端显示映射表';

-- 7. 创建数据同步日志表
CREATE TABLE base_dict_sync_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_code VARCHAR(50) NOT NULL,
    version INT NOT NULL COMMENT '同步的版本号',
    sync_module VARCHAR(50) NOT NULL COMMENT '同步模块：user_pet/merchant_service',
    sync_status TINYINT DEFAULT 2 COMMENT '同步状态：0-失败 1-成功 2-进行中',
    error_msg TEXT COMMENT '错误信息',
    retry_count INT DEFAULT 0 COMMENT '重试次数',
    sync_started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sync_completed_at TIMESTAMP NULL,
    INDEX idx_dict_version(dict_code, version),
    INDEX idx_status(sync_status)
) COMMENT='基础数据同步日志表';

-- ==========================================
-- 初始化核心基础数据
-- ==========================================

-- 1. 初始化宠物基础数据
INSERT INTO base_dict_type (dict_code, dict_name, dict_level, parent_code, is_fixed, status, created_by) VALUES
('pet_species', '宠物种类', 1, NULL, 0, 1, 'system'),
('pet_breed', '宠物品种', 2, 'pet_species', 0, 1, 'system'),
('pet_gender', '宠物性别', 1, NULL, 1, 1, 'system'),
('pet_size', '宠物体型', 1, NULL, 0, 1, 'system');

-- 宠物种类值
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, status, created_by) VALUES
('pet_species', 'dog', '犬', 1, 1, 'system'),
('pet_species', 'cat', '猫', 2, 1, 'system'),
('pet_species', 'exotic', '异宠', 3, 1, 'system');

-- 犬类品种（示例）
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, extra_data, status, created_by) VALUES
('pet_breed', 'dog_golden_retriever', '金毛', 1, '{"species":"dog","origin":"苏格兰","size":"大型","weight_range":[27,34]}', 1, 'system'),
('pet_breed', 'dog_teddy', '泰迪', 2, '{"species":"dog","origin":"法国","size":"小型","weight_range":[3,6]}', 1, 'system'),
('pet_breed', 'dog_husky', '哈士奇', 3, '{"species":"dog","origin":"西伯利亚","size":"大型","weight_range":[20,27]}', 1, 'system'),
('pet_breed', 'dog_corgi', '柯基', 4, '{"species":"dog","origin":"英国","size":"中型","weight_range":[10,12]}', 1, 'system');

-- 猫类品种（示例）
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, extra_data, status, created_by) VALUES
('pet_breed', 'cat_ragdoll', '布偶', 1, '{"species":"cat","origin":"美国","size":"大型","weight_range":[4.5,9]}', 1, 'system'),
('pet_breed', 'cat_british_shorthair', '英短', 2, '{"species":"cat","origin":"英国","size":"中型","weight_range":[4,8]}', 1, 'system'),
('pet_breed', 'cat_scottish_fold', '苏格兰折耳', 3, '{"species":"cat","origin":"苏格兰","size":"中型","weight_range":[3.5,6]}', 1, 'system'),
('pet_breed', 'cat_persian', '波斯猫', 4, '{"species":"cat","origin":"伊朗","size":"中型","weight_range":[3,5.5]}', 1, 'system');

-- 宠物性别
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, status, created_by) VALUES
('pet_gender', 'male', '公', 1, 1, 'system'),
('pet_gender', 'female', '母', 2, 1, 'system'),
('pet_gender', 'neutered', '已绝育', 3, 1, 'system');

-- 宠物体型
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, extra_data, color_tag, status, created_by) VALUES
('pet_size', 'small', '小型', 1, '{"weight_range":[0,10],"unit":"kg"}', '#67C23A', 1, 'system'),
('pet_size', 'medium', '中型', 2, '{"weight_range":[10,25],"unit":"kg"}', '#E6A23C', 1, 'system'),
('pet_size', 'large', '大型', 3, '{"weight_range":[25,999],"unit":"kg"}', '#F56C6C', 1, 'system');

-- 2. 初始化服务基础数据
INSERT INTO base_dict_type (dict_code, dict_name, dict_level, parent_code, is_fixed, status, created_by) VALUES
('service_type', '服务类型', 1, NULL, 0, 1, 'system'),
('medical_service', '医疗服务', 2, 'service_type', 0, 1, 'system'),
('foster_service', '寄养服务', 2, 'service_type', 0, 1, 'system'),
('beauty_service', '美容服务', 2, 'service_type', 0, 1, 'system'),
('training_service', '训练服务', 2, 'service_type', 0, 1, 'system');

-- 医疗服务类型
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, extra_data, status, created_by) VALUES
('medical_service', 'consultation', '问诊', 1, '{"price_range":[50,200],"duration":30,"unit":"分钟"}', 1, 'system'),
('medical_service', 'checkup', '体检', 2, '{"price_range":[100,500],"duration":60,"unit":"分钟"}', 1, 'system'),
('medical_service', 'surgery', '手术', 3, '{"price_range":[500,5000],"duration":120,"unit":"分钟","requires":["surgery_level"]}', 1, 'system'),
('medical_service', 'vaccination', '疫苗接种', 4, '{"price_range":[80,300],"duration":20,"unit":"分钟"}', 1, 'system');

-- 寄养服务类型
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, extra_data, status, created_by) VALUES
('foster_service', 'day_care', '日托', 1, '{"price_range":[50,80],"time_range":"8:00-18:00","max_days":1}', 1, 'system'),
('foster_service', 'weekly', '周托', 2, '{"price_range":[300,500],"max_days":7}', 1, 'system'),
('foster_service', 'long_term', '长期寄养', 3, '{"price_range":[800,2000],"min_days":8}', 1, 'system');

-- 美容服务类型
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, extra_data, status, created_by) VALUES
('beauty_service', 'bath', '洗澡', 1, '{"price_range":[30,100],"duration":30,"unit":"分钟"}', 1, 'system'),
('beauty_service', 'haircut', '剪毛', 2, '{"price_range":[50,200],"duration":60,"unit":"分钟"}', 1, 'system'),
('beauty_service', 'styling', '造型', 3, '{"price_range":[100,300],"duration":90,"unit":"分钟"}', 1, 'system'),
('beauty_service', 'deworming', '驱虫', 4, '{"price_range":[50,150],"duration":20,"unit":"分钟"}', 1, 'system');

-- 3. 初始化订单基础数据
INSERT INTO base_dict_type (dict_code, dict_name, dict_level, parent_code, is_fixed, status, created_by) VALUES
('order_status', '订单状态', 1, NULL, 1, 1, 'system'),
('payment_status', '支付状态', 1, NULL, 1, 1, 'system');

-- 订单状态（固定枚举，配置流转规则）
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, extra_data, color_tag, status, created_by) VALUES
('order_status', 'wait_pay', '待支付', 1, '{"next_status":["paid","canceled"]}', '#909399', 1, 'system'),
('order_status', 'paid', '已支付', 2, '{"next_status":["wait_service","refunding"]}', '#409EFF', 1, 'system'),
('order_status', 'wait_service', '待服务', 3, '{"next_status":["completed","refunding"]}', '#E6A23C', 1, 'system'),
('order_status', 'completed', '已完成', 4, '{"next_status":[]}', '#67C23A', 1, 'system'),
('order_status', 'canceled', '已取消', 5, '{"next_status":[]}', '#909399', 1, 'system'),
('order_status', 'refunding', '退款中', 6, '{"next_status":["refunded"]}', '#E6A23C', 1, 'system'),
('order_status', 'refunded', '已退款', 7, '{"next_status":[]}', '#F56C6C', 1, 'system');

-- 支付状态
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, color_tag, status, created_by) VALUES
('payment_status', 'unpaid', '未支付', 1, '#909399', 1, 'system'),
('payment_status', 'paid', '支付成功', 2, '#67C23A', 1, 'system'),
('payment_status', 'failed', '支付失败', 3, '#F56C6C', 1, 'system');

-- 4. 初始化其他基础数据
INSERT INTO base_dict_type (dict_code, dict_name, dict_level, parent_code, is_fixed, status, created_by) VALUES
('merchant_type', '商家类型', 1, NULL, 0, 1, 'system'),
('vaccine_type', '疫苗类型', 1, NULL, 0, 1, 'system'),
('unit_type', '单位类型', 1, NULL, 1, 1, 'system');

-- 商家类型
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, status, created_by) VALUES
('merchant_type', 'hospital', '宠物医院', 1, 1, 'system'),
('merchant_type', 'pet_house', '宠物馆', 2, 1, 'system'),
('merchant_type', 'goods_merchant', '用品商家', 3, 1, 'system');

-- 疫苗类型
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, extra_data, status, created_by) VALUES
('vaccine_type', 'rabies', '狂犬疫苗', 1, '{"applicable_species":["dog","cat"],"protection_period":12,"unit":"月"}', 1, 'system'),
('vaccine_type', 'cat_triple', '猫三联', 2, '{"applicable_species":["cat"],"protection_period":12,"unit":"月"}', 1, 'system'),
('vaccine_type', 'dog_hexavalent', '犬六联', 3, '{"applicable_species":["dog"],"protection_period":12,"unit":"月"}', 1, 'system');

-- 单位类型（固定枚举）
INSERT INTO base_dict_value (dict_code, value_code, value_name, value_order, status, created_by) VALUES
('unit_type', 'kg', '千克', 1, 1, 'system'),
('unit_type', 'g', '克', 2, 1, 'system'),
('unit_type', 'day', '天', 3, 1, 'system'),
('unit_type', 'hour', '小时', 4, 1, 'system'),
('unit_type', 'minute', '分钟', 5, 1, 'system'),
('unit_type', 'km', '千米', 6, 1, 'system'),
('unit_type', 'm', '米', 7, 1, 'system'),
('unit_type', 'cny', '人民币', 8, 1, 'system');

-- ==========================================
-- 初始化跨端映射（示例）
-- ==========================================
-- 用户移动端显示简化名称
INSERT INTO base_dict_mapping (dict_code, value_code, terminal_type, display_name, display_order) VALUES
('pet_size', 'small', 'user_mobile', '小型(<10kg)', 1),
('pet_size', 'medium', 'user_mobile', '中型(10-25kg)', 2),
('pet_size', 'large', 'user_mobile', '大型(>25kg)', 3);

-- 平台管理端显示完整信息
INSERT INTO base_dict_mapping (dict_code, value_code, terminal_type, display_name, display_order) VALUES
('pet_size', 'small', 'admin_pc', '小型犬/猫（<10kg）', 1),
('pet_size', 'medium', 'admin_pc', '中型犬/猫（10-25kg）', 2),
('pet_size', 'large', 'admin_pc', '大型犬/猫（>25kg）', 3);

COMMIT;
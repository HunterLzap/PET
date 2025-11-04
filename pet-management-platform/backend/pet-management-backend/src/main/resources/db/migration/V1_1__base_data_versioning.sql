-- 基础数据表字段升级
ALTER TABLE `base_data`
  ADD COLUMN `version` INT NOT NULL DEFAULT 1 COMMENT '版本号',
  ADD COLUMN `disabled_at` DATETIME(6) NULL COMMENT '禁用时间',
  ADD COLUMN `created_by` VARCHAR(64) NULL COMMENT '创建人',
  ADD COLUMN `updated_by` VARCHAR(64) NULL COMMENT '修改人';

-- 版本表
CREATE TABLE `base_data_version` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `base_data_id` BIGINT(20) NOT NULL,
  `snapshot` TEXT NOT NULL COMMENT '该版本的数据内容(json)',
  `version` INT NOT NULL,
  `action` VARCHAR(32) NOT NULL COMMENT '操作类型(新增/编辑/禁用/导入/回滚)',
  `operated_by` VARCHAR(64) NOT NULL,
  `operated_at` DATETIME(6) NOT NULL,
  `remark` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  INDEX idx_basedata (`base_data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 日志表
CREATE TABLE `base_data_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `base_data_id` BIGINT(20) NOT NULL,
  `action` VARCHAR(32) NOT NULL,
  `detail` TEXT NULL COMMENT '变化摘要/diff或失败原因',
  `operated_by` VARCHAR(64) NOT NULL,
  `operated_at` DATETIME(6) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX idx_basedata (`base_data_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

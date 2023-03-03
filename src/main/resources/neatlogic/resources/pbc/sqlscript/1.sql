/*
Copyright(c) $today.year NeatLogic Co., Ltd. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
SET FOREIGN_KEY_CHECKS=0;

CREATE TABLE `pbc_branch_item`  (
  `branch_id` bigint NOT NULL COMMENT '批次ID',
  `item_id` bigint NOT NULL COMMENT '数据ID',
  PRIMARY KEY (`branch_id`, `item_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送批次关联报送数据表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_category`  (
  `id` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '一级分类标识符（两位）、二级分类标识符（两位）、三级分类标识符（三位）和四级分类标识符（三位）',
  `interface_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据元名称',
  `interface_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '数据元传输标识',
  `id_1` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '一级分类标识符',
  `name_1` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '一级分类',
  `id_2` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '二级分类标识符',
  `name_2` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '二级分类',
  `id_3` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '三级分类标识符',
  `name_3` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '三级分类',
  `id_4` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '四级分类标识符',
  `name_4` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '四级分类',
  `is_match` tinyint(1) NULL DEFAULT NULL COMMENT '报送是否符合要求',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_full_category`(`name_1`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送分类标识符表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_corporation`  (
  `id` bigint NOT NULL COMMENT '主键',
  `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '配置',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '名称',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送公司表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_enum`  (
  `property_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '人行属性ID',
  `value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '枚举属性值',
  `text` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '枚举属性显示',
  PRIMARY KEY (`property_id`, `value`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送属性枚举表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_interface`  (
  `uid` int NOT NULL COMMENT '数字id',
  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '人行提供的id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `customview_id` bigint NULL DEFAULT NULL COMMENT '自定义视图id',
  `ci_id` bigint NULL DEFAULT NULL COMMENT '模型id',
  `status` enum('','validating','reporting','mapping') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态',
  `user_id` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '同步用户',
  `action_time` timestamp(3) NULL DEFAULT NULL COMMENT '同步时间',
  `error` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '错误信息',
  `priority` enum('view','ci') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'ci' COMMENT '同步数据优先级',
  PRIMARY KEY (`uid`) USING BTREE,
  UNIQUE INDEX `uk_id`(`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送模型表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_interface_corporation`  (
  `interface_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接口id',
  `rule` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '规则',
  `corporation_id` bigint NOT NULL COMMENT '机构id',
  PRIMARY KEY (`interface_id`, `corporation_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送接口公司关联表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_interface_item`  (
  `id` bigint NOT NULL COMMENT '雪花id',
  `interface_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '接口id',
  `cientity_id` bigint NULL DEFAULT NULL COMMENT '配置项id',
  `ci_id` bigint NULL DEFAULT NULL COMMENT '模型id',
  `data` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '数据',
  `data_hash` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '内容哈希，用于检查是否有变化',
  `error` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '校验异常',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '添加日期',
  `fcu` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '添加人',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '更新日期',
  `lcu` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `is_new` tinyint(1) NULL DEFAULT NULL COMMENT '是否新数据',
  `is_imported` tinyint(1) NULL DEFAULT NULL COMMENT '是否已经上报过',
  `is_delete` tinyint(1) NULL DEFAULT NULL COMMENT '是否删除',
  `corporation_id` bigint NULL DEFAULT NULL COMMENT '机构id',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_interface_cientity_id`(`interface_id`, `cientity_id`, `corporation_id`) USING BTREE,
  INDEX `idx_corporation_id`(`corporation_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送模型数据表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_policy`  (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '说明',
  `is_active` tinyint(1) NULL DEFAULT NULL COMMENT '是否激活',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '定时策略',
  `last_exec_date` timestamp(3) NULL DEFAULT NULL COMMENT '最后一次执行时间',
  `input_from` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发起方式',
  `phase` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '范例:sync,collect',
  `config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'json格式，额外配置',
  `corporation_id` bigint NOT NULL COMMENT '机构id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送策略表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_policy_audit`  (
  `id` bigint NOT NULL COMMENT '主键',
  `policy_id` bigint NULL DEFAULT NULL COMMENT '策略id',
  `start_time` timestamp(3) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp(3) NULL DEFAULT NULL COMMENT '结束时间',
  `status` enum('running','failed','success','pending') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态',
  `user_id` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发起用户',
  `server_id` int NULL DEFAULT NULL COMMENT '机器id',
  `error` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '异常',
  `input_from` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发起方式',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_policy_id`(`policy_id`) USING BTREE,
  INDEX `idx_end_time`(`end_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送策略执行记录表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_policy_audit_interfaceitem`  (
  `audit_id` bigint NOT NULL COMMENT '策略执行记录ID',
  `interfaceitem_id` bigint NOT NULL COMMENT '模型数据ID',
  `action` enum('new','update','delete') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '动作',
  PRIMARY KEY (`audit_id`, `interfaceitem_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送策略执行记录关联报送数据表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_policy_interface`  (
  `policy_id` bigint NOT NULL COMMENT '策略id',
  `interface_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接口id',
  PRIMARY KEY (`policy_id`, `interface_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送策略关联模型表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_policy_phase`  (
  `id` bigint NOT NULL COMMENT '自增id',
  `audit_id` bigint NULL DEFAULT NULL COMMENT '记录ID',
  `phase` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '阶段',
  `status` enum('running','success','failed','aborted','pending') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '状态',
  `start_time` timestamp(3) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` timestamp(3) NULL DEFAULT NULL COMMENT '结束时间',
  `result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '处理结果',
  `error` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '异常',
  `sort` tinyint NULL DEFAULT NULL COMMENT '排序',
  `exec_count` int NULL DEFAULT NULL COMMENT '执行次数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk`(`audit_id`, `phase`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送策略执行阶段表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_property`  (
  `uid` int NOT NULL COMMENT 'global id',
  `id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '属性标识',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性名称',
  `complex_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复合属性标识',
  `complex_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复合属性名称',
  `interface_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接口id',
  `data_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '数据类型',
  `value_range` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '值域',
  `restraint` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '约束条件',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '备注',
  `definition` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '定义',
  `is_key` tinyint(1) NULL DEFAULT NULL COMMENT '是否接口属性，一个接口只有一个',
  `example` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '示例',
  PRIMARY KEY (`uid`) USING BTREE,
  UNIQUE INDEX `uk_id`(`id`, `complex_id`, `interface_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送模型属性表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_property_mapping`  (
  `property_uid` bigint NOT NULL COMMENT 'pbc_property表的主键',
  `interface_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '模型ID',
  `complex_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '复合属性ID',
  `property_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '属性ID',
  `mapping` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '映射配置',
  `transfer_rule` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '转换规则',
  `default_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '默认值',
  PRIMARY KEY (`property_uid`) USING BTREE,
  INDEX `idx_interface_id`(`interface_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送属性映射表' ROW_FORMAT = Dynamic;

CREATE TABLE `pbc_property_rel`  (
  `id` bigint NOT NULL COMMENT '自增id',
  `from_property_uid` bigint NULL DEFAULT NULL COMMENT '上游属性uid',
  `to_interface_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '关联接口id',
  `to_value_property_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '值属性uid',
  `to_text_property_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '显示文本uid',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk`(`from_property_uid`, `to_interface_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '监管报送属性关联表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS=1;
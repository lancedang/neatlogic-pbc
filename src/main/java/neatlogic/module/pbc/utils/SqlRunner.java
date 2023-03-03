/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.pbc.utils;

import neatlogic.framework.pbc.dao.mapper.EnumMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Title: SqlRunner
 * @Package neatlogic.module.pbc.utils
 * @Description: 数据执行类，支持在任意地方调用执行
 * @Author: yangy
 * @Date: 2021/7/20 17:16
 **/
@Service
public class SqlRunner {
    private static final Map<String, Integer> propertyEnumMap = new HashMap<>();

    private static SqlRunner sqlRunner;

    @PostConstruct
    public void init() {
        sqlRunner = this;
    }

    @Resource
    private EnumMapper enumMapper;


    public static boolean checkPropertyHasEnum(String propertyId) {
        if (!propertyEnumMap.containsKey(propertyId)) {
            propertyEnumMap.put(propertyId, sqlRunner.enumMapper.checkPropertyIdHasEnum(propertyId));
        }
        return propertyEnumMap.get(propertyId) > 0;
    }


    public static String getEnumText(String propertyId, String value) {
        if (StringUtils.isNotBlank(propertyId) && StringUtils.isNotBlank(value)) {
            String t = sqlRunner.enumMapper.getEnumText(propertyId, value);
            if (StringUtils.isBlank(t)) {
                return value;
            } else {
                return t;
            }
        }
        return "";
    }
}

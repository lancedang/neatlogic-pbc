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

import neatlogic.framework.pbc.dto.InterfaceItemVo;
import neatlogic.framework.pbc.dto.PropertyVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class InterfaceItemUtil {
    /**
     * 验证数据是否符合上传要求
     *
     * @param interfaceItemVo 数据
     * @param propertyList    属性列表
     */
    public static void validData(InterfaceItemVo interfaceItemVo, List<PropertyVo> propertyList) {
        JSONObject dataObj = interfaceItemVo.getData();
        JSONObject errorMap = new JSONObject();
        for (PropertyVo propertyVo : propertyList) {
            if (StringUtils.isBlank(propertyVo.getComplexId())) {
                //简单属性校验
                if (StringUtils.isBlank(dataObj.getString(propertyVo.getId()))) {
                    if (propertyVo.getRestraint().equalsIgnoreCase("M")) {
                        errorMap.put(propertyVo.getId(), "属性：" + propertyVo.getName() + "(" + propertyVo.getId() + ")不能为空");
                    }
                } else {
                    String error = ValueRangeValidator.build(propertyVo.getValueRange()).valid(dataObj.getString(propertyVo.getId()));
                    if (StringUtils.isNotBlank(error)) {
                        errorMap.put(propertyVo.getId(), "属性：" + propertyVo.getName() + "(" + propertyVo.getId() + ")格式错误，" + error);
                    }
                }
            } else {
                //复合属性校验
                JSONArray dataList = dataObj.getJSONArray(propertyVo.getComplexId());
                if (CollectionUtils.isNotEmpty(dataList)) {
                    for (int i = 0; i < dataList.size(); i++) {
                        JSONObject pObj = dataList.getJSONObject(i);
                        if (StringUtils.isBlank(pObj.getString(propertyVo.getId()))) {
                            if (propertyVo.getRestraint().equalsIgnoreCase("M")) {
                                String errMsg = "属性：" + propertyVo.getComplexName() + "-" + propertyVo.getName() + "(" + propertyVo.getComplexId() + "-" + propertyVo.getId() + ")不能为空";
                                if (!errorMap.containsKey(propertyVo.getComplexId())) {
                                    errorMap.put(propertyVo.getComplexId(), new JSONObject());
                                }
                                if (!errorMap.getJSONObject(propertyVo.getComplexId()).containsKey(Integer.toString(i))) {
                                    errorMap.getJSONObject(propertyVo.getComplexId()).put(Integer.toString(i), new JSONObject());
                                }
                                errorMap.getJSONObject(propertyVo.getComplexId()).getJSONObject(Integer.toString(i)).put(propertyVo.getId(), errMsg);
                            }
                        } else {
                            String error = ValueRangeValidator.build(propertyVo.getValueRange()).valid(pObj.getString(propertyVo.getId()));
                            if (StringUtils.isNotBlank(error)) {
                                String errMsg = "属性：" + propertyVo.getComplexName() + "-" + propertyVo.getName() + "(" + propertyVo.getComplexId() + "-" + propertyVo.getId() + ")格式错误，" + error;
                                if (!errorMap.containsKey(propertyVo.getComplexId())) {
                                    errorMap.put(propertyVo.getComplexId(), new JSONObject());
                                }
                                if (!errorMap.getJSONObject(propertyVo.getComplexId()).containsKey(Integer.toString(i))) {
                                    errorMap.getJSONObject(propertyVo.getComplexId()).put(Integer.toString(i), new JSONObject());
                                }
                                errorMap.getJSONObject(propertyVo.getComplexId()).getJSONObject(Integer.toString(i)).put(propertyVo.getId(), errMsg);
                            }
                        }
                    }
                } else {
                    if (propertyVo.getRestraint().equalsIgnoreCase("M")) {
                        errorMap.put(propertyVo.getComplexId(), "属性：" + propertyVo.getComplexName() + "(" + propertyVo.getComplexId() + ")不能为空");
                    }
                }
            }
        }
        if (MapUtils.isNotEmpty(errorMap)) {
            interfaceItemVo.setError(errorMap);
        } else {
            interfaceItemVo.setError(null);
        }
    }
}

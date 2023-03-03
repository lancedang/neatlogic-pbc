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

package neatlogic.framework.pbc.dto;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.restful.annotation.EntityField;

import java.util.List;

public class PolicyConfigVo {
    @EntityField(name = "字段名", type = ApiParamType.STRING)
    private String key;
    @EntityField(name = "数据类型", type = ApiParamType.STRING)
    private String type;
    @EntityField(name = "字段名称", type = ApiParamType.STRING)
    private String label;
    @EntityField(name = "说明", type = ApiParamType.STRING)
    private String description;
    @EntityField(name = "是否必填", type = ApiParamType.INTEGER)
    private Integer isRequired = 0;
    @EntityField(name = "选项列表", type = ApiParamType.JSONARRAY)
    private List<ValueTextVo> dataList;

    public PolicyConfigVo(String key, String label, String type) {
        this.key = key;
        this.label = label;
        this.type = type;
    }

    public PolicyConfigVo(String key, String label, String type, String description) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.description = description;
    }

    public PolicyConfigVo(String key, String label, String type, String description, Integer isRequired) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.description = description;
        this.isRequired = isRequired;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Integer isRequired) {
        this.isRequired = isRequired;
    }

    public PolicyConfigVo(String key, String label, String type, String description, List<ValueTextVo> dataList) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.description = description;
        this.dataList = dataList;
    }

    public PolicyConfigVo(String key, String label, String type, List<ValueTextVo> dataList) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.dataList = dataList;
    }

    public PolicyConfigVo(String key, String label, String type, String description, Integer isRequired, List<ValueTextVo> dataList) {
        this.key = key;
        this.label = label;
        this.type = type;
        this.description = description;
        this.isRequired = isRequired;
        this.dataList = dataList;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<ValueTextVo> getDataList() {
        return dataList;
    }

    public void setDataList(List<ValueTextVo> dataList) {
        this.dataList = dataList;
    }
}

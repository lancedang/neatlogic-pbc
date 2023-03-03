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
import neatlogic.framework.restful.annotation.EntityField;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

public class InterfaceCorporationVo {
    @EntityField(name = "接口id", type = ApiParamType.STRING)
    private String interfaceId;
    @JSONField(serialize = false)
    private String ruleStr;
    @EntityField(name = "规则", type = ApiParamType.JSONOBJECT)
    private JSONObject rule;
    @EntityField(name = "机构id", type = ApiParamType.LONG)
    private Long corporationId;
    @EntityField(name = "机构名称", type = ApiParamType.STRING)
    private String corporationName;

    public String getInterfaceId() {
        return interfaceId;
    }

    public String getCorporationName() {
        return corporationName;
    }

    public void setCorporationName(String corporationName) {
        this.corporationName = corporationName;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getRuleStr() {
        if (StringUtils.isBlank(ruleStr) && rule != null) {
            ruleStr = rule.toString();
        }
        return ruleStr;
    }

    public void setRuleStr(String ruleStr) {
        this.ruleStr = ruleStr;
    }

    public JSONObject getRule() {
        if (rule == null && StringUtils.isNotBlank(ruleStr)) {
            try {
                rule = JSONObject.parseObject(ruleStr);
            } catch (Exception ignored) {

            }
        }
        return rule;
    }

    public void setRule(JSONObject rule) {
        this.rule = rule;
    }

    public Long getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(Long corporationId) {
        this.corporationId = corporationId;
    }
}

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

package neatlogic.module.pbc.api.property;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.EnumVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class SaveEnumApi extends PrivateApiComponentBase {

    @Autowired
    PropertyMapper propertyMapper;

    @Override
    public String getToken() {
        return "/pbc/enum/save";
    }

    @Override
    public String getName() {
        return "保存人行属性接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "propertyId", type = ApiParamType.STRING, isRequired = true, desc = "属性id"), @Param(name = "enumList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "枚举列表，成员需要是json格式，包含value和text两个属性")})
    @Description(desc = "保存人行属性接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        propertyMapper.deleteEnumByPropertyId(paramObj.getString("propertyId"));
        JSONArray enumList = paramObj.getJSONArray("enumList");
        String propertyId = paramObj.getString("propertyId");
        if (CollectionUtils.isNotEmpty(enumList)) {
            for (int i = 0; i < enumList.size(); i++) {
                JSONObject enumObj = enumList.getJSONObject(i);
                EnumVo enumVo = JSONObject.toJavaObject(enumObj, EnumVo.class);
                if (StringUtils.isNotBlank(enumVo.getValue()) && StringUtils.isNotBlank(enumVo.getText())) {
                    enumVo.setPropertyId(propertyId);
                    propertyMapper.insertEnum(enumVo);
                }
            }
        }
        return null;

    }

}

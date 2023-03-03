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
import neatlogic.framework.pbc.dto.PropertyVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class UpdatePropertySortApi extends PrivateApiComponentBase {

    @Autowired
    PropertyMapper propertyMapper;

    @Override
    public String getToken() {
        return "/pbc/property/updatesort";
    }

    @Override
    public String getName() {
        return "修改接口属性排序";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "propertyList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "属性列表", help = "成员需要包含id,interfaceId,complexId三个属性")})
    @Description(desc = "修改接口属性排序接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONArray propertyList = paramObj.getJSONArray("propertyList");
        for (int i = 0; i < propertyList.size(); i++) {
            JSONObject propertyObj = propertyList.getJSONObject(i);
            PropertyVo propertyVo = JSONObject.toJavaObject(propertyObj, PropertyVo.class);
            propertyVo.setSort(i + 1);
            propertyMapper.updatePropertySort(propertyVo);
        }
        return null;
    }

}

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
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.PropertyRelVo;
import neatlogic.framework.pbc.dto.PropertyVo;
import neatlogic.framework.pbc.exception.PropertyRelIsExistsException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SavePropertyRelApi extends PrivateApiComponentBase {

    @Autowired
    PropertyMapper propertyMapper;

    @Override
    public String getToken() {
        return "/pbc/property/rel/save";
    }

    @Override
    public String getName() {
        return "保存属性关系";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uid", type = ApiParamType.LONG, desc = "uid", isRequired = true),
            @Param(name = "relList", type = ApiParamType.JSONARRAY, desc = "关系数组")})
    @Description(desc = "保存属性关系接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        PropertyVo propertyVo = JSONObject.toJavaObject(paramObj, PropertyVo.class);
        propertyMapper.deletePropertyRelByFromPropertyUid(propertyVo.getUid());
        if (CollectionUtils.isNotEmpty(propertyVo.getRelList())) {
            for (PropertyRelVo propertyRelVo : propertyVo.getRelList()) {
                propertyRelVo.setFromPropertyUid(propertyVo.getUid());
                if (propertyMapper.checkPropertyRelIsExists(propertyRelVo) > 0) {
                    throw new PropertyRelIsExistsException(propertyRelVo);
                }
                propertyMapper.insertPropertyRel(propertyRelVo);
            }
        }
        return null;
    }

}

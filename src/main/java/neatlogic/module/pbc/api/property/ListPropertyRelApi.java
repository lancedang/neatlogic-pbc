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
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.PropertyVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListPropertyRelApi extends PrivateApiComponentBase {

    @Autowired
    PropertyMapper propertyMapper;

    @Override
    public String getToken() {
        return "/pbc/property/rel/list";
    }

    @Override
    public String getName() {
        return "获取属性关系列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uid", type = ApiParamType.LONG, isRequired = true, desc = "uid")})
    @Output({@Param(explode = PropertyVo.class)})
    @Description(desc = "获取属性关系列表接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        return propertyMapper.getPropertyRelByFromPropertyUid(paramObj.getLong("uid"));
    }
}

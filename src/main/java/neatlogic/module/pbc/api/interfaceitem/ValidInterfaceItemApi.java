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

package neatlogic.module.pbc.api.interfaceitem;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.InterfaceItemVo;
import neatlogic.framework.pbc.dto.PropertyVo;
import neatlogic.framework.pbc.exception.InterfaceItemNotFoundException;
import neatlogic.module.pbc.utils.InterfaceItemUtil;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class ValidInterfaceItemApi extends PrivateApiComponentBase {


    @Resource
    private InterfaceItemMapper interfaceItemMapper;

    @Resource
    private PropertyMapper propertyMapper;

    @Override
    public String getToken() {
        return "/pbc/interfaceitem/valid";
    }

    @Override
    public String getName() {
        return "校验单条接口数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "数据id")})
    @Description(desc = "校验单条接口数据接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        InterfaceItemVo interfaceItemVo = interfaceItemMapper.getInterfaceItemById(paramObj.getLong("id"));
        if (interfaceItemVo == null) {
            throw new InterfaceItemNotFoundException(paramObj.getLong("id"));
        }
        List<PropertyVo> propertyList = propertyMapper.getPropertyByInterfaceId(interfaceItemVo.getInterfaceId());
        InterfaceItemUtil.validData(interfaceItemVo, propertyList);
        interfaceItemMapper.updateInterfaceItemError(interfaceItemVo);
        return null;
    }


}

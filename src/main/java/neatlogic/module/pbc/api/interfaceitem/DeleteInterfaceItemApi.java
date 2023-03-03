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
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dto.InterfaceItemVo;
import neatlogic.framework.pbc.exception.InterfaceItemCanNotDeleteException;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.DELETE)
public class DeleteInterfaceItemApi extends PrivateApiComponentBase {


    @Resource
    InterfaceItemMapper interfaceItemMapper;

    @Override
    public String getToken() {
        return "/pbc/interfaceitem/delete";
    }

    @Override
    public String getName() {
        return "删除单条接口数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "数据id")})
    @Description(desc = "删除单条接口数据接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long id = paramObj.getLong("id");
        InterfaceItemVo interfaceItemVo = interfaceItemMapper.getInterfaceItemById(id);
        if (interfaceItemVo != null) {
            if (interfaceItemVo.getIsImported() == 0) {
                interfaceItemMapper.deleteInterfaceItemById(id);
            } else {
                throw new InterfaceItemCanNotDeleteException(id);
            }
        }
        return null;
    }


}

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
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetInterfaceItemListApi extends PrivateApiComponentBase {


    @Resource
    InterfaceItemMapper interfaceItemMapper;

    @Override
    public String getToken() {
        return "/pbc/interfaceitem/getlist";
    }

    @Override
    public String getName() {
        return "获取多条接口数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "idList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "数据id列表")})
    @Description(desc = "获取多条接口数据接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<Long> idList = paramObj.getJSONArray("idList").stream().map(d -> Long.parseLong(d.toString())).collect(Collectors.toList());
        return interfaceItemMapper.getInterfaceItemByIdList(idList);
    }


}

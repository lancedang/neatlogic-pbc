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

package neatlogic.module.pbc.api.interfacemanage;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dao.mapper.InterfaceMapper;
import neatlogic.framework.pbc.dto.InterfaceVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ListAllInterfaceApi extends PrivateApiComponentBase {

    @Resource
    private InterfaceMapper interfaceMapper;

    @Resource
    private InterfaceItemMapper interfaceItemMapper;

    @Override
    public String getToken() {
        return "/pbc/interface/listall";
    }

    @Override
    public String getName() {
        return "获取所有接口列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "needItemCount", type = ApiParamType.INTEGER, desc = "是否需要返回数据数量")})
    @Output({@Param(explode = InterfaceVo[].class)})
    @Description(desc = "获取所有接口列表接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<InterfaceVo> interfaceList = interfaceMapper.getAllInterfaceList();
        if (paramObj.getIntValue("needItemCount") == 1) {
            for (InterfaceVo interfaceVo : interfaceList) {
                interfaceVo.setItemCount(interfaceItemMapper.getInterfaceItemCountByInterfaceId(interfaceVo.getId()));
            }
        }
        return interfaceList;
    }
}

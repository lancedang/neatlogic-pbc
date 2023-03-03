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

package neatlogic.module.pbc.api.corporation;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.CorporationMapper;
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dto.CorporationVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchCorporationApi extends PrivateApiComponentBase {

    @Resource
    private CorporationMapper corporationMapper;

    @Resource
    private InterfaceItemMapper interfaceItemMapper;

    @Override
    public String getName() {
        return "获取机构列表";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Description(desc = "获取机构列表接口")
    @Input({@Param(name = "needItemCount", type = ApiParamType.BOOLEAN, desc = "是否需要返回数据量"),
            @Param(name = "interfaceId", type = ApiParamType.STRING, desc = "接口id，当needItemCount为true时才生效")})
    @Output({@Param(explode = CorporationVo[].class)})
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<CorporationVo> corporationList = corporationMapper.searchCorporation();
        String interfaceId = paramObj.getString("interfaceId");
        if (paramObj.getBooleanValue("needItemCount")) {
            for (CorporationVo corporationVo : corporationList) {
                corporationVo.setItemCount(interfaceItemMapper.getItemCountByCorporationId(corporationVo.getId(), interfaceId));
            }
        }
        return corporationList;
    }

    @Override
    public String getToken() {
        return "/pbc/corporation/search";
    }
}

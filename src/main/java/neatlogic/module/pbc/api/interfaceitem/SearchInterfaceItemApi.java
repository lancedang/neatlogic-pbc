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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dto.InterfaceItemVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchInterfaceItemApi extends PrivateApiComponentBase {


    @Resource
    InterfaceItemMapper interfaceItemMapper;

    @Override
    public String getToken() {
        return "/pbc/interfaceitem/search";
    }

    @Override
    public String getName() {
        return "搜索人行模型数据列表接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "interfaceId", type = ApiParamType.STRING, isRequired = true, desc = "人行模型ID"),
            @Param(name = "corporationId", type = ApiParamType.LONG, isRequired = true, desc = "机构id"),
            @Param(name = "status", type = ApiParamType.STRING, desc = "人行数据状态"),
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数量"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")})
    @Output({@Param(name = "interfaceItemList", explode = InterfaceItemVo[].class),
            @Param(explode = BasePageVo.class)})
    @Description(desc = "搜索人行模型数据列表接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        InterfaceItemVo interfaceItemVo = JSONObject.toJavaObject(paramObj, InterfaceItemVo.class);
        int rowNum = interfaceItemMapper.searchInterfaceItemCount(interfaceItemVo);
        interfaceItemVo.setRowNum(rowNum);
        interfaceItemVo.setPageCount(PageUtil.getPageCount(rowNum, interfaceItemVo.getPageSize()));
        return TableResultUtil.getResult(interfaceItemMapper.searchInterfaceItem(interfaceItemVo), interfaceItemVo);
    }


}

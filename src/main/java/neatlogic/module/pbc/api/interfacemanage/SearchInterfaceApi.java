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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.InterfaceMapper;
import neatlogic.framework.pbc.dto.InterfaceVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchInterfaceApi extends PrivateApiComponentBase {

    @Resource
    private InterfaceMapper interfaceMapper;

    @Override
    public String getToken() {
        return "/pbc/interface/search";
    }

    @Override
    public String getName() {
        return "查询人行接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true),
            @Param(name = "hasCi", type = ApiParamType.INTEGER, desc = "是否关联配置项项模型"),
            @Param(name = "hasCustomView", type = ApiParamType.INTEGER, desc = "是否关联自定义视图"),
            @Param(name = "defaultValue", type = ApiParamType.JSONARRAY, desc = "默认值，用于下拉回显")})
    @Output({@Param(explode = InterfaceVo[].class), @Param(explode = BasePageVo.class)})
    @Description(desc = "查询人行接口接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        InterfaceVo interfaceVo = JSONObject.toJavaObject(paramObj, InterfaceVo.class);
        int rowNum = interfaceMapper.searchInterfaceCount(interfaceVo);
        interfaceVo.setRowNum(rowNum);
        interfaceVo.setPageCount(PageUtil.getPageCount(rowNum, interfaceVo.getPageSize()));
        return TableResultUtil.getResult(interfaceMapper.searchInterface(interfaceVo), interfaceVo);
    }
}

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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.PropertyVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchPropertyApi extends PrivateApiComponentBase {

    @Autowired
    private PropertyMapper propertyMapper;

    @Override
    public String getToken() {
        return "/pbc/property/search";
    }

    @Override
    public String getName() {
        return "查询接口属性";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true),
            @Param(name = "interfaceId", type = ApiParamType.STRING, desc = "接口id", xss = true),
            @Param(name = "valueRange", type = ApiParamType.STRING, desc = "值域", xss = true),
            @Param(name = "dataType", type = ApiParamType.STRING, desc = "数据类型", xss = true),
            @Param(name = "restraint", type = ApiParamType.STRING, desc = "约束", xss = true)})
    @Output({@Param(name = "tbodyList", explode = PropertyVo[].class), @Param(explode = BasePageVo.class)})
    @Description(desc = "查询接口属性接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        PropertyVo propertyVo = JSONObject.toJavaObject(paramObj, PropertyVo.class);
        int rowNum = propertyMapper.searchPropertyCount(propertyVo);
        propertyVo.setPageCount(PageUtil.getPageCount(rowNum, propertyVo.getPageSize()));
        propertyVo.setRowNum(rowNum);
        return TableResultUtil.getResult(propertyMapper.searchProperty(propertyVo), propertyVo);
    }

}

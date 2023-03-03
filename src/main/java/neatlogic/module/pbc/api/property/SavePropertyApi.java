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
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.PropertyVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class SavePropertyApi extends PrivateApiComponentBase {

    @Autowired
    PropertyMapper propertyMapper;

    @Override
    public String getToken() {
        return "/pbc/property/save";
    }

    @Override
    public String getName() {
        return "保存人行属性接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "uid", type = ApiParamType.LONG, desc = "uid，不存在代表添加"),
            @Param(name = "id", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "属性传输标识"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "属性名称"),
            @Param(name = "interfaceId", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "采集接口"),
            @Param(name = "complexId", type = ApiParamType.STRING, xss = true, desc = "复合属性传输标识"),
            @Param(name = "complexName", type = ApiParamType.STRING, xss = true, desc = "复合属性名称"),
            @Param(name = "alias", type = ApiParamType.STRING, xss = true, desc = "别名"),
            @Param(name = "dataType", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "数据类型"),
            @Param(name = "valueRange", type = ApiParamType.STRING, xss = true, desc = "值域"),
            @Param(name = "restraint", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "约束条件"),
            @Param(name = "definition", type = ApiParamType.STRING, xss = true, desc = "定义"),
            @Param(name = "description", type = ApiParamType.STRING, xss = true, desc = "说明")})
    @Description(desc = "保存人行属性接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        //Long uid = paramObj.getLong("uid");
        PropertyVo propertyVo = JSONObject.toJavaObject(paramObj, PropertyVo.class);
        /*if (propertyMapper.checkPropertyIsExists(propertyVo.getInterfaceId(), propertyVo.getId(), propertyVo.getComplexId()) > 0) {
            throw new PropertyIsExistsException(propertyVo.getId(), propertyVo.getComplexId());
        }*/
        Integer sort = propertyMapper.getPropertyMaxSortByInterfaceId(propertyVo.getInterfaceId());
        //if (uid == null) {
        if (sort != null) {
            propertyVo.setSort(sort + 1);
        } else {
            propertyVo.setSort(1);
        }
        propertyMapper.insertProperty(propertyVo);
        //} else {
        //  propertyMapper.updatePropertyByUid(propertyVo);
        //}
        return null;
    }

}

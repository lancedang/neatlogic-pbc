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
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.InterfaceMapper;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.InterfaceCorporationVo;
import neatlogic.framework.pbc.dto.InterfaceVo;
import neatlogic.framework.pbc.dto.PropertyVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
@Transactional
public class SaveInterfaceMappingApi extends PrivateApiComponentBase {

    @Resource
    private InterfaceMapper interfaceMapper;

    @Resource
    private PropertyMapper propertyMapper;

    @Override
    public String getToken() {
        return "/pbc/interface/mapping/save";
    }

    @Override
    public String getName() {
        return "保存属性映射关系";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "customViewId", type = ApiParamType.LONG, desc = "自定义视图id"),
            @Param(name = "ciId", type = ApiParamType.LONG, desc = "模型id"),
            @Param(name = "priority", type = ApiParamType.ENUM, rule = "ci,view", desc = "优先级"),
            @Param(name = "corporationRuleList", type = ApiParamType.JSONARRAY, desc = "机构规则列表"),
            @Param(name = "propertyList", type = ApiParamType.JSONARRAY, desc = "属性列表，包含uid,attrId,customViewAttrUuid属性")})
    @Description(desc = "保存属性映射关系接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        InterfaceVo interfaceVo = JSONObject.toJavaObject(paramObj, InterfaceVo.class);
        if (CollectionUtils.isNotEmpty(interfaceVo.getPropertyList())) {
            //清除没有任何配置的数据
            interfaceVo.getPropertyList().removeIf(d -> MapUtils.isEmpty(d.getMapping()) && StringUtils.isBlank(d.getTransferRule()) && StringUtils.isBlank(d.getPropDefaultValue()));
        }
        interfaceMapper.updateInterfaceMapping(interfaceVo);
        propertyMapper.deletePropertyMappingByInterfaceId(interfaceVo.getId());
        if (CollectionUtils.isNotEmpty(interfaceVo.getPropertyList())) {
            for (PropertyVo propertyVo : interfaceVo.getPropertyList()) {
                propertyMapper.insertPropertyMapping(propertyVo);
            }
        }
        interfaceMapper.deleteCorporationRuleByInterfaceId(interfaceVo.getId());
        if (CollectionUtils.isNotEmpty(interfaceVo.getCorporationRuleList())) {
            for (InterfaceCorporationVo rule : interfaceVo.getCorporationRuleList()) {
                interfaceMapper.insertCorporationRule(rule);
            }
        }
        return null;
    }

}

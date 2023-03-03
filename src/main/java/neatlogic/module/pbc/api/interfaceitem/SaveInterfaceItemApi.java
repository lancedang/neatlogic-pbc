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

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.Md5Util;
import neatlogic.framework.util.UuidUtil;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.InterfaceItemVo;
import neatlogic.framework.pbc.dto.PropertyVo;
import neatlogic.module.pbc.utils.InterfaceItemUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


@Service
@OperationType(type = OperationTypeEnum.UPDATE)
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
public class SaveInterfaceItemApi extends PrivateApiComponentBase {

    private static final Logger logger = LoggerFactory.getLogger(SaveInterfaceItemApi.class);

    @Resource
    PropertyMapper propertyMapper;

    @Resource
    InterfaceItemMapper interfaceItemMapper;


    @Override
    public String getName() {
        return "保存人行模型上报数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "interfaceId", type = ApiParamType.STRING, isRequired = true, desc = "接口id"),
            @Param(name = "corporationId", type = ApiParamType.LONG, isRequired = true, desc = "机构id"),
            @Param(name = "data", isRequired = true, type = ApiParamType.JSONOBJECT, desc = "数据")
    })
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        InterfaceItemVo interfaceItemVo = JSONObject.toJavaObject(paramObj, InterfaceItemVo.class);
        List<PropertyVo> propertyList = propertyMapper.getPropertyByInterfaceId(interfaceItemVo.getInterfaceId());
        /*
        处理uuid型属性，先生成uuid再写入数据库
         */
        List<PropertyVo> uuidPropertyList = propertyList.stream().filter(d -> d.getInputType().equals(PropertyVo.InputType.UUID.getValue())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(uuidPropertyList)) {
            for (PropertyVo propertyVo : uuidPropertyList) {
                if (StringUtils.isBlank(propertyVo.getComplexId())) {
                    if (!interfaceItemVo.getData().containsKey(propertyVo.getId()) || StringUtils.isBlank(interfaceItemVo.getData().getString(propertyVo.getId()))) {
                        interfaceItemVo.getData().put(propertyVo.getId(), UuidUtil.randomUuid());
                    }
                } else {
                    if (interfaceItemVo.getData().containsKey(propertyVo.getComplexId())) {
                        JSONArray subPropList = interfaceItemVo.getData().getJSONArray(propertyVo.getComplexId());
                        if (CollectionUtils.isNotEmpty(subPropList)) {
                            for (int i = 0; i < subPropList.size(); i++) {
                                JSONObject subProp = subPropList.getJSONObject(i);
                                if (!subProp.containsKey(propertyVo.getId()) || StringUtils.isBlank(subProp.getString(propertyVo.getId()))) {
                                    subProp.put(propertyVo.getId(), UuidUtil.randomUuid());
                                }
                            }
                        }
                    }
                }
            }
        }
        InterfaceItemUtil.validData(interfaceItemVo, propertyList);

        InterfaceItemVo oldInterfaceItemVo = interfaceItemMapper.getInterfaceItemById(interfaceItemVo.getId());
        if (oldInterfaceItemVo == null) {
            interfaceItemVo.setFcu(UserContext.get().getUserId(true));
            if (MapUtils.isEmpty(interfaceItemVo.getError())) {
                interfaceItemVo.setIsNew(1);
            } else {
                interfaceItemVo.setIsNew(0);
            }
            interfaceItemMapper.insertInterfaceItem(interfaceItemVo);
        } else {
            if (MapUtils.isEmpty(interfaceItemVo.getError()) && !Md5Util.encryptMD5(interfaceItemVo.getDataStr()).equalsIgnoreCase(oldInterfaceItemVo.getDataHash())) {
                interfaceItemVo.setIsNew(1);
            } else {
                interfaceItemVo.setIsNew(0);
            }
            interfaceItemVo.setLcu(UserContext.get().getUserId(true));
            interfaceItemMapper.updateInterfaceItem(interfaceItemVo);
        }
        return null;
    }

    @Override
    public String getToken() {
        return "/pbc/interfaceitem/save";
    }
}

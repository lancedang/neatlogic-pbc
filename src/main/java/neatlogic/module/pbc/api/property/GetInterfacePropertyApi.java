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
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.EnumVo;
import neatlogic.framework.pbc.dto.InterfaceItemVo;
import neatlogic.framework.pbc.dto.PropertyRelVo;
import neatlogic.framework.pbc.dto.PropertyVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetInterfacePropertyApi extends PrivateApiComponentBase {

    @Resource
    private PropertyMapper propertyMapper;

    @Resource
    private InterfaceItemMapper interfaceItemMapper;

    @Override
    public String getToken() {
        return "/pbc/interface/property/get";
    }

    @Override
    public String getName() {
        return "获取接口属性";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "interfaceId", type = ApiParamType.STRING, desc = "接口id", isRequired = true),
            @Param(name = "isFlatten", type = ApiParamType.INTEGER, desc = "是否扁平化返回数据，扁平化不会根据复杂属性展示属性的从属关系")})
    @Output({@Param(explode = PropertyVo[].class)})
    @Description(desc = "获取接口属性接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        List<PropertyVo> propertyList = propertyMapper.getPropertyByInterfaceId(paramObj.getString("interfaceId"));
        Set<String> complexIdSet = new HashSet<>();
        int isFlatten = paramObj.getIntValue("isFlatten");
        if (isFlatten == 0) {
            JSONArray finalPropertyList = new JSONArray();
            for (PropertyVo propertyVo : propertyList) {
                if (StringUtils.isBlank(propertyVo.getComplexId())) {
                    JSONObject propObj = new JSONObject();
                    propObj.put("uid", propertyVo.getUid());
                    propObj.put("id", propertyVo.getId());
                    propObj.put("name", propertyVo.getName());
                    propObj.put("alias", propertyVo.getAlias());
                    propObj.put("interfaceId", propertyVo.getInterfaceId());
                    propObj.put("interfaceName", propertyVo.getInterfaceName());
                    propObj.put("restraint", propertyVo.getRestraint());
                    propObj.put("inputType", propertyVo.getInputType());
                    propObj.put("valueRange", propertyVo.getValueRange());
                    propObj.put("description", propertyVo.getDescription());
                    propObj.put("definition", propertyVo.getDefinition());
                    propObj.put("mapping", propertyVo.getMapping());
                    propObj.put("propDefaultValue", propertyVo.getPropDefaultValue());
                    propObj.put("transferRule", propertyVo.getTransferRule());
                    //如果有配置关联//关联其他接口的属性
                    if ("relselect".equals(propertyVo.getInputType()) && CollectionUtils.isNotEmpty(propertyVo.getRelList())) {
                        List<PropertyRelVo> relList = propertyVo.getRelList();
                        List<EnumVo> enumVoList = getRelInterfaceUuidList(propertyVo.getId(), relList);//组装下拉框数据
                        if (CollectionUtils.isNotEmpty(enumVoList)) {//现有的data查不出
                            propObj.put("enumList", enumVoList);
                        }
                    }


                    if (CollectionUtils.isNotEmpty(propertyVo.getEnumList())) {
                        propObj.put("enumList", propertyVo.getEnumList());
                    }
                    finalPropertyList.add(propObj);
                } else {
                    if (!complexIdSet.contains(propertyVo.getComplexId())) {
                        List<PropertyVo> subPropertyList = propertyList.stream().filter(d -> d.getComplexId().equals(propertyVo.getComplexId())).collect(Collectors.toList());
                        JSONObject propObj = new JSONObject();
                        propObj.put("complexId", propertyVo.getComplexId());
                        propObj.put("complexName", propertyVo.getComplexName());
                        JSONArray subPropList = new JSONArray();
                        for (PropertyVo subProp : subPropertyList) {
                            JSONObject subPropObj = new JSONObject();
                            subPropObj.put("complexId", propertyVo.getComplexId());
                            subPropObj.put("complexName", propertyVo.getComplexName());
                            subPropObj.put("uid", subProp.getUid());
                            subPropObj.put("id", subProp.getId());
                            subPropObj.put("name", subProp.getName());
                            subPropObj.put("alias", subProp.getAlias());
                            subPropObj.put("interfaceId", subProp.getInterfaceId());
                            subPropObj.put("interfaceName", subProp.getInterfaceName());
                            subPropObj.put("restraint", subProp.getRestraint());
                            subPropObj.put("inputType", subProp.getInputType());
                            subPropObj.put("valueRange", subProp.getValueRange());
                            subPropObj.put("description", subProp.getDescription());
                            subPropObj.put("definition", subProp.getDefinition());
                            subPropObj.put("mapping", subProp.getMapping());
                            subPropObj.put("propDefaultValue", subProp.getPropDefaultValue());
                            subPropObj.put("transferRule", subProp.getTransferRule());
                            if (CollectionUtils.isNotEmpty(subProp.getEnumList())) {
                                subPropObj.put("enumList", subProp.getEnumList());
                            }
                            subPropList.add(subPropObj);
                        }
                        propObj.put("subPropertyList", subPropList);
                        finalPropertyList.add(propObj);
                        complexIdSet.add(propertyVo.getComplexId());
                    }
                }
            }
            return finalPropertyList;
        } else {
            return propertyList;
        }
    }

    private List<EnumVo> getRelInterfaceUuidList(String fromPropertyId, List<PropertyRelVo> relList) {
        List<EnumVo> enumVoList = new ArrayList<>();
        for (PropertyRelVo propertyRelVo : relList) {
            InterfaceItemVo itemVo = new InterfaceItemVo();
            itemVo.setInterfaceId(propertyRelVo.getToInterfaceId());
            List<InterfaceItemVo> itemList = interfaceItemMapper.searchInterfaceItem(itemVo);
            for (InterfaceItemVo interfaceItemVo : itemList) {
                if (interfaceItemVo != null && interfaceItemVo.getData() != null) {
                    String valuePropertyId = propertyRelVo.getToValuePropertyId();
                    String textPropertyId = propertyRelVo.getToTextPropertyId();
                    String value = drawValueFromJson(interfaceItemVo.getData(), valuePropertyId);
                    String text = drawValueFromJson(interfaceItemVo.getData(), textPropertyId);
                    if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(text)) {
                        EnumVo enumVo = new EnumVo();
                        enumVo.setPropertyId(fromPropertyId);
                        enumVo.setValue(value);
                        enumVo.setText(text);
                        enumVoList.add(enumVo);
                    }
                }
            }
        }
        return enumVoList;
    }


    public static String drawValueFromJson(JSONObject jsonObj, String keyName) {
        String[] results = JSONPath.read(jsonObj.toJSONString(), "$.." + keyName, String[].class);
        if (results != null && results.length > 0) {
            return String.join(",", results);
        }
        return "";
    }


}

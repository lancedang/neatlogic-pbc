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

package neatlogic.module.pbc.policy.handler;

import neatlogic.framework.cmdb.crossover.ICiEntityCrossoverService;
import neatlogic.framework.cmdb.crossover.ICustomViewCrossoverService;
import neatlogic.framework.cmdb.crossover.ICustomViewDataCrossoverService;
import neatlogic.framework.cmdb.dto.cientity.AttrEntityVo;
import neatlogic.framework.cmdb.dto.cientity.CiEntityVo;
import neatlogic.framework.cmdb.dto.cientity.RelEntityVo;
import neatlogic.framework.cmdb.dto.customview.CustomViewAttrVo;
import neatlogic.framework.cmdb.dto.customview.CustomViewConditionVo;
import neatlogic.framework.cmdb.dto.customview.CustomViewConstAttrVo;
import neatlogic.framework.cmdb.dto.group.ConditionGroupVo;
import neatlogic.framework.cmdb.dto.group.ConditionVo;
import neatlogic.framework.cmdb.enums.RelDirectionType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.*;
import neatlogic.framework.pbc.exception.InterfaceItemIrregularException;
import neatlogic.framework.pbc.policy.core.PhaseHandlerBase;
import neatlogic.framework.util.Md5Util;
import neatlogic.framework.util.javascript.JavascriptUtil;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SyncPhaseHandler extends PhaseHandlerBase {
    private static final Logger logger = LoggerFactory.getLogger(SyncPhaseHandler.class);

    @Override
    public String getPhase() {
        return "sync";
    }

    @Override
    public String getPhaseLabel() {
        return "从配置库中同步数据";
    }

    @Resource
    private InterfaceItemMapper interfaceItemMapper;

    @Resource
    private PropertyMapper propertyMapper;

    @Override
    public List<PolicyConfigVo> getConfigTemplate() {
        return null;
    }

    private JSONObject generateData(List<PropertyVo> propertyList, Map<String, Object> customViewData) {
        JSONObject dataObj = new JSONObject();
        for (PropertyVo propertyVo : propertyList) {
            if (MapUtils.isNotEmpty(propertyVo.getMapping())) {
                if (StringUtils.isNotBlank(propertyVo.getComplexId())) {
                    if (!dataObj.containsKey(propertyVo.getComplexId())) {
                        dataObj.put(propertyVo.getComplexId(), new JSONArray());
                    }
                }

                JSONObject mappingObj = propertyVo.getMapping();
                String name = mappingObj.getString("name");
                String dataValue = customViewData.get(name) != null ? convertValue(customViewData.get(name).toString(), propertyVo.getTransferRule()) : "";
                //由于视图的日期格式和人行需要的不一样，因此需要先转换格式
                if (StringUtils.isBlank(propertyVo.getComplexId())) {
                    dataObj.put(propertyVo.getId(), dataValue);
                } else {
                    if (CollectionUtils.isEmpty(dataObj.getJSONArray(propertyVo.getComplexId()))) {
                        dataObj.getJSONArray(propertyVo.getComplexId()).add(new JSONObject());
                    }
                    dataObj.getJSONArray(propertyVo.getComplexId()).getJSONObject(0).put(propertyVo.getId(), dataValue);
                }
            }
            //设置默认值
            if (StringUtils.isNotBlank(propertyVo.getPropDefaultValue())) {
                if (StringUtils.isBlank(propertyVo.getComplexId())) {
                    if (StringUtils.isBlank(dataObj.getString(propertyVo.getId()))) {
                        dataObj.put(propertyVo.getId(), propertyVo.getPropDefaultValue());
                    }
                } else {
                    if (CollectionUtils.isEmpty(dataObj.getJSONArray(propertyVo.getComplexId()))) {
                        dataObj.put(propertyVo.getComplexId(), new JSONArray());
                        dataObj.getJSONArray(propertyVo.getComplexId()).add(new JSONObject() {{
                            this.put(propertyVo.getId(), propertyVo.getPropDefaultValue());
                        }});
                    } else {
                        for (int i = 0; i < dataObj.getJSONArray(propertyVo.getComplexId()).size(); i++) {
                            JSONObject d = dataObj.getJSONArray(propertyVo.getComplexId()).getJSONObject(i);
                            if (StringUtils.isBlank(d.getString(propertyVo.getId()))) {
                                d.put(propertyVo.getId(), propertyVo.getPropDefaultValue());
                            }
                        }
                    }
                }
            }
        }
        return dataObj;
    }

    /**
     * 生成接口数据
     *
     * @param propertyList 属性列表
     * @param ciEntityVo   配置项信息
     * @return 人行要求的数据
     */
    private JSONObject generateData(List<PropertyVo> propertyList, CiEntityVo ciEntityVo) {
        JSONObject dataObj = new JSONObject();
        for (PropertyVo propertyVo : propertyList) {
            if (MapUtils.isNotEmpty(propertyVo.getMapping()) && propertyVo.getMapping().containsKey("id")) {
                if (StringUtils.isNotBlank(propertyVo.getComplexId())) {
                    if (!dataObj.containsKey(propertyVo.getComplexId())) {
                        dataObj.put(propertyVo.getComplexId(), new JSONArray());
                    }
                }

                JSONObject mappingObj = propertyVo.getMapping();
                String type = propertyVo.getMapping().getString("type");
                ICiEntityCrossoverService ciEntityCrossoverService = CrossoverServiceFactory.getApi(ICiEntityCrossoverService.class);
                if (!mappingObj.containsKey("targetCiId")) {
                    //处理普通属性
                    switch (type) {
                        case "uuid":
                            String uuid = convertValue(ciEntityVo.getUuid(), propertyVo.getTransferRule());
                            if (StringUtils.isBlank(propertyVo.getComplexId())) {
                                dataObj.put(propertyVo.getId(), uuid);
                            } else {
                                if (CollectionUtils.isEmpty(dataObj.getJSONArray(propertyVo.getComplexId()))) {
                                    dataObj.getJSONArray(propertyVo.getComplexId()).add(new JSONObject());
                                }
                                dataObj.getJSONArray(propertyVo.getComplexId()).getJSONObject(0).put(propertyVo.getId(), uuid);
                            }
                            break;
                        case "name":
                            String name = convertValue(ciEntityVo.getName(), propertyVo.getTransferRule());
                            if (StringUtils.isBlank(propertyVo.getComplexId())) {
                                dataObj.put(propertyVo.getId(), name);
                            } else {
                                if (CollectionUtils.isEmpty(dataObj.getJSONArray(propertyVo.getComplexId()))) {
                                    dataObj.getJSONArray(propertyVo.getComplexId()).add(new JSONObject());
                                }
                                dataObj.getJSONArray(propertyVo.getComplexId()).getJSONObject(0).put(propertyVo.getId(), name);
                            }
                            break;
                        case "lcd":
                            String lcdStr = "";
                            if (ciEntityVo.getLcd() != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                lcdStr = sdf.format(ciEntityVo.getLcd());
                            }
                            lcdStr = convertValue(lcdStr, propertyVo.getTransferRule());
                            if (StringUtils.isBlank(propertyVo.getComplexId())) {
                                dataObj.put(propertyVo.getId(), lcdStr);
                            } else {
                                if (CollectionUtils.isEmpty(dataObj.getJSONArray(propertyVo.getComplexId()))) {
                                    dataObj.getJSONArray(propertyVo.getComplexId()).add(new JSONObject());
                                }

                                dataObj.getJSONArray(propertyVo.getComplexId()).getJSONObject(0).put(propertyVo.getId(), lcdStr);
                            }
                            break;
                        case "attr":
                            Long attrId = mappingObj.getLong("attrId");
                            if (attrId != null) {
                                AttrEntityVo attrEntityVo = ciEntityVo.getAttrEntityByAttrId(attrId);
                                if (attrEntityVo != null && StringUtils.isNotBlank(attrEntityVo.getValue())) {
                                    if (StringUtils.isBlank(propertyVo.getComplexId())) {
                                        dataObj.put(propertyVo.getId(), convertValue(attrEntityVo.getActualValue(), propertyVo.getTransferRule()));
                                    } else {
                                        if (CollectionUtils.isEmpty(dataObj.getJSONArray(propertyVo.getComplexId()))) {
                                            dataObj.getJSONArray(propertyVo.getComplexId()).add(new JSONObject());
                                        }
                                        dataObj.getJSONArray(propertyVo.getComplexId()).getJSONObject(0).put(propertyVo.getId(), convertValue(attrEntityVo.getActualValue(), propertyVo.getTransferRule()));
                                    }
                                }
                            }
                            break;
                    }
                } else {
                    Long attrId = mappingObj.getLong("attrId");
                    Long relId = mappingObj.getLong("relId");
                    String direction = mappingObj.getString("direction");
                    List<Long> targetCiEntityIdList = new ArrayList<>();
                    if (attrId != null) {
                        AttrEntityVo attrEntityVo = ciEntityVo.getAttrEntityByAttrId(attrId);
                        if (attrEntityVo != null && CollectionUtils.isNotEmpty(attrEntityVo.getValueList())) {
                            try {
                                targetCiEntityIdList = attrEntityVo.getValueList().stream().map(d -> Long.parseLong(d.toString())).collect(Collectors.toList());
                            } catch (Exception ignored) {

                            }
                        }
                    } else if (relId != null && StringUtils.isNotBlank(direction)) {
                        List<RelEntityVo> relEntityList = ciEntityVo.getRelEntityByRelIdAndDirection(relId, direction);
                        if (CollectionUtils.isNotEmpty(relEntityList)) {
                            for (RelEntityVo relEntityVo : relEntityList) {
                                targetCiEntityIdList.add(direction.equals(RelDirectionType.FROM.getValue()) ? relEntityVo.getToCiEntityId() : relEntityVo.getFromCiEntityId());
                            }
                        }
                    }
                    //处理引用属性
                    if (CollectionUtils.isNotEmpty(targetCiEntityIdList)) {
                        CiEntityVo cientity = new CiEntityVo();
                        cientity.setIdList(targetCiEntityIdList);
                        List<CiEntityVo> targetCiEntityList = ciEntityCrossoverService.getCiEntityByIdList(cientity);
                        if (CollectionUtils.isNotEmpty(targetCiEntityList)) {
                            switch (type) {
                                case "uuid":
                                    if (StringUtils.isBlank(propertyVo.getComplexId())) {
                                        dataObj.put(propertyVo.getId(), convertValue(targetCiEntityList.stream().map(CiEntityVo::getUuid).collect(Collectors.joining(",")), propertyVo.getTransferRule()));
                                    } else {
                                        if (!dataObj.containsKey(propertyVo.getComplexId())) {
                                            dataObj.put(propertyVo.getComplexId(), new JSONArray());
                                        }
                                        JSONArray dataList = dataObj.getJSONArray(propertyVo.getComplexId());
                                        for (int i = 0; i < targetCiEntityList.size(); i++) {
                                            CiEntityVo targetCiEntityVo = targetCiEntityList.get(i);
                                            if (dataList.size() > i) {
                                                dataList.getJSONObject(i).put(propertyVo.getId(), convertValue(targetCiEntityVo.getUuid(), propertyVo.getTransferRule()));
                                            } else {
                                                JSONObject d = new JSONObject();
                                                d.put(propertyVo.getId(), convertValue(targetCiEntityVo.getUuid(), propertyVo.getTransferRule()));
                                                dataList.add(d);
                                            }
                                        }
                                    }
                                    break;
                                case "name":
                                    if (StringUtils.isBlank(propertyVo.getComplexId())) {
                                        dataObj.put(propertyVo.getId(), convertValue(targetCiEntityList.stream().map(CiEntityVo::getName).collect(Collectors.joining(",")), propertyVo.getTransferRule()));
                                    } else {
                                        if (!dataObj.containsKey(propertyVo.getComplexId())) {
                                            dataObj.put(propertyVo.getComplexId(), new JSONArray());
                                        }
                                        JSONArray dataList = dataObj.getJSONArray(propertyVo.getComplexId());
                                        for (int i = 0; i < targetCiEntityList.size(); i++) {
                                            CiEntityVo targetCiEntityVo = targetCiEntityList.get(i);
                                            if (dataList.size() > i) {
                                                dataList.getJSONObject(i).put(propertyVo.getId(), convertValue(targetCiEntityVo.getName(), propertyVo.getTransferRule()));
                                            } else {
                                                JSONObject d = new JSONObject();
                                                d.put(propertyVo.getId(), convertValue(targetCiEntityVo.getName(), propertyVo.getTransferRule()));
                                                dataList.add(d);
                                            }
                                        }
                                    }
                                    break;
                                case "lcd":
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                    if (StringUtils.isBlank(propertyVo.getComplexId())) {
                                        List<Date> lcdList = targetCiEntityList.stream().map(CiEntityVo::getLcd).collect(Collectors.toList());
                                        List<String> lcdStrList = new ArrayList<>();
                                        for (Date d : lcdList) {
                                            if (d != null) {
                                                lcdStrList.add(sdf.format(d));
                                            }
                                        }
                                        dataObj.put(propertyVo.getId(), convertValue(String.join(",", lcdStrList), propertyVo.getTransferRule()));
                                    } else {
                                        if (!dataObj.containsKey(propertyVo.getComplexId())) {
                                            dataObj.put(propertyVo.getComplexId(), new JSONArray());
                                        }
                                        JSONArray dataList = dataObj.getJSONArray(propertyVo.getComplexId());
                                        for (int i = 0; i < targetCiEntityList.size(); i++) {
                                            CiEntityVo targetCiEntityVo = targetCiEntityList.get(i);
                                            String lcdStr = "";
                                            if (targetCiEntityVo.getLcd() != null) {
                                                lcdStr = sdf.format(targetCiEntityVo.getLcd());
                                            }
                                            if (dataList.size() > i) {
                                                dataList.getJSONObject(i).put(propertyVo.getId(), convertValue(lcdStr, propertyVo.getTransferRule()));
                                            } else {
                                                JSONObject d = new JSONObject();
                                                d.put(propertyVo.getId(), convertValue(lcdStr, propertyVo.getTransferRule()));
                                                dataList.add(d);
                                            }
                                        }
                                    }
                                    break;
                                case "attr":
                                    Long targetAttrId = mappingObj.getLong("targetAttrId");
                                    if (targetAttrId != null) {
                                        if (StringUtils.isBlank(propertyVo.getComplexId())) {
                                            List<String> attrValueList = new ArrayList<>();
                                            targetCiEntityList.forEach(targetCiEntity -> {
                                                AttrEntityVo targetAttrVo = targetCiEntity.getAttrEntityByAttrId(targetAttrId);
                                                if (targetAttrVo != null) {
                                                    attrValueList.add(targetAttrVo.getValue());
                                                }
                                            });
                                            if (CollectionUtils.isNotEmpty(attrValueList)) {
                                                dataObj.put(propertyVo.getId(), convertValue(String.join(",", attrValueList), propertyVo.getTransferRule()));
                                            }
                                        } else {
                                            if (!dataObj.containsKey(propertyVo.getComplexId())) {
                                                dataObj.put(propertyVo.getComplexId(), new JSONArray());
                                            }
                                            JSONArray dataList = dataObj.getJSONArray(propertyVo.getComplexId());
                                            for (int i = 0; i < targetCiEntityList.size(); i++) {
                                                CiEntityVo targetCiEntity = targetCiEntityList.get(i);
                                                AttrEntityVo targetAttrVo = targetCiEntity.getAttrEntityByAttrId(targetAttrId);
                                                if (dataList.size() > i) {
                                                    if (targetAttrVo != null) {
                                                        dataList.getJSONObject(i).put(propertyVo.getId(), convertValue(targetAttrVo.getValue(), propertyVo.getTransferRule()));
                                                    }
                                                } else {
                                                    JSONObject d = new JSONObject();
                                                    if (targetAttrVo != null) {
                                                        d.put(propertyVo.getId(), convertValue(targetAttrVo.getValue(), propertyVo.getTransferRule()));
                                                    }
                                                    dataList.add(d);
                                                }
                                            }
                                        }
                                    }
                                    break;
                            }
                        }
                    }
                }

            }
            //设置默认值
            if (StringUtils.isNotBlank(propertyVo.getPropDefaultValue())) {
                if (StringUtils.isBlank(propertyVo.getComplexId())) {
                    if (StringUtils.isBlank(dataObj.getString(propertyVo.getId()))) {
                        dataObj.put(propertyVo.getId(), propertyVo.getPropDefaultValue());
                    }
                } else {
                    if (CollectionUtils.isEmpty(dataObj.getJSONArray(propertyVo.getComplexId()))) {
                        dataObj.put(propertyVo.getComplexId(), new JSONArray());
                        dataObj.getJSONArray(propertyVo.getComplexId()).add(new JSONObject() {{
                            this.put(propertyVo.getId(), propertyVo.getPropDefaultValue());
                        }});
                    } else {
                        for (int i = 0; i < dataObj.getJSONArray(propertyVo.getComplexId()).size(); i++) {
                            JSONObject d = dataObj.getJSONArray(propertyVo.getComplexId()).getJSONObject(i);
                            if (StringUtils.isBlank(d.getString(propertyVo.getId()))) {
                                d.put(propertyVo.getId(), propertyVo.getPropDefaultValue());
                            }
                        }
                    }
                }
            }
        }
        return dataObj;
    }

    /**
     * 根据转换规则转换数据
     *
     * @param value        原数据
     * @param transferRule 转换规则
     * @return 转换后数据
     */
    private static String convertValue(String value, String transferRule) {
        if (StringUtils.isNotBlank(value) && StringUtils.isNotBlank(transferRule)) {
            //如果数据是多个值的组合就会出现逗号，先尝试按照逗号拆分数据逐个匹配
            String[] values = value.split(",");
            String[] rules = transferRule.split(",");
            for (int i = 0; i < values.length; i++) {
                for (String rule : rules) {
                    String[] r = rule.split(":");
                    if (r.length == 2 && StringUtils.isNotBlank(r[0])) {
                        if (StringUtils.isNotBlank(values[i]) && values[i].equalsIgnoreCase(r[0])) {
                            values[i] = r[1];
                            break;
                        }
                    }
                }
            }
            return String.join(",", values);
        }
        return value;
    }

    private static boolean matchRule(Map<String, Object> data, Map<String, String> define, JSONObject ruleObj) {
        boolean isAllMatch = false;
        if (data != null && MapUtils.isNotEmpty(ruleObj)) {
            JSONArray conditionGroupList = ruleObj.getJSONArray("conditionGroupList");
            JSONArray conditionGroupRelList = ruleObj.getJSONArray("conditionGroupRelList");
            if (CollectionUtils.isNotEmpty(conditionGroupList)) {
                //构造脚本
                StringBuilder script = new StringBuilder();
                JSONObject conditionObj = new JSONObject();
                for (int i = 0; i < conditionGroupList.size(); i++) {
                    ConditionGroupVo conditionGroupVo = JSONObject.toJavaObject(conditionGroupList.getJSONObject(i), ConditionGroupVo.class);
                    if (i > 0 && CollectionUtils.isNotEmpty(conditionGroupRelList)) {
                        if (conditionGroupRelList.size() >= i) {
                            String joinType = conditionGroupRelList.getString(i - 1);
                            script.append(joinType.equals("and") ? " && " : " || ");
                        } else {
                            //数据异常跳出
                            break;
                        }
                    }
                    script.append("(").append(conditionGroupVo.buildScript()).append(")");
                    if (CollectionUtils.isNotEmpty(conditionGroupVo.getConditionList())) {
                        for (ConditionVo conditionVo : conditionGroupVo.getConditionList()) {
                            conditionObj.put(conditionVo.getUuid(), conditionVo.getValueList());
                        }
                    }

                }
                //将配置项参数处理成指定格式，格式和表达式相关，不能随意修改格式
                JSONObject paramObj = new JSONObject();
                JSONObject dataObj = new JSONObject();
                JSONObject defineObj = new JSONObject();
                for (String key : data.keySet()) {
                    if (!key.endsWith("_hash")) {
                        defineObj.put(key, define.get(key));
                        dataObj.put(key, new JSONArray() {{
                            this.add(data.get(key));
                        }});
                    }
                }
                paramObj.put("define", defineObj);
                paramObj.put("data", dataObj);
                paramObj.put("condition", conditionObj);
                try {
                    isAllMatch = JavascriptUtil.runExpression(paramObj, script.toString());
                } catch (ApiRuntimeException ex) {
                    logger.error(ex.getMessage(), ex);
                    //忽略内部异常
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            } else {
                //空规则，直接返回true
                isAllMatch = true;
            }
        }
        return isAllMatch;
    }

    private static boolean matchRule(CiEntityVo ciEntityVo, JSONObject ruleObj) {
        boolean isAllMatch = false;
        if (ciEntityVo != null && MapUtils.isNotEmpty(ruleObj)) {
            JSONArray conditionGroupList = ruleObj.getJSONArray("conditionGroupList");
            JSONArray conditionGroupRelList = ruleObj.getJSONArray("conditionGroupRelList");
            if (CollectionUtils.isNotEmpty(conditionGroupList)) {
                //构造脚本
                StringBuilder script = new StringBuilder();
                JSONObject conditionObj = new JSONObject();
                for (int i = 0; i < conditionGroupList.size(); i++) {
                    ConditionGroupVo conditionGroupVo = JSONObject.toJavaObject(conditionGroupList.getJSONObject(i), ConditionGroupVo.class);
                    if (i > 0 && CollectionUtils.isNotEmpty(conditionGroupRelList)) {
                        if (conditionGroupRelList.size() >= i) {
                            String joinType = conditionGroupRelList.getString(i - 1);
                            script.append(joinType.equals("and") ? " && " : " || ");
                        } else {
                            //数据异常跳出
                            break;
                        }
                    }
                    script.append("(").append(conditionGroupVo.buildScript()).append(")");
                    if (CollectionUtils.isNotEmpty(conditionGroupVo.getConditionList())) {
                        for (ConditionVo conditionVo : conditionGroupVo.getConditionList()) {
                            conditionObj.put(conditionVo.getUuid(), conditionVo.getValueList());
                        }
                    }

                }
                //将配置项参数处理成指定格式，格式和表达式相关，不能随意修改格式
                JSONObject paramObj = new JSONObject();
                JSONObject dataObj = new JSONObject();
                JSONObject defineObj = new JSONObject();
                if (MapUtils.isNotEmpty(ciEntityVo.getAttrEntityData())) {
                    for (String key : ciEntityVo.getAttrEntityData().keySet()) {
                        defineObj.put(key, ciEntityVo.getAttrEntityData().getJSONObject(key).getString("label"));
                        dataObj.put(key, ciEntityVo.getAttrEntityData().getJSONObject(key).getJSONArray("valueList"));
                    }
                }
                if (MapUtils.isNotEmpty(ciEntityVo.getRelEntityData())) {
                    for (String key : ciEntityVo.getRelEntityData().keySet()) {
                        //转换格式
                        JSONArray valueList = new JSONArray();
                        if (CollectionUtils.isNotEmpty(ciEntityVo.getRelEntityData().getJSONObject(key).getJSONArray("valueList"))) {
                            for (int i = 0; i < ciEntityVo.getRelEntityData().getJSONObject(key).getJSONArray("valueList").size(); i++) {
                                JSONObject entityObj = ciEntityVo.getRelEntityData().getJSONObject(key).getJSONArray("valueList").getJSONObject(i);
                                valueList.add(entityObj.getLong("ciEntityId"));
                            }
                        }
                        defineObj.put(key, ciEntityVo.getRelEntityData().getJSONObject(key).getString("label"));
                        dataObj.put(key, valueList);
                    }
                }
                paramObj.put("define", defineObj);
                paramObj.put("data", dataObj);
                paramObj.put("condition", conditionObj);
                try {
                    isAllMatch = JavascriptUtil.runExpression(paramObj, script.toString());
                } catch (ApiRuntimeException ex) {
                    logger.error(ex.getMessage(), ex);
                    //忽略内部异常
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            } else {
                //空规则，直接返回true
                isAllMatch = true;
            }
        }
        return isAllMatch;
    }

    @Override
    protected String myExecute(PolicyAuditVo policyAuditVo, PolicyVo policyVo, PolicyPhaseVo policyPhaseVo, List<InterfaceVo> interfaceList) {
        //重置所有有配置项id的数据的操作为空，以便找出需要删除的数据(人工录入数据则不重置)
        int errorCount = 0;
        int syncCount = 0;
        for (InterfaceVo interfaceVo : interfaceList) {
            List<PropertyVo> propertyList = propertyMapper.getPropertyByInterfaceId(interfaceVo.getId());
            /*boolean hasMapping = false;
            if (interfaceVo.getCiId() != null) {
                for (PropertyVo propertyVo : propertyList) {
                    if (MapUtils.isNotEmpty(propertyVo.getMapping())) {
                        hasMapping = true;
                        break;
                    }
                }
            }*/
            InterfaceCorporationVo interfaceCorporationVo = interfaceMapper.getInterfaceCorporationByInterfaceIdAndCorporationId(interfaceVo.getId(), policyVo.getCorporationId());
            if (interfaceVo.getPriority().equals(InterfaceVo.Priority.CI.getValue()) && interfaceVo.getCiId() != null) {
                CiEntityVo pCiEntityVo = new CiEntityVo();
                pCiEntityVo.setCiId(interfaceVo.getCiId());
                pCiEntityVo.setPageSize(100);
                pCiEntityVo.setCurrentPage(1);
                ICiEntityCrossoverService ciEntityCrossoverService = CrossoverServiceFactory.getApi(ICiEntityCrossoverService.class);
                List<Long> ciEntityIdList = ciEntityCrossoverService.getCiEntityIdByCiId(pCiEntityVo);
                //先把已导入的数据数据重置成需要删除
                interfaceItemMapper.updateInterfaceItemIsDeleteByInterfaceIdAndCiId(interfaceVo.getId(), interfaceVo.getCiId());
                while (CollectionUtils.isNotEmpty(ciEntityIdList)) {
                    for (Long ciEntityId : ciEntityIdList) {
                        CiEntityVo cientity = new CiEntityVo();
                        cientity.setCiId(interfaceVo.getCiId());
                        cientity.setLimitAttrEntity(false);
                        cientity.setLimitRelEntity(false);
                        cientity.setId(ciEntityId);
                        CiEntityVo ciEntityVo = ciEntityCrossoverService.getCiEntityById(cientity);

                        if (ciEntityVo != null) {
                            if (interfaceCorporationVo != null && MapUtils.isNotEmpty(interfaceCorporationVo.getRule())) {
                                if (!matchRule(ciEntityVo, interfaceCorporationVo.getRule())) {
                                    //如果不符合机构规则，直接忽略此配置项导入
                                    continue;
                                }
                            }
                            JSONObject data = generateData(propertyList, ciEntityVo);
                            InterfaceItemVo interfaceItemVo = interfaceItemMapper.getInterfaceItemByInterfaceIdAndCiEntityIdAndCorporationId(interfaceVo.getId(), ciEntityId, policyVo.getCorporationId());
                            if (interfaceItemVo == null) {
                                interfaceItemVo = new InterfaceItemVo();
                                interfaceItemVo.setInterfaceId(interfaceVo.getId());
                                interfaceItemVo.setData(data);
                                interfaceItemVo.setCiId(interfaceVo.getCiId());
                                interfaceItemVo.setCiEntityId(ciEntityId);
                                interfaceItemVo.setCorporationId(policyVo.getCorporationId());
                                //验证数据是否合法
                                InterfaceItemUtil.validData(interfaceItemVo, propertyList);
                                if (MapUtils.isEmpty(interfaceItemVo.getError())) {
                                    interfaceItemVo.setIsNew(1);
                                    syncCount++;
                                } else {
                                    interfaceItemVo.setIsNew(0);
                                    errorCount += 1;
                                }
                                interfaceItemMapper.insertInterfaceItem(interfaceItemVo);
                            } else {
                                //如果已经上报成功，则检查是否和上报数据一致
                                interfaceItemVo.setData(data);
                                interfaceItemVo.setIsDelete(0);
                                interfaceItemVo.setCorporationId(policyVo.getCorporationId());
                                InterfaceItemUtil.validData(interfaceItemVo, propertyList);
                                if (MapUtils.isEmpty(interfaceItemVo.getError())) {
                                    if (StringUtils.isNotBlank(interfaceItemVo.getDataHash())) {
                                        //如果datahash和这次同步的生成的数据不一致，代表需要修改
                                        if (!Md5Util.encryptMD5(interfaceItemVo.getDataStr()).equalsIgnoreCase(interfaceItemVo.getDataHash())) {
                                            interfaceItemVo.setIsNew(1);
                                            syncCount++;
                                        } else {
                                            interfaceItemVo.setIsNew(0);
                                        }
                                    } else {
                                        interfaceItemVo.setIsNew(1);
                                        syncCount++;
                                    }
                                } else {
                                    errorCount += 1;
                                    interfaceItemVo.setIsNew(0);
                                }
                                interfaceItemMapper.updateInterfaceItem(interfaceItemVo);
                            }
                        }
                    }
                    pCiEntityVo.setCurrentPage(pCiEntityVo.getCurrentPage() + 1);
                    ciEntityIdList = ciEntityCrossoverService.getCiEntityIdByCiId(pCiEntityVo);
                }
                //删除isDelete=1并且isImported=0的数据
                interfaceItemMapper.deleteInterfaceItemByInterfaceIdAndCiId(interfaceVo.getId(), interfaceVo.getCiId());
            } else if (interfaceVo.getPriority().equals(InterfaceVo.Priority.VIEW.getValue()) && interfaceVo.getCustomViewId() != null) {
                //从自定义视图获取数据
                ICustomViewCrossoverService customViewCrossoverService = CrossoverServiceFactory.getApi(ICustomViewCrossoverService.class);
                CustomViewAttrVo customViewAttrVo = new CustomViewAttrVo();
                customViewAttrVo.setIsHidden(0);
                customViewAttrVo.setCustomViewId(interfaceVo.getCustomViewId());
                List<CustomViewAttrVo> attrList = customViewCrossoverService.getCustomViewAttrByCustomViewId(customViewAttrVo);

                CustomViewConstAttrVo customViewConstAttrVo = new CustomViewConstAttrVo();
                customViewConstAttrVo.setIsHidden(0);
                customViewConstAttrVo.setCustomViewId(interfaceVo.getCustomViewId());
                List<CustomViewConstAttrVo> constAttrList = customViewCrossoverService.getCustomViewConstAttrByCustomViewId(customViewConstAttrVo);

                //用于报错时显示属性民称
                Map<String, String> defineData = new HashMap<>();
                List<String> primaryKeyList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(attrList)) {
                    for (CustomViewAttrVo attrVo : attrList) {
                        if (StringUtils.isNotBlank(attrVo.getName())) {
                            defineData.put(attrVo.getName(), attrVo.getAlias());
                            if (attrVo.getIsPrimary() != null && attrVo.getIsPrimary().equals(1)) {
                                primaryKeyList.add(attrVo.getName());
                            }
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(constAttrList)) {
                    for (CustomViewConstAttrVo constAttrVo : constAttrList) {
                        defineData.put(constAttrVo.getName(), constAttrVo.getAlias());
                        if (StringUtils.isNotBlank(constAttrVo.getName()) && constAttrVo.getIsPrimary() != null && constAttrVo.getIsPrimary().equals(1)) {
                            primaryKeyList.add(constAttrVo.getName());
                        }
                    }
                }
                //System.out.println("PK KEY:" + String.join("+", primaryKeyList));
                CustomViewConditionVo customViewConditionVo = new CustomViewConditionVo();
                customViewConditionVo.setCustomViewId(interfaceVo.getCustomViewId());
                customViewConditionVo.setPageSize(100);
                customViewConditionVo.setCurrentPage(1);

                ICustomViewDataCrossoverService customViewDataCrossoverService = CrossoverServiceFactory.getApi(ICustomViewDataCrossoverService.class);
                List<Map<String, Object>> customViewDataList = customViewDataCrossoverService.searchCustomViewDataFlatten(customViewConditionVo);
                //先把已导入的数据数据重置成需要删除
                interfaceItemMapper.updateInterfaceItemIsDeleteByInterfaceIdAndCustomViewId(interfaceVo.getId(), interfaceVo.getCustomViewId());
                while (CollectionUtils.isNotEmpty(customViewDataList)) {
                    int index = 0;
                    for (Map<String, Object> customViewData : customViewDataList) {
                        if (index >= customViewConditionVo.getPageSize()) {
                            //由于使用了pageSizePlus进行分页，所以需要跳过最后一条数据
                            break;
                        }
                        if (interfaceCorporationVo != null && MapUtils.isNotEmpty(interfaceCorporationVo.getRule())) {
                            if (!matchRule(customViewData, defineData, interfaceCorporationVo.getRule())) {
                                //如果不符合机构规则，直接忽略此配置项导入
                                continue;
                            }
                        }
                        //计算出数据主键
                        List<String> primaryKeyValueList = new ArrayList<>();
                        if (CollectionUtils.isNotEmpty(primaryKeyList)) {
                            for (String primaryKey : primaryKeyList) {
                                if (customViewData.get(primaryKey) != null) {
                                    primaryKeyValueList.add(customViewData.get(primaryKey).toString());
                                }
                            }
                        } else {
                            for (String keys : customViewData.keySet()) {
                                if (customViewData.get(keys) != null) {
                                    primaryKeyValueList.add(customViewData.get(keys).toString());
                                }
                            }
                        }
                        //System.out.println("PK VALUE:" + String.join("+", primaryKeyValueList));
                        //把所有值进行排序，只要顺序能随着值固定就好
                        primaryKeyValueList.sort(Comparator.comparingInt(Object::hashCode));
                        String pk = Md5Util.encryptMD5(String.join("#", primaryKeyValueList));
                        //System.out.println("PK:" + pk);
                        //将primary key设进原始数据，在generateData时就可以根据primarykey获取值
                        customViewData.put("_primarykey", pk);
                        JSONObject data = generateData(propertyList, customViewData);
                        InterfaceItemVo interfaceItemVo = interfaceItemMapper.getInterfaceItemByPrimaryKeyAndCorporationId(interfaceVo.getId(), pk, policyVo.getCorporationId());
                        if (interfaceItemVo == null) {
                            interfaceItemVo = new InterfaceItemVo();
                            interfaceItemVo.setInterfaceId(interfaceVo.getId());
                            interfaceItemVo.setData(data);
                            interfaceItemVo.setCustomViewId(interfaceVo.getCustomViewId());
                            interfaceItemVo.setPrimaryKey(pk);
                            interfaceItemVo.setCorporationId(policyVo.getCorporationId());
                            //验证数据是否合法
                            InterfaceItemUtil.validData(interfaceItemVo, propertyList);
                            if (MapUtils.isEmpty(interfaceItemVo.getError())) {
                                interfaceItemVo.setIsNew(1);
                                syncCount++;
                            } else {
                                interfaceItemVo.setIsNew(0);
                                errorCount += 1;
                            }
                            interfaceItemMapper.insertInterfaceItem(interfaceItemVo);
                        } else {
                            //如果已经上报成功，则检查是否和上报数据一致
                            interfaceItemVo.setIsDelete(0);
                            interfaceItemVo.setData(data);
                            interfaceItemVo.setCorporationId(policyVo.getCorporationId());

                            InterfaceItemUtil.validData(interfaceItemVo, propertyList);
                            if (MapUtils.isEmpty(interfaceItemVo.getError())) {
                                if (StringUtils.isNotBlank(interfaceItemVo.getDataHash())) {
                                    //如果datahash和这次同步的生成的数据不一致，代表需要修改
                                    if (!Md5Util.encryptMD5(interfaceItemVo.getDataStr()).equalsIgnoreCase(interfaceItemVo.getDataHash())) {
                                        interfaceItemVo.setIsNew(1);
                                        syncCount++;
                                    } else {
                                        interfaceItemVo.setIsNew(0);
                                    }
                                } else {
                                    interfaceItemVo.setIsNew(1);
                                    syncCount++;
                                }
                            } else {
                                errorCount += 1;
                                interfaceItemVo.setIsNew(0);
                            }
                            interfaceItemMapper.updateInterfaceItem(interfaceItemVo);
                        }
                        index += 1;
                    }
                    customViewConditionVo.setCurrentPage(customViewConditionVo.getCurrentPage() + 1);
                    customViewDataList = customViewDataCrossoverService.searchCustomViewDataFlatten(customViewConditionVo);
                }
                //删除isDelete=1并且isImported=0的数据
                interfaceItemMapper.deleteInterfaceItemByInterfaceIdAndCustomViewId(interfaceVo.getId(), interfaceVo.getCustomViewId());
            }
        }
        if (syncCount <= 0 && errorCount > 0) {
            throw new InterfaceItemIrregularException(errorCount);
        }
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("syncCount", syncCount);
        return jsonObj.toJSONString();
    }

}

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

package neatlogic.framework.pbc.dto;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.restful.annotation.EntityField;
import neatlogic.framework.util.I18nUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class InterfaceVo extends BasePageVo {
    public enum Status {
        VALIDATING("validating", "enum.pbc.status.validating"),
        REPORTING("reporting", "enum.pbc.status.reporting"),
        MAPPING("mapping", "enum.pbc.status.mapping");
        private final String type;
        private final String text;

        Status(String _type, String _text) {
            this.type = _type;
            this.text = _text;
        }

        public String getValue() {
            return type;
        }

        public String getText() {
            return I18nUtils.getMessage(text);
        }

        public static String getText(String name) {
            for (InterfaceVo.Status s : InterfaceVo.Status.values()) {
                if (s.getValue().equals(name)) {
                    return s.getText();
                }
            }
            return "";
        }
    }

    public enum Priority {
        VIEW("view", "enum.pbc.priority.view"), CI("ci", "enum.pbc.priority.ci");
        private final String type;
        private final String text;

        Priority(String _type, String _text) {
            this.type = _type;
            this.text = _text;
        }

        public String getValue() {
            return type;
        }

        public String getText() {
            return I18nUtils.getMessage(text);
        }

        public static String getText(String name) {
            for (Priority s : Priority.values()) {
                if (s.getValue().equals(name)) {
                    return s.getText();
                }
            }
            return "";
        }
    }

    @EntityField(name = "数字id", type = ApiParamType.INTEGER)
    private Integer uid;
    @EntityField(name = "人行提供的接口id，是英文单词", type = ApiParamType.STRING)
    private String id;
    @EntityField(name = "名称", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "cmdb自定义视图id", type = ApiParamType.LONG)
    private Long customViewId;
    @EntityField(name = "cmdb自定义视图名称", type = ApiParamType.STRING)
    private String customViewName;
    @EntityField(name = "cmdb模型id", type = ApiParamType.LONG)
    private Long ciId;
    @EntityField(name = "cmdb模型唯一标识", type = ApiParamType.LONG)
    private String ciName;
    @EntityField(name = "cmdb模型名称", type = ApiParamType.LONG)
    private String ciLabel;
    @EntityField(name = "映射优先级", type = ApiParamType.ENUM, member = Priority.class)
    private String priority = Priority.CI.getValue();
    @EntityField(name = "属性列表", type = ApiParamType.JSONARRAY)
    private List<PropertyVo> propertyList;

    @EntityField(name = "机构规则列表", type = ApiParamType.JSONARRAY)
    private List<InterfaceCorporationVo> corporationRuleList;
    @EntityField(name = "状态", type = ApiParamType.ENUM, member = Status.class)
    private String status;
    @EntityField(name = "状态名称", type = ApiParamType.STRING)
    private String statusText;
    @EntityField(name = "操作时间", type = ApiParamType.LONG)
    private Date actionTime;
    @EntityField(name = "同步异常", type = ApiParamType.STRING)
    private String error;
    private String priKey;
    @EntityField(name = "最后同步时间", type = ApiParamType.LONG)
    private Date lastReportTime;
    @EntityField(name = "数据列表", type = ApiParamType.JSONARRAY)
    private List<InterfaceItemVo> interfaceItemList;
    private int handlerFailCount;
    @EntityField(name = "关联策略", type = ApiParamType.JSONARRAY)
    private List<PolicyVo> policyList;
    @EntityField(name = "数据量", type = ApiParamType.INTEGER)
    private Integer itemCount;
    @EntityField(name = "是否关联配置项项模型", type = ApiParamType.INTEGER)
    private Integer hasCi;
    @EntityField(name = "是否关联自定义视图", type = ApiParamType.INTEGER)
    private Integer hasCustomView;

    public List<InterfaceCorporationVo> getCorporationRuleList() {
        return corporationRuleList;
    }

    public void setCorporationRuleList(List<InterfaceCorporationVo> corporationRuleList) {
        this.corporationRuleList = corporationRuleList;
    }

    public void addProperty(PropertyVo propertyVo) {
        if (propertyList == null) {
            propertyList = new ArrayList<>();
        }
        if (propertyVo != null && !propertyList.contains(propertyVo)) {
            propertyList.add(propertyVo);
        }
    }

    public Integer getHasCustomView() {
        return hasCustomView;
    }

    public void setHasCustomView(Integer hasCustomView) {
        this.hasCustomView = hasCustomView;
    }

    public Integer getHasCi() {
        return hasCi;
    }

    public void setHasCi(Integer hasCi) {
        this.hasCi = hasCi;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCiLabel() {
        return ciLabel;
    }

    public void setCiLabel(String ciLabel) {
        this.ciLabel = ciLabel;
    }

    public Long getCustomViewId() {
        return customViewId;
    }

    public void setCustomViewId(Long customViewId) {
        this.customViewId = customViewId;
    }

    public String getCustomViewName() {
        return customViewName;
    }

    public void setCustomViewName(String customViewName) {
        this.customViewName = customViewName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<InterfaceItemVo> getInterfaceItemList() {
        return interfaceItemList;
    }

    public void setInterfaceItemList(List<InterfaceItemVo> interfaceItemList) {
        this.interfaceItemList = interfaceItemList;
    }

    public Integer getUid() {
        if (uid == null && StringUtils.isNotBlank(this.id)) {
            uid = Objects.hash(this.id);
        }
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public List<PropertyVo> getPropertyList() {
        return propertyList;
    }

    public void setPropertyList(List<PropertyVo> propertyList) {
        this.propertyList = propertyList;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusText() {
        if (StringUtils.isNotBlank(status) && StringUtils.isBlank(statusText)) {
            statusText = Status.getText(status);
        }
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public Date getActionTime() {
        return actionTime;
    }

    public void setActionTime(Date actionTime) {
        this.actionTime = actionTime;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getCiId() {
        return ciId;
    }

    public void setCiId(Long ciId) {
        this.ciId = ciId;
    }

    public String getCiName() {
        return ciName;
    }

    public void setCiName(String ciName) {
        this.ciName = ciName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public Date getLastReportTime() {
        return lastReportTime;
    }

    public void setLastReportTime(Date lastReportTime) {
        this.lastReportTime = lastReportTime;
    }

    public Integer getHandlerFailCount() {
        return handlerFailCount;
    }

    public void setHandlerFailCount(Integer handlerFailCount) {
        this.handlerFailCount = handlerFailCount;
    }

    public List<PolicyVo> getPolicyList() {
        return policyList;
    }

    public void setPolicyList(List<PolicyVo> policyList) {
        this.policyList = policyList;
    }
}

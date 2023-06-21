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
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.framework.pbc.policy.core.IPhaseHandler;
import neatlogic.framework.pbc.policy.core.PhaseHandlerFactory;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PolicyVo extends BasePageVo {

    @EntityField(name = "id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "名称", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "描述", type = ApiParamType.STRING)
    private String description;
    @EntityField(name = "是否激活", type = ApiParamType.INTEGER)
    private Integer isActive;
    @EntityField(name = "接口数量", type = ApiParamType.INTEGER)
    private Integer interfaceCount;
    @EntityField(name = "时间计划", type = ApiParamType.STRING)
    private String cronExpression;
    @EntityField(name = "接口列表", type = ApiParamType.JSONARRAY)
    private List<String> interfaceList;
    @EntityField(name = "执行次数", type = ApiParamType.INTEGER)
    private int execCount;
    @EntityField(name = "最后一次执行时间", type = ApiParamType.LONG)
    private Date lastExecDate;
    @EntityField(name = "需要执行的阶段范围", type = ApiParamType.STRING)
    private String phase;
    @EntityField(name = "需要执行的阶段范围文本", type = ApiParamType.STRING)
    private String phaseText;
    @EntityField(name = "高级配置", type = ApiParamType.JSONOBJECT)
    private JSONObject config;
    @JSONField(serialize = false)
    private String configStr;
    @EntityField(name = "机构id", type = ApiParamType.LONG)
    private Long corporationId;
    @EntityField(name = "机构名称", type = ApiParamType.STRING)
    private String corporationName;
    @EntityField(name = "阶段名称列表", type = ApiParamType.STRING)
    private List<String> phaseTextList;

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }

    public Long getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(Long corporationId) {
        this.corporationId = corporationId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCorporationName() {
        return corporationName;
    }

    public void setCorporationName(String corporationName) {
        this.corporationName = corporationName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getInterfaceCount() {
        return interfaceCount;
    }

    public void setInterfaceCount(Integer interfaceCount) {
        this.interfaceCount = interfaceCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getIsActive() {
        return isActive;
    }

    public void setIsActive(Integer isActive) {
        this.isActive = isActive;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public List<String> getInterfaceList() {
        return interfaceList;
    }

    public void setInterfaceList(List<String> interfaceList) {
        this.interfaceList = interfaceList;
    }

    public int getExecCount() {
        return execCount;
    }

    public void setExecCount(int execCount) {
        this.execCount = execCount;
    }

    public Date getLastExecDate() {
        return lastExecDate;
    }

    public void setLastExecDate(Date lastExecDate) {
        this.lastExecDate = lastExecDate;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getPhaseText() {
        phaseText = "";
        if (StringUtils.isNotBlank(phase)) {
            String[] phases = phase.split(",");
            for (String p : phases) {
                IPhaseHandler handler = PhaseHandlerFactory.getHandler(p);
                if (handler != null) {
                    if (StringUtils.isNotBlank(phaseText)) {
                        phaseText += " -> ";
                    }
                    phaseText += handler.getPhaseLabel();
                }
            }
        }
        return phaseText;
    }

    public List<String> getPhaseTextList() {
        List<String> phaseTextList = new ArrayList<>();
        if (StringUtils.isNotBlank(phase)) {
            String[] phases = phase.split(",");
            for (String p : phases) {
                IPhaseHandler handler = PhaseHandlerFactory.getHandler(p);
                if (handler != null) {
                    phaseTextList.add(handler.getPhaseLabel());
                }
            }
        }
        return phaseTextList;
    }

    public void setPhaseTextList(List<String> phaseTextList) {
        this.phaseTextList = phaseTextList;
    }

    public JSONObject getConfig() {
        if (config == null && StringUtils.isNotBlank(configStr)) {
            try {
                config = JSONObject.parseObject(configStr);
            } catch (Exception ignored) {

            }
        }
        return config;
    }

    public void setConfig(JSONObject config) {
        this.config = config;
        this.configStr = null;
    }

    public String getConfigStr() {
        if (StringUtils.isBlank(configStr) && config != null) {
            configStr = config.toString();
        }
        return configStr;
    }

    public void setConfigStr(String configStr) {
        this.configStr = configStr;
    }

}

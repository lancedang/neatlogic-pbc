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

import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.InputFrom;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.restful.annotation.EntityField;
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.module.pbc.enums.Status;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

public class PolicyAuditVo extends BasePageVo {
    public static class PolicyPhase {
        private PolicyPhase prevPhase;
        private PolicyPhase nextPhase;
        private final String phase;

        public PolicyPhase(String phase) {
            this.phase = phase;
        }

        public String getPhase() {
            return this.phase;
        }

        public PolicyPhase setNextPhase(PolicyPhase nextPolicyPhase) {
            this.nextPhase = nextPolicyPhase;
            nextPolicyPhase.prevPhase = this;
            return this;
        }

        public PolicyPhase getPrevPhase() {
            return prevPhase;
        }

        public void setPrevPhase(PolicyPhase prevPhase) {
            this.prevPhase = prevPhase;
        }

        public PolicyPhase getNextPhase() {
            return this.nextPhase;
        }
    }

    @EntityField(name = "id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "策略id", type = ApiParamType.LONG)
    private Long policyId;
    @EntityField(name = "开始时间", type = ApiParamType.LONG)
    private Date startTime;
    @EntityField(name = "结束时间", type = ApiParamType.LONG)
    private Date endTime;
    @EntityField(name = "状态", type = ApiParamType.STRING)
    private String status;
    @EntityField(name = "状态名称", type = ApiParamType.STRING)
    private String statusText;
    @EntityField(name = "用户id", type = ApiParamType.STRING)
    private String userId;
    @EntityField(name = "异常", type = ApiParamType.STRING)
    private String error;
    @EntityField(name = "阶段列表", type = ApiParamType.JSONARRAY)
    private List<PolicyPhaseVo> phaseList;
    @EntityField(name = "触发方式", type = ApiParamType.ENUM, member = InputFrom.class)
    private String inputFrom;
    @EntityField(name = "触发方式名称", type = ApiParamType.STRING)
    private String inputFromText;
    @EntityField(name = "耗时（毫秒）", type = ApiParamType.LONG)
    private Long timeCost;
    @EntityField(name = "数据量", type = ApiParamType.INTEGER)
    private Integer dataCount;
    @JSONField(serialize = false)
    private Integer serverId;
    @JSONField(serialize = false)//当前阶段
    private PolicyPhase currentPhase;
    @JSONField(serialize = false)
    private List<String> startTimeRange;
    @JSONField(serialize = false)
    private List<String> endTimeRange;
    @JSONField(serialize = false)
    private Boolean hasError;//查询条件，是否有异常
    @EntityField(name = "高级配置", type = ApiParamType.JSONOBJECT)
    private JSONObject config;
    @JSONField(serialize = false)
    private List<Long> idList;//id列表，精确查找用

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }

    public PolicyPhase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(PolicyPhase currentPhase) {
        this.currentPhase = currentPhase;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getServerId() {
        if (serverId == null) {
            serverId = Config.SCHEDULE_SERVER_ID;
        }
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public void setPolicyId(Long policyId) {
        this.policyId = policyId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<PolicyPhaseVo> getPhaseList() {
        return phaseList;
    }

    public void setPhaseList(List<PolicyPhaseVo> phaseList) {
        this.phaseList = phaseList;
    }

    public List<String> getStartTimeRange() {
        return startTimeRange;
    }

    public void setStartTimeRange(List<String> startTimeRange) {
        this.startTimeRange = startTimeRange;
    }

    public List<String> getEndTimeRange() {
        return endTimeRange;
    }

    public void setEndTimeRange(List<String> endTimeRange) {
        this.endTimeRange = endTimeRange;
    }

    public Boolean getHasError() {
        return hasError;
    }

    public void setHasError(Boolean hasError) {
        this.hasError = hasError;
    }

    public String getInputFrom() {
        return inputFrom;
    }

    public void setInputFrom(String inputFrom) {
        this.inputFrom = inputFrom;
    }

    public String getInputFromText() {
        if (StringUtils.isBlank(inputFromText) && StringUtils.isNotBlank(inputFrom)) {
            inputFromText = InputFrom.getText(inputFrom);
        }
        return inputFromText;
    }

    public void setInputFromText(String inputFromText) {
        this.inputFromText = inputFromText;
    }

    public Long getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(Long timeCost) {
        this.timeCost = timeCost;
    }

    public Integer getDataCount() {
        return dataCount;
    }

    public void setDataCount(Integer dataCount) {
        this.dataCount = dataCount;
    }

    public String getStatusText() {
        if (StringUtils.isBlank(statusText) && StringUtils.isNotBlank(status)) {
            statusText = Status.getText(status);
        }
        return statusText;
    }

    public JSONObject getConfig() {
        return config;
    }

    public void setConfig(JSONObject config) {
        this.config = config;
    }

    public List<Long> getIdList() {
        return idList;
    }

    public void setIdList(List<Long> idList) {
        this.idList = idList;
    }
}

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
import neatlogic.framework.pbc.policy.core.IPhaseHandler;
import neatlogic.framework.pbc.policy.core.PhaseHandlerFactory;
import neatlogic.framework.restful.annotation.EntityField;
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.module.pbc.enums.Status;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class PolicyPhaseVo {
    @EntityField(name = "id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "执行记录id", type = ApiParamType.LONG)
    private Long auditId;
    @EntityField(name = "阶段", type = ApiParamType.STRING)
    private String phase;
    @EntityField(name = "名称", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "状态", type = ApiParamType.ENUM, member = Status.class)
    private String status;
    @EntityField(name = "状态名称", type = ApiParamType.ENUM, member = Status.class)
    private String statusText;
    @EntityField(name = "开始时间", type = ApiParamType.LONG)
    private Date startTime;
    @EntityField(name = "结束时间", type = ApiParamType.LONG)
    private Date endTime;
    @EntityField(name = "耗时（毫秒）", type = ApiParamType.LONG)
    private Long timeCost;
    @EntityField(name = "执行结果", type = ApiParamType.STRING)
    private String result;
    @EntityField(name = "异常", type = ApiParamType.STRING)
    private String error;
    @EntityField(name = "顺序", type = ApiParamType.INTEGER)
    private Integer sort;
    @EntityField(name = "上一步执行结果", type = ApiParamType.STRING)
    private String prevResult;
    @EntityField(name = "高级配置", type = ApiParamType.JSONOBJECT)
    private JSONObject config;
    @EntityField(name = "执行次数", type = ApiParamType.INTEGER)
    private int execCount = 0;//执行次数
    @JSONField(serialize = false)
    private int retryCount = 0;//重试次数
    @JSONField(serialize = false)
    private int retryInterval = 0;//重试间隔

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public String getPhase() {
        return phase;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getTimeCost() {
        return timeCost;
    }

    public void setTimeCost(Long timeCost) {
        this.timeCost = timeCost;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusText() {
        if (StringUtils.isBlank(statusText) && StringUtils.isNotBlank(status)) {
            statusText = Status.getText(status);
        }
        return statusText;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public void setResult(String result) {
        this.result = result;
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

    public String getPrevResult() {
        return prevResult;
    }

    public void setPrevResult(String prevResult) {
        this.prevResult = prevResult;
    }

    public String getName() {
        if (StringUtils.isBlank(name) && StringUtils.isNotBlank(phase)) {
            IPhaseHandler handler = PhaseHandlerFactory.getHandler(phase);
            if (handler != null) {
                name = handler.getPhaseLabel();
            }
        }
        return name;
    }

    public JSONObject getConfig() {
        return config;
    }

    public void setConfig(JSONObject config) {
        this.config = config;
    }

    public int getExecCount() {
        return execCount;
    }

    public void setExecCount(int execCount) {
        this.execCount = execCount;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = Math.max(0, retryCount);
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = Math.max(0, retryInterval);
    }
}

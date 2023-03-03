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

package neatlogic.module.pbc.schedule;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.common.constvalue.InputFrom;
import neatlogic.framework.scheduler.core.JobBase;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.framework.pbc.dao.mapper.PolicyMapper;
import neatlogic.framework.pbc.dto.PolicyAuditVo;
import neatlogic.framework.pbc.dto.PolicyVo;
import neatlogic.framework.pbc.exception.PolicyHasNoPhaseException;
import neatlogic.framework.pbc.exception.PolicyNotFoundException;
import neatlogic.framework.pbc.policy.core.PolicyManager;
import org.apache.commons.lang3.StringUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;


@Component
@DisallowConcurrentExecution
public class PbcPolicyExecuteScheduleJob extends JobBase {
    @Resource
    private SchedulerManager schedulerManager;

    @Resource
    private PolicyMapper policyMapper;


    @Override
    public void executeInternal(JobExecutionContext context, JobObject jobObject) {
        Long policyId = (Long) jobObject.getData("policyId");
        PolicyVo policyVo = policyMapper.getPolicyById(policyId);
        if (policyVo == null) {
            throw new PolicyNotFoundException();
        }
        String phase = policyVo.getPhase();
        if (StringUtils.isBlank(phase)) {
            throw new PolicyHasNoPhaseException();
        }
        PolicyAuditVo policyAuditVo = new PolicyAuditVo();
        policyAuditVo.setPolicyId(policyVo.getId());
        policyAuditVo.setInputFrom(InputFrom.PAGE.getValue());
        PolicyAuditVo.PolicyPhase currentPhase = null;
        String[] phases = phase.split(",");
        for (String p : phases) {
            if (currentPhase == null) {
                currentPhase = new PolicyAuditVo.PolicyPhase(p);
                policyAuditVo.setCurrentPhase(currentPhase);
            } else {
                PolicyAuditVo.PolicyPhase nextPhase = new PolicyAuditVo.PolicyPhase(p);
                currentPhase.setNextPhase(nextPhase);
                currentPhase = nextPhase;
            }
        }
        if (policyAuditVo.getCurrentPhase() == null) {
            throw new PolicyHasNoPhaseException();
        }
        PolicyManager.execute(policyAuditVo);
    }


    @Override
    public String getGroupName() {
        return TenantContext.get().getTenantUuid() + "-PBC-POLICY";
    }

    @Override
    public Boolean isMyHealthy(JobObject jobObject) {
        Long policyId = (Long) jobObject.getData("policyId");
        PolicyVo policyVo = policyMapper.getPolicyById(policyId);
        if (policyVo != null && policyVo.getIsActive().equals(1)) {
            return policyVo.getCronExpression().equals(jobObject.getCron());
        }
        return false;
    }

    @Override
    public void reloadJob(JobObject jobObject) {
        String tenantUuid = jobObject.getTenantUuid();
        Long policyId = (Long) jobObject.getData("policyId");
        PolicyVo policyVo = policyMapper.getPolicyById(policyId);
        if (policyVo != null) {
            JobObject newJobObject = new JobObject.Builder(policyVo.getId().toString(), this.getGroupName(), this.getClassName(), tenantUuid).withCron(policyVo.getCronExpression()).addData("policyId", policyVo.getId()).build();
            schedulerManager.loadJob(newJobObject);
        } else {
            schedulerManager.unloadJob(jobObject);
        }
    }

    @Override
    public void initJob(String tenantUuid) {
        List<PolicyVo> policyList = policyMapper.getAllCronPolicy();
        for (PolicyVo policyVo : policyList) {
            JobObject.Builder newJobObjectBuilder = new JobObject.Builder(policyVo.getId().toString(), this.getGroupName(), this.getClassName(), tenantUuid).withCron(policyVo.getCronExpression()).addData("policyId", policyVo.getId());
            JobObject newJobObject = newJobObjectBuilder.build();
            schedulerManager.loadJob(newJobObject);
        }
    }
}

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

package neatlogic.module.pbc.startup.handler;

import neatlogic.framework.common.config.Config;
import neatlogic.framework.pbc.dao.mapper.PolicyMapper;
import neatlogic.framework.pbc.dto.PolicyAuditVo;
import neatlogic.framework.pbc.dto.PolicyPhaseVo;
import neatlogic.framework.startup.StartupBase;
import neatlogic.module.pbc.enums.Status;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ResetPolicyAuditStatusStartupHandler extends StartupBase {
    @Resource
    private PolicyMapper policyMapper;

    @Override
    public String getName() {
        return "重置策略执行记录状态";
    }

    @Override
    public int sort() {
        return 1;
    }

    @Override
    public int executeForCurrentTenant() {
        List<PolicyAuditVo> policyAuditList = policyMapper.getRunningPolicyAuditByServerId(Config.SCHEDULE_SERVER_ID);
        if (CollectionUtils.isNotEmpty(policyAuditList)) {
            for (PolicyAuditVo policyAuditVo : policyAuditList) {
                if (CollectionUtils.isNotEmpty(policyAuditVo.getPhaseList())) {
                    for (PolicyPhaseVo phase : policyAuditVo.getPhaseList()) {
                        if (phase.getStatus().equals(Status.RUNNING.getValue())) {
                            phase.setStatus(Status.FAILED.getValue());
                            phase.setError("系统重启导致执行终止");
                            policyMapper.updatePolicyPhase(phase);
                        }
                    }
                }
                policyAuditVo.setStatus(Status.FAILED.getValue());
                policyAuditVo.setError("系统重启导致执行终止");
                policyMapper.updatePolicyAudit(policyAuditVo);
            }
        }
        return 0;
    }

}

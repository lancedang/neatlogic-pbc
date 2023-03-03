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

package neatlogic.framework.pbc.policy.core;

import neatlogic.framework.asynchronization.thread.NeatLogicThread;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.pbc.dao.mapper.PolicyMapper;
import neatlogic.framework.pbc.dto.PolicyAuditVo;
import neatlogic.framework.pbc.dto.PolicyPhaseVo;
import neatlogic.module.pbc.enums.Status;
import neatlogic.framework.pbc.exception.PolicyHasNoPhaseException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PolicyManager {
    private static PolicyMapper policyMapper;

    @Autowired
    public PolicyManager(PolicyMapper _policyMapper) {
        policyMapper = _policyMapper;
    }

    public static void redo(PolicyAuditVo policyAuditVo, boolean isFromStart) {
        if (CollectionUtils.isNotEmpty(policyAuditVo.getPhaseList())) {
            PolicyAuditVo.PolicyPhase currentPhase = null;
            PolicyPhaseVo prePhaseVo = null;
            for (PolicyPhaseVo policyPhaseVo : policyAuditVo.getPhaseList()) {
                //如果从头开始，则重置所有步骤状态和结果信息
                if (isFromStart) {
                    policyPhaseVo.setError(null);
                    policyPhaseVo.setResult(null);
                    policyPhaseVo.setStatus(Status.PENDING.getValue());
                    policyMapper.updatePolicyPhase(policyPhaseVo);
                }
                if (prePhaseVo != null) {
                    policyPhaseVo.setPrevResult(prePhaseVo.getResult());
                }
                //Phase p = Phase.getByPhase(policyPhaseVo.getPhase());
                //if (p != null) {
                if (currentPhase == null) {
                    currentPhase = new PolicyAuditVo.PolicyPhase(policyPhaseVo.getPhase());
                } else {
                    PolicyAuditVo.PolicyPhase nextPhase = new PolicyAuditVo.PolicyPhase(policyPhaseVo.getPhase());
                    currentPhase.setNextPhase(nextPhase);
                    currentPhase = nextPhase;
                }
                //}
                if ((isFromStart || !policyPhaseVo.getStatus().equals(Status.SUCCESS.getValue())) && policyAuditVo.getCurrentPhase() == null) {
                    policyAuditVo.setCurrentPhase(currentPhase);
                }
                prePhaseVo = policyPhaseVo;
            }
            if (policyAuditVo.getCurrentPhase() == null) {
                throw new PolicyHasNoPhaseException();
            }
            CachedThreadPool.execute(new NeatLogicThread("PBC-POLICY-REDO" + policyAuditVo.getPolicyId() + "-" + policyAuditVo.getId()) {
                @Override
                protected void execute() {
                    try {
                        IPhaseHandler handler = PhaseHandlerFactory.getHandler(policyAuditVo.getCurrentPhase().getPhase());
                        if (handler != null) {
                            handler.execute(policyAuditVo);
                        }
                    } catch (ApiRuntimeException ex) {
                        policyAuditVo.setStatus(Status.FAILED.getValue());
                        policyAuditVo.setError(ex.getMessage());
                        policyMapper.updatePolicyAudit(policyAuditVo);
                    } catch (Exception ex) {
                        policyAuditVo.setStatus(Status.FAILED.getValue());
                        policyAuditVo.setError(ExceptionUtils.getStackTrace(ex));
                        policyMapper.updatePolicyAudit(policyAuditVo);
                    }
                }
            });
        }
    }

    public static void execute(PolicyAuditVo policyAuditVo) {
        policyAuditVo.setStatus(Status.PENDING.getValue());
        policyAuditVo.setUserId(UserContext.get().getUserId());
        policyMapper.insertPolicyAudit(policyAuditVo);
        PolicyAuditVo.PolicyPhase phase = policyAuditVo.getCurrentPhase();
        int sort = 1;
        List<PolicyPhaseVo> phaseList = new ArrayList<>();
        while (phase != null) {
            PolicyPhaseVo policyPhaseVo = new PolicyPhaseVo();
            policyPhaseVo.setStatus(Status.PENDING.getValue());
            policyPhaseVo.setAuditId(policyAuditVo.getId());
            policyPhaseVo.setSort(sort);
            policyPhaseVo.setPhase(phase.getPhase());
            policyMapper.insertPolicyPhase(policyPhaseVo);
            phase = phase.getNextPhase();
            sort += 1;
            phaseList.add(policyPhaseVo);
        }
        policyAuditVo.setPhaseList(phaseList);
        CachedThreadPool.execute(new NeatLogicThread("PBC-POLICY-EXECUTE" + policyAuditVo.getPolicyId() + "-" + policyAuditVo.getId()) {
            @Override
            protected void execute() {
                try {
                    IPhaseHandler handler = PhaseHandlerFactory.getHandler(policyAuditVo.getCurrentPhase().getPhase());
                    if (handler != null) {
                        handler.execute(policyAuditVo);
                    }
                } catch (ApiRuntimeException ex) {
                    policyAuditVo.setStatus(Status.FAILED.getValue());
                    policyAuditVo.setError(ex.getMessage());
                    policyMapper.updatePolicyAudit(policyAuditVo);
                } catch (Exception ex) {
                    policyAuditVo.setStatus(Status.FAILED.getValue());
                    policyAuditVo.setError(ExceptionUtils.getStackTrace(ex));
                    policyMapper.updatePolicyAudit(policyAuditVo);
                }
            }
        });
    }
}

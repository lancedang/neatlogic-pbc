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
import neatlogic.framework.asynchronization.threadpool.CachedThreadPool;
import neatlogic.framework.asynchronization.threadpool.ScheduledThreadPool;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dao.mapper.InterfaceMapper;
import neatlogic.framework.pbc.dao.mapper.PolicyMapper;
import neatlogic.framework.pbc.dto.InterfaceVo;
import neatlogic.framework.pbc.dto.PolicyAuditVo;
import neatlogic.framework.pbc.dto.PolicyPhaseVo;
import neatlogic.framework.pbc.dto.PolicyVo;
import neatlogic.module.pbc.enums.Status;
import neatlogic.framework.pbc.exception.PhaseHandlerNotFoundException;
import neatlogic.framework.pbc.exception.PolicyPhaseNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

public abstract class PhaseHandlerBase implements IPhaseHandler {
    private static final Logger logger = LoggerFactory.getLogger(PhaseHandlerBase.class);
    protected static PolicyMapper policyMapper;

    protected static InterfaceMapper interfaceMapper;

    protected static InterfaceItemMapper interfaceItemMapper;

    @Resource
    public void setPolicyMapper(PolicyMapper _policyMapper) {
        policyMapper = _policyMapper;
    }

    @Resource
    public void setInterfaceMapper(InterfaceMapper _interfaceMapper) {
        interfaceMapper = _interfaceMapper;
    }

    @Resource
    public void setInterfaceItemMapper(InterfaceItemMapper _interfaceItemMapper) {
        interfaceItemMapper = _interfaceItemMapper;
    }

    @Override
    public final void execute(PolicyAuditVo policyAuditVo) {
        execute(policyAuditVo, false);
    }

    @Override
    public void execute(PolicyAuditVo policyAuditVo, boolean isRetry) {
        Optional<PolicyPhaseVo> op = policyAuditVo.getPhaseList().stream().filter(d -> d.getPhase().equalsIgnoreCase(this.getPhase())).findFirst();
        PolicyPhaseVo policyPhaseVo;
        if (op.isPresent()) {
            policyPhaseVo = op.get();
        } else {
            throw new PolicyPhaseNotFoundException(this.getPhaseLabel());
        }

        //提取对应步骤的配置
        int delay = 0;
        if (!isRetry && MapUtils.isNotEmpty(policyAuditVo.getConfig())) {
            if (policyAuditVo.getConfig().containsKey("phaseConfig") && policyAuditVo.getConfig().get("phaseConfig") instanceof JSONObject) {
                JSONObject p = policyAuditVo.getConfig().getJSONObject("phaseConfig");
                if (p.containsKey(policyPhaseVo.getPhase()) && p.get(policyPhaseVo.getPhase()) instanceof JSONObject) {
                    policyPhaseVo.setConfig(p.getJSONObject(policyPhaseVo.getPhase()));
                    delay = policyPhaseVo.getConfig().getIntValue("delay");
                    //需要加上已经执行的次数
                    policyPhaseVo.setRetryCount(policyPhaseVo.getConfig().getIntValue("retryCount") + (!isRetry ? policyPhaseVo.getExecCount() : 0));
                    policyPhaseVo.setRetryInterval(policyPhaseVo.getConfig().getIntValue("retryInterval"));
                }
            }
        }
        //第一次执行才需要延时
        if (policyPhaseVo.getExecCount() == 0) {
            delay = Math.max(0, delay);
        } else {
            delay = 0;
        }
        PolicyPhaseVo finalPolicyPhaseVo = policyPhaseVo;

        //更新操作记录状态为执行中
        PolicyAuditVo auditStatusVo = policyMapper.getPolicyAuditById(policyAuditVo.getId());
        if (!auditStatusVo.getStatus().equals(Status.RUNNING.getValue())) {
            policyAuditVo.setStatus(Status.RUNNING.getValue());
            policyMapper.updatePolicyAudit(policyAuditVo);
        }
        //修改步骤状态为已就绪
        PolicyPhaseVo phaseStatusVo = policyMapper.getPolicyPhaseByAuditIdAndPhase(policyAuditVo.getId(), this.getPhase());
        if (!phaseStatusVo.getStatus().equals(Status.RUNNING.getValue())) {
            phaseStatusVo.setStatus(Status.PENDING.getValue());
            policyMapper.updatePolicyPhase(phaseStatusVo);
        }

        ScheduledThreadPool.execute(new NeatLogicThread("PBC-POLICY-DELAY-HANDLER") {
            @Override
            protected void execute() {
                CachedThreadPool.execute(new NeatLogicThread("PBC-POLICY-HANDLER") {
                    @Override
                    protected void execute() {
                        executeCurrentPhase(policyAuditVo, finalPolicyPhaseVo);
                    }
                });
            }
        }, isRetry ? 0L : delay * 60L);
    }

    private void executeCurrentPhase(PolicyAuditVo policyAuditVo, PolicyPhaseVo policyPhaseVo) {
        policyPhaseVo.setExecCount(policyPhaseVo.getExecCount() + 1);
        PolicyPhaseVo phaseStatusVo = policyMapper.getPolicyPhaseByAuditIdAndPhase(policyAuditVo.getId(), this.getPhase());
        if (!phaseStatusVo.getStatus().equals(Status.RUNNING.getValue())) {
            policyPhaseVo.setStatus(Status.RUNNING.getValue());
            policyMapper.updatePolicyPhase(policyPhaseVo);
        }
        //获取上一阶段的处理结果
        if (policyAuditVo.getCurrentPhase().getPrevPhase() != null) {
            Optional<PolicyPhaseVo> prevOp = policyAuditVo.getPhaseList().stream().filter(d -> d.getPhase().equalsIgnoreCase(policyAuditVo.getCurrentPhase().getPrevPhase().getPhase())).findFirst();
            prevOp.ifPresent(phaseVo -> policyPhaseVo.setPrevResult(phaseVo.getResult()));
        }

        try {
            List<InterfaceVo> interfaceList = interfaceMapper.getInterfaceByPolicyId(policyAuditVo.getPolicyId());
            PolicyVo policyVo = policyMapper.getPolicyById(policyAuditVo.getPolicyId());
            String result = myExecute(policyAuditVo, policyVo, policyPhaseVo, interfaceList);
            policyPhaseVo.setResult(result);
            policyPhaseVo.setStatus(Status.SUCCESS.getValue());
            policyPhaseVo.setError(null);
        } catch (ApiRuntimeException ex) {
            policyPhaseVo.setError(ex.getMessage());
            policyPhaseVo.setStatus(Status.FAILED.getValue());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            policyPhaseVo.setError(ExceptionUtils.getStackTrace(ex));
            policyPhaseVo.setStatus(Status.FAILED.getValue());
        }

        if (policyPhaseVo.getStatus().equals(Status.FAILED.getValue())) {
            if (policyPhaseVo.getRetryCount() > policyPhaseVo.getExecCount() - 1) {
                IPhaseHandler handler = this;
                ScheduledThreadPool.execute(new NeatLogicThread("PBC-POLICY-DELAY-HANDLER") {
                    @Override
                    protected void execute() {
                        CachedThreadPool.execute(new NeatLogicThread("PBC-POLICY-HANDLER") {
                            @Override
                            protected void execute() {
                                handler.execute(policyAuditVo, true);
                            }
                        });
                    }
                }, policyPhaseVo.getRetryInterval() * 60L);
            } else {
                policyMapper.updatePolicyPhase(policyPhaseVo);

                policyAuditVo.setStatus(Status.FAILED.getValue());
                policyAuditVo.setError(policyPhaseVo.getError());
                policyMapper.updatePolicyAudit(policyAuditVo);
            }
        } else if (policyPhaseVo.getStatus().equals(Status.SUCCESS.getValue())) {
            policyMapper.updatePolicyPhase(policyPhaseVo);

            PolicyAuditVo.PolicyPhase newPolicyPhase = policyAuditVo.getCurrentPhase().getNextPhase();
            if (newPolicyPhase != null) {
                policyAuditVo.setCurrentPhase(newPolicyPhase);
                IPhaseHandler handler = PhaseHandlerFactory.getHandler(newPolicyPhase.getPhase());
                if (handler == null) {
                    throw new PhaseHandlerNotFoundException(newPolicyPhase.getPhase());
                }
                handler.execute(policyAuditVo);
            } else {
                policyAuditVo.setStatus(Status.SUCCESS.getValue());
                policyMapper.updatePolicyAudit(policyAuditVo);
            }
        }
    }

    protected abstract String myExecute(PolicyAuditVo policyAuditVo, PolicyVo policyVo, PolicyPhaseVo policyPhaseVo, List<InterfaceVo> interfaceList);


}

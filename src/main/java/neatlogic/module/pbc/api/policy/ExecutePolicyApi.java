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

package neatlogic.module.pbc.api.policy;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.constvalue.InputFrom;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.PolicyMapper;
import neatlogic.framework.pbc.dto.PolicyAuditVo;
import neatlogic.framework.pbc.dto.PolicyVo;
import neatlogic.framework.pbc.exception.PolicyHasNoPhaseException;
import neatlogic.framework.pbc.exception.PolicyNotFoundException;
import neatlogic.framework.pbc.policy.core.PolicyManager;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class ExecutePolicyApi extends PrivateApiComponentBase {

    @Resource
    private PolicyMapper policyMapper;

    @Override
    public String getToken() {
        return "/pbc/policy/execute";
    }

    @Override
    public String getName() {
        return "执行同步策略";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "id，不提供代表新增")})
    @Description(desc = "执行同步策略接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        PolicyVo policyVo = policyMapper.getPolicyById(paramObj.getLong("id"));
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
        policyAuditVo.setConfig(policyVo.getConfig());
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
        return null;
    }
}

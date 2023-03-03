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
import neatlogic.framework.pbc.exception.PolicyAuditNotFoundException;
import neatlogic.framework.pbc.exception.PolicyNotFoundException;
import neatlogic.framework.pbc.policy.core.PolicyManager;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.OPERATE)
public class RedoPolicyApi extends PrivateApiComponentBase {

    @Resource
    private PolicyMapper policyMapper;

    @Override
    public String getToken() {
        return "/pbc/policy/redo";
    }

    @Override
    public String getName() {
        return "重新执行同步策略";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "auditId", type = ApiParamType.LONG, isRequired = true, desc = "操作记录id"),
            @Param(name = "isFromStart", type = ApiParamType.BOOLEAN, isRequired = true, desc = "是否从头开始执行，如果否则从中断位置开始执行")})
    @Description(desc = "重新执行同步策略接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        PolicyAuditVo policyAuditVo = policyMapper.getPolicyAuditById(paramObj.getLong("auditId"));
        boolean isFromStart = paramObj.getBoolean("isFromStart");
        if (policyAuditVo == null) {
            throw new PolicyAuditNotFoundException(paramObj.getLong("auditId"));
        }
        PolicyVo policyVo = policyMapper.getPolicyById(policyAuditVo.getPolicyId());
        if (policyVo == null) {
            throw new PolicyNotFoundException();
        }
        policyAuditVo.setConfig(policyVo.getConfig());
        if (CollectionUtils.isNotEmpty(policyAuditVo.getPhaseList())) {
            PolicyManager.redo(policyAuditVo, isFromStart);
        }
        return null;
    }
}

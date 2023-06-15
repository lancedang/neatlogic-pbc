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

package neatlogic.module.pbc.policy.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.pbc.dto.*;
import neatlogic.framework.pbc.exception.LoginFailedException;
import neatlogic.framework.pbc.exception.PhaseException;
import neatlogic.framework.pbc.exception.ReportResultLackParamException;
import neatlogic.framework.pbc.exception.ReportResultNotFoundException;
import neatlogic.framework.pbc.policy.core.PhaseHandlerBase;
import neatlogic.framework.util.HttpRequestUtil;
import neatlogic.module.pbc.utils.ConfigManager;
import neatlogic.module.pbc.utils.TokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SelectUploadDataPhaseHandler extends PhaseHandlerBase {
    @Override
    public String getPhase() {
        return "selectdata";
    }

    @Override
    public String getPhaseLabel() {
        return "查询数据处理状态";
    }

    @Override
    public List<PolicyConfigVo> getConfigTemplate() {
        return null;
    }

    @Override
    protected String myExecute(PolicyAuditVo policyAuditVo, PolicyVo policyVo, PolicyPhaseVo policyPhaseVo, List<InterfaceVo> interfaceList) {
        PolicyPhaseVo reportPhaseVo = policyMapper.getPolicyPhaseByAuditIdAndPhase(policyAuditVo.getId(), "report");
        if (reportPhaseVo == null || StringUtils.isBlank(reportPhaseVo.getResult())) {
            throw new ReportResultNotFoundException();
        }
        JSONObject jsonObj = JSONObject.parseObject(reportPhaseVo.getResult());
        if (StringUtils.isBlank(jsonObj.getString("branchId"))) {
            throw new ReportResultLackParamException("branchId");
        }
        String branchId = jsonObj.getString("branchId");
        JSONObject data = new JSONObject();
        data.put("facilityOwnerAgency", ConfigManager.getConfig(policyVo.getCorporationId()).getFacilityOwnerAgency());
        data.put("branchId", branchId);
        return sendData(policyVo.getCorporationId(), data);
    }

    private String sendData(Long corporationId, JSONObject reportData) {
        String token = TokenUtil.getToken(corporationId);
        if (StringUtils.isBlank(token)) {
            throw new LoginFailedException();
        }
        HttpRequestUtil httpRequestUtil = HttpRequestUtil.post(ConfigManager.getConfig(corporationId).getSelectDataUrl()).setTenant(TenantContext.get().getTenantUuid()).addHeader("X-Access-Token", token).setAuthType(AuthenticateType.BASIC).setUsername("techsure").setPassword("x15wDEzSbBL6tV1W").setPayload(reportData.toJSONString()).sendRequest();
        if (StringUtils.isNotBlank(httpRequestUtil.getError())) {
            throw new PhaseException(httpRequestUtil.getError());
        } else {
            return httpRequestUtil.getResult();
        }
    }
}
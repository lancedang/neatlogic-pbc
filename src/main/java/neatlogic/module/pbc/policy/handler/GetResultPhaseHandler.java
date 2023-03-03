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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GetResultPhaseHandler extends PhaseHandlerBase {
    @Override
    public String getPhase() {
        return "getresult";
    }

    @Override
    public String getPhaseLabel() {
        return "获取数据核验结果";
    }

    @Override
    public List<PolicyConfigVo> getConfigTemplate() {
        List<PolicyConfigVo> configList = new ArrayList<>();
        configList.add(new PolicyConfigVo("retryCount", "重试次数", "number"));//重试次数
        configList.add(new PolicyConfigVo("retryInterval", "重试间隔(分钟)", "number"));//重试间隔
        configList.add(new PolicyConfigVo("delay", "延迟执行(分钟)", "number"));//延迟执行，单位分钟
        return configList;
    }

    private static final Map<String, String> codeMap = new HashMap<>();

    static {
        codeMap.put("WL-10005", "已保存");
        codeMap.put("WL-10006", "处理中");
        codeMap.put("WL-10007", "处理失败");
        codeMap.put("WL-10008", "部分数据异常");
        codeMap.put("WL-10009", "处理成功");
        codeMap.put("WL-10011", "上报数据不能为空");
        codeMap.put("WL-10012", "上报数据中缺少branchId字段");
        codeMap.put("WL-10013", "上报数据中缺少facilityOwnerAgency字段");
        codeMap.put("WL-10014", "上报数据中缺少data字段");
    }

    /**
     * 返回数据范例
     * <p>
     * {
     * "branchId": "5f11d9471e33ff0ec08b970c",
     * "code": "WL-10008",
     * "msg": "部分失败",
     * "data": [{
     * "code": "WL-20001",
     * "msg": "[facilityUsedState]不在填报范围",
     * "facilityCategory": "FAITSERPCS",
     * "facilityDescriptor": "5f11db861e33ff0ec08ba546"
     * },
     * {
     * "code": "WL-20002 ",
     * "msg": "数据处理失败",
     * "facilityCategory": "FAITSERPCS",
     * "facilityDescriptor": "5f14ff501e33ff0ec08dd312"
     * }
     * ]
     * }
     */

    @Override
    protected String myExecute(PolicyAuditVo policyAuditVo, PolicyVo policyVo, PolicyPhaseVo policyPhaseVo, List<InterfaceVo> interfaceList) {
        PolicyPhaseVo reportPhaseVo = policyMapper.getPolicyPhaseByAuditIdAndPhase(policyAuditVo.getId(), "report");
        //System.out.println("执行" + policyPhaseVo.getExecCount() + "次");
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
        String result = sendData(policyVo.getCorporationId(), data);
        JSONObject resultObj = JSONObject.parseObject(result);
        if (resultObj.getString("code").equals("WL-10009")) {
            List<Long> deleteInterfaceIdList = interfaceItemMapper.getNeedDeleteInterfaceItemIdByAuditId(policyAuditVo.getId());
            if (CollectionUtils.isNotEmpty(deleteInterfaceIdList)) {
                for (Long id : deleteInterfaceIdList) {
                    interfaceItemMapper.deleteInterfaceItemById(id);
                }
            }
            //更新policy的last_action_date字段
            policyMapper.updatePolicyLastExecDate(policyAuditVo.getPolicyId());
        }
        //if (policyPhaseVo.getExecCount() <= 2) {
        //throw new ApiRuntimeException("故意抛出的异常");
        //}
        return result;
    }

    private String sendData(Long corporationId, JSONObject reportData) {
        String token = TokenUtil.getToken(corporationId);
        if (StringUtils.isBlank(token)) {
            throw new LoginFailedException();
        }
        HttpRequestUtil httpRequestUtil = HttpRequestUtil.post(ConfigManager.getConfig(corporationId).getValidResultUrl())
                .setTenant(TenantContext.get().getTenantUuid())
                .addHeader("X-Access-Token", token)
                .setAuthType(AuthenticateType.BASIC)
                .setUsername("techsure")
                .setPassword("x15wDEzSbBL6tV1W")
                .setPayload(reportData.toJSONString())
                .sendRequest();
        if (StringUtils.isNotBlank(httpRequestUtil.getError())) {
            throw new PhaseException(httpRequestUtil.getError());
        } else {
            return httpRequestUtil.getResult();
        }
    }

}

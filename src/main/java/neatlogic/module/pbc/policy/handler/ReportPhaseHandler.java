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

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.integration.authentication.enums.AuthenticateType;
import neatlogic.framework.pbc.dto.*;
import neatlogic.framework.util.GzipUtil;
import neatlogic.framework.util.HttpRequestUtil;
import neatlogic.framework.pbc.exception.LoginFailedException;
import neatlogic.framework.pbc.exception.NoDataToReportException;
import neatlogic.framework.pbc.exception.PhaseException;
import neatlogic.framework.pbc.policy.core.PhaseHandlerBase;
import neatlogic.module.pbc.utils.ConfigManager;
import neatlogic.module.pbc.utils.TokenUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportPhaseHandler extends PhaseHandlerBase {
    private final Logger logger = LoggerFactory.getLogger(ReportPhaseHandler.class);

    @Override
    public String getPhase() {
        return "report";
    }

    @Override
    public String getPhaseLabel() {
        return "发送批量数据元";
    }

    @Override
    public List<PolicyConfigVo> getConfigTemplate() {
        return null;
    }
    /*
    接收成功示例：
{
	"branchId": "51244123381A487798AF515092A008C3",
	"code": "WL-10000",
	"msg": "接收成功"
}
接收异常示例：
{
	"branchId": "51244123381A487798AF515092A008C3",
	"code": "WL-10001",
	"msg": "机构编号不能为空"
}

     */

    @Override
    protected String myExecute(PolicyAuditVo policyAuditVo, PolicyVo policyVo, PolicyPhaseVo policyPhaseVo, List<InterfaceVo> interfaceList) {
        List<InterfaceVo> interfaceAndItemList = interfaceItemMapper.getInterfaceItemByAuditId(policyAuditVo.getId());
        if (CollectionUtils.isEmpty(interfaceAndItemList)) {
            throw new NoDataToReportException();
        }
        JSONObject reportData = new JSONObject();
        reportData.put("facilityOwnerAgency", ConfigManager.getConfig(policyVo.getCorporationId()).getFacilityOwnerAgency());
        //如果有结果代表不是第一次执行
        if (StringUtils.isNotBlank(policyPhaseVo.getResult())) {
            try {
                JSONObject result = JSONObject.parseObject(policyPhaseVo.getResult());
                reportData.put("branchId", result.getString("branchId"));
            } catch (Exception ignored) {

            }
        }
        JSONArray dataList = new JSONArray();

        for (InterfaceVo interfaceVo : interfaceAndItemList) {
            JSONObject interfaceData = new JSONObject();
            interfaceData.put("dataType", interfaceVo.getId());
            JSONArray interfaceDataList = new JSONArray();
            interfaceData.put("dataList", interfaceDataList);
            if (CollectionUtils.isNotEmpty(interfaceVo.getInterfaceItemList())) {
                for (InterfaceItemVo interfaceItemVo : interfaceVo.getInterfaceItemList()) {
                    interfaceDataList.add(interfaceItemVo.getData());
                }
            }
            dataList.add(interfaceData);
        }
        reportData.put("data", GzipUtil.compress(dataList.toJSONString()));
        //reportData.put("data", dataList.toJSONString());
        String result = sendReportData(policyVo.getCorporationId(), reportData);
        JSONObject resultObj = JSONObject.parseObject(result);
        //处理成功需要修改interfaceItem状态
        if (resultObj.getString("code").equals("WL-10000")) {
            interfaceItemMapper.updateInterfaceItemDataHashByAuditId(policyAuditVo.getId());
        }
        return result;
    }


    private String sendReportData(Long corporationId, JSONObject reportData) {
        String token = TokenUtil.getToken(corporationId);
        if (StringUtils.isBlank(token)) {
            throw new LoginFailedException();
        }
        HttpRequestUtil httpRequestUtil = HttpRequestUtil.post(ConfigManager.getConfig(corporationId).getReportUrl()).setTenant(TenantContext.get().getTenantUuid()).addHeader("X-Access-Token", token).setAuthType(AuthenticateType.BASIC).setUsername("techsure").setPassword("x15wDEzSbBL6tV1W").setPayload(reportData.toJSONString()).sendRequest();
        if (StringUtils.isNotBlank(httpRequestUtil.getError())) {
            throw new PhaseException(httpRequestUtil.getError());
        } else {
            return httpRequestUtil.getResult();
        }
    }

}

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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.pbc.dto.*;
import neatlogic.framework.pbc.exception.*;
import neatlogic.framework.pbc.policy.core.PhaseHandlerBase;
import neatlogic.framework.util.HttpRequestUtil;
import neatlogic.module.pbc.utils.ConfigManager;
import neatlogic.module.pbc.utils.TokenUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SelectUploadDataPhaseHandler extends PhaseHandlerBase {
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
        List<PolicyConfigVo> configList = new ArrayList<>();
        configList.add(new PolicyConfigVo("retryCount", "重试次数", "number"));//重试次数
        configList.add(new PolicyConfigVo("retryInterval", "重试间隔(分钟)", "number"));//重试间隔
        configList.add(new PolicyConfigVo("delay", "延迟执行(分钟)", "number"));//延迟执行，单位分钟
        return configList;
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
        String resultStr = sendData(policyVo.getCorporationId(), data);
        JSONObject returnObj = JSONObject.parseObject(resultStr);
        if (returnObj != null && returnObj.containsKey("code")) {
            String msg = returnObj.getString("msg");
            String code = returnObj.getString("code");
            if ("WL-10009".equalsIgnoreCase(code) || "WL-10013".equalsIgnoreCase(code)) {//处理成功 || 处理成功但部分数据存在警告
                interfaceItemMapper.updateInterfaceItemDataHashByAuditId(policyAuditVo.getId());
            } else if ("WL-10007".equalsIgnoreCase(code)) {//处理失败
                throw new PhaseException(returnObj);
            } else if ("WL-10008".equalsIgnoreCase(code)) {//部分处理失败  需更新各自item的状态
                JSONArray dataList = returnObj.getJSONArray("data");
                if (CollectionUtils.isNotEmpty(dataList)) {
                    List<InterfaceItemVo> errorItemList = new ArrayList<>();
                    List<InterfaceItemVo> interfaceItemList = new ArrayList<>();
                    List<InterfaceVo> interfaceAndItemList = interfaceItemMapper.getInterfaceItemByAuditId(policyAuditVo.getId());
                    for (InterfaceVo interfaceVo : interfaceAndItemList) {
                        List<InterfaceItemVo> itemList = interfaceVo.getInterfaceItemList();
                        interfaceItemList.addAll(itemList);
                    }
                    for (int i = 0; i < dataList.size(); i++) {
                        JSONObject dataObj = dataList.getJSONObject(i);
                        interfaceItemList.removeIf(d -> Objects.equals(d.getData().getString("facilityCategory"), dataObj.getString("facilityCategory"))
                                && Objects.equals(d.getData().getString("facilityDescriptor"), dataObj.getString("facilityDescriptor")));
                    }
                    //剩余的代表处理成功
                    for (InterfaceItemVo itemVo : interfaceItemList) {
                        interfaceItemMapper.updateInterfaceItemDataHashById(itemVo.getId());
                    }
                    throw new PhaseException(returnObj);
                }
            } else {//处理中 异常
                throw new PhaseNotCompletedException(returnObj);
            }
        }

        return resultStr;
    }

    private String sendData(Long corporationId, JSONObject reportData) {
        String token = TokenUtil.getToken(corporationId);
        if (StringUtils.isBlank(token)) {
            throw new LoginFailedException();
        }
        HttpRequestUtil httpRequestUtil = HttpRequestUtil.post(ConfigManager.getConfig(corporationId).getSelectDataUrl())
                //.setTenant(TenantContext.get().getTenantUuid())
                .addHeader("X-Access-Token", token)
                //.setAuthType(AuthenticateType.BASIC)
                //.setUsername("neatlogic")
                //.setPassword("x15wDEzSbBL6tV1W")
                .setPayload(reportData.toJSONString()).sendRequest();
        if (StringUtils.isNotBlank(httpRequestUtil.getError())) {
            throw new ApiRuntimeException(httpRequestUtil.getError());
        } else {
            return httpRequestUtil.getResult();
        }
    }
}

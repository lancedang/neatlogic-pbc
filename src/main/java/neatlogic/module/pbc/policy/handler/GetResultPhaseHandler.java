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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

   /* private static final Map<String, String> codeMap = new HashMap<>();

    static {
        codeMap.put("WL-40000", "等待逻辑检核开始");
        codeMap.put("WL-40001", "等待入库申请");
        codeMap.put("WL-40002", "入库处理中");
        codeMap.put("WL-40003", "入库处理成功");
        codeMap.put("WL-40004", "入库部分成功");
        codeMap.put("WL-40005", "入库处理失败");
        codeMap.put("WL-40006", "入库处理成功，但部分数据存在警告");
    }*/

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
        PolicyPhaseVo validatePhaseVo = policyMapper.getPolicyPhaseByAuditIdAndPhase(policyAuditVo.getId(), "validate");
        if (validatePhaseVo == null || StringUtils.isBlank(validatePhaseVo.getResult())) {
            throw new ValidateResultNotFoundException();
        }
        JSONObject jsonObj = JSONObject.parseObject(validatePhaseVo.getResult());
        if (StringUtils.isBlank(jsonObj.getString("groupId"))) {
            throw new ReportResultLackParamException("groupId");
        }
        String groupId = jsonObj.getString("groupId");
        JSONObject data = new JSONObject();

        data.put("facilityOwnerAgency", ConfigManager.getConfig(policyVo.getCorporationId()).getFacilityOwnerAgency());
        data.put("groupId", groupId);
        String result = sendData(ConfigManager.getConfig(policyVo.getCorporationId()).getValidResultUrl(), policyVo.getCorporationId(), data);
        JSONObject returnObj = JSONObject.parseObject(result);
        if (returnObj != null && returnObj.containsKey("code")) {
            String code = returnObj.getString("code");
            if ("WL-40002".equalsIgnoreCase(code)) {
                //处理中
                throw new PhaseNotCompletedException(returnObj);
            } else if ("WL-40005".equalsIgnoreCase(code) || "WL-40006".equalsIgnoreCase(code)) {
                //处理成功
                List<Long> deleteInterfaceIdList = interfaceItemMapper.getNeedDeleteInterfaceItemIdByAuditId(policyAuditVo.getId());
                if (CollectionUtils.isNotEmpty(deleteInterfaceIdList)) {
                    for (Long id : deleteInterfaceIdList) {
                        interfaceItemMapper.deleteInterfaceItemById(id);
                    }
                }
                interfaceItemMapper.updateInterfaceItemDataHashByAuditId(policyAuditVo.getId());
                policyMapper.updatePolicyLastExecDate(policyAuditVo.getPolicyId());
            } else if ("WL-40004".equalsIgnoreCase(code)) {
                //部分处理失败  需更新各自item的状态
                JSONArray dataList = returnObj.getJSONArray("data");
                if (CollectionUtils.isNotEmpty(dataList)) {
                    List<InterfaceItemVo> interfaceItemList = new ArrayList<>();
                    List<InterfaceVo> interfaceAndItemList = interfaceItemMapper.getInterfaceItemByAuditId(policyAuditVo.getId());
                    for (InterfaceVo interfaceVo : interfaceAndItemList) {
                        List<InterfaceItemVo> itemList = interfaceVo.getInterfaceItemList();
                        interfaceItemList.addAll(itemList);
                    }
                    for (int i = 0; i < dataList.size(); i++) {
                        JSONObject dataObj = dataList.getJSONObject(i);
                        if ("WL-10008".equalsIgnoreCase(dataObj.getString("code"))) {
                            String branchId = dataObj.getString("branchId");
                            JSONObject requestBranchData = new JSONObject();
                            requestBranchData.put("facilityOwnerAgency", ConfigManager.getConfig(policyVo.getCorporationId()).getFacilityOwnerAgency());
                            requestBranchData.put("branchId", branchId);
                            String branchDataStr = sendData(ConfigManager.getConfig(policyVo.getCorporationId()).getSelectDataUrl(), policyVo.getCorporationId(), requestBranchData);
                            JSONObject branchData = JSONObject.parseObject(branchDataStr);
                            JSONArray branchDataList = branchData.getJSONArray("data");
                            if (CollectionUtils.isNotEmpty(branchDataList)) {
                                for (int j = 0; j < branchDataList.size(); j++) {
                                    JSONObject branchDataObj = branchDataList.getJSONObject(j);
                                    interfaceItemList.removeIf(d -> Objects.equals(d.getData().getString("facilityCategory"), branchDataObj.getString("facilityCategory"))
                                            && Objects.equals(d.getData().getString("facilityDescriptor"), branchDataObj.getString("facilityDescriptor")));
                                }
                            }
                        }
                    }
                }
            } else {//其他处理异常
                throw new PhaseException(returnObj);
            }
        }

        return result;
    }


    private String sendData(String url, Long corporationId, JSONObject reportData) {
        String token = TokenUtil.getToken(corporationId);
        if (StringUtils.isBlank(token)) {
            throw new LoginFailedException();
        }
        HttpRequestUtil httpRequestUtil = HttpRequestUtil.post(url)
                .addHeader("X-Access-Token", token)
                .setPayload(reportData.toJSONString())
                .sendRequest();
        if (StringUtils.isNotBlank(httpRequestUtil.getError())) {
            throw new ApiRuntimeException(httpRequestUtil.getError());
        } else {
            return httpRequestUtil.getResult();
        }
    }

}

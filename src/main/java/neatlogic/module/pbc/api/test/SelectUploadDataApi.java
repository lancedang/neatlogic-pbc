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

package neatlogic.module.pbc.api.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dao.mapper.TestMapper;
import neatlogic.framework.pbc.dto.InterfaceItemVo;
import neatlogic.framework.pbc.dto.InterfaceVo;
import neatlogic.framework.pbc.dto.PolicyPhaseVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;


@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class SelectUploadDataApi extends PublicApiComponentBase {

    @Resource
    private TestMapper testMapper;

    @Override
    public boolean isRaw() {
        return true;
    }

    @Resource
    private InterfaceItemMapper interfaceItemMapper;


    @Override
    public String getToken() {
        return "/pbc/test/selectuploaddata";
    }

    @Override
    public String getName() {
        return "查询数据处理状态测试接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "branchId", type = ApiParamType.STRING, isRequired = true, desc = "批次id"),
            @Param(name = "facilityOwnerAgency", type = ApiParamType.STRING, isRequired = true, desc = "金融机构编码")})
    @Description(desc = "查询数据处理状态测试接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        PolicyPhaseVo phaseVo = testMapper.getLastPhase("report");
        if (phaseVo != null) {
            JSONObject resultObj = JSONObject.parseObject(phaseVo.getResult());
            if (phaseVo.getEndTime() != null && phaseVo.getEndTime().getTime() + 60 * 1000 > System.currentTimeMillis()) {
                JSONObject returnData = new JSONObject();
                returnData.put("branchId", resultObj.getString("branchId"));
                returnData.put("code", "WL-10006");
                returnData.put("msg", "等待核验中");
                return returnData;
            }

            List<InterfaceVo> interfaceAndItemList = interfaceItemMapper.getInterfaceItemByAuditId(phaseVo.getAuditId());
            Random random = new Random();
            List<InterfaceItemVo> itemList = new ArrayList<>();
            for (InterfaceVo interfaceVo : interfaceAndItemList) {
                if (CollectionUtils.isNotEmpty(interfaceVo.getInterfaceItemList())) {
                    itemList.addAll(interfaceVo.getInterfaceItemList());
                }
            }
            int numToSelect = random.nextInt(Math.min(itemList.size(), 10) + 1);
            Set<Integer> selectedIndices = new HashSet<>();
            JSONArray errorList = new JSONArray();
            for (int i = 0; i < numToSelect; i++) {
                int randomIndex;
                do {
                    randomIndex = random.nextInt(itemList.size());
                } while (selectedIndices.contains(randomIndex)); // 确保不选择重复的元素
                InterfaceItemVo itemVo = itemList.get(randomIndex);
                selectedIndices.add(randomIndex);
                errorList.add(new JSONObject() {{
                    this.put("code", "WL-20001");
                    this.put("msg", "自定义异常");
                    this.put("facilityCategory", itemVo.getData().getString("facilityCategory"));
                    this.put("facilityDescriptor", itemVo.getData().getString("facilityDescriptor"));
                }});
            }
            if (CollectionUtils.isNotEmpty(errorList)) {
                JSONObject returnData = new JSONObject();
                returnData.put("branchId", resultObj.getString("branchId"));
                returnData.put("code", "WL-10008");
                returnData.put("msg", "部分失败");
                returnData.put("data", errorList);
                return returnData;
            } else {
                JSONObject returnData = new JSONObject();
                returnData.put("branchId", resultObj.getString("branchId"));
                returnData.put("code", "WL-10009");
                returnData.put("msg", "成功");
                return returnData;
            }
        } else {
            JSONObject returnData = new JSONObject();
            returnData.put("branchId", "??????");
            returnData.put("code", "????");
            returnData.put("msg", "无法获取上报阶段结果");
            return returnData;
        }
    }


}

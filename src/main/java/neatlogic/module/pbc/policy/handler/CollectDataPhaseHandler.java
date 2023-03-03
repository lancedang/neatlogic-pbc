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

import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dao.mapper.PolicyMapper;
import neatlogic.framework.pbc.dto.*;
import neatlogic.module.pbc.enums.Action;
import neatlogic.framework.pbc.exception.NoDataToReportException;
import neatlogic.framework.pbc.policy.core.PhaseHandlerBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CollectDataPhaseHandler extends PhaseHandlerBase {
    @Override
    public String getPhase() {
        return "collect";
    }

    @Override
    public String getPhaseLabel() {
        return "计算需要上报的数据";
    }

    @Override
    public List<PolicyConfigVo> getConfigTemplate() {
        return null;
    }

    @Resource
    private InterfaceItemMapper interfaceItemMapper;

    @Resource
    private PolicyMapper policyMapper;

    @Override
    protected String myExecute(PolicyAuditVo policyAuditVo, PolicyVo policyVo, PolicyPhaseVo policyPhaseVo, List<InterfaceVo> interfaceList) {
        int reportCount = 0;
        policyMapper.deletePolicyAuditInterfaceItemByAuditId(policyAuditVo.getId());
        for (InterfaceVo interfaceVo : interfaceList) {
            List<InterfaceItemVo> interfaceItemList = interfaceItemMapper.getNeedReportInterfaceItemList(interfaceVo.getId(), policyVo.getCorporationId());
            for (InterfaceItemVo interfaceItemVo : interfaceItemList) {
                if (interfaceItemVo.getIsNew() == 1 && interfaceItemVo.getIsImported() == 0) {
                    policyMapper.insertAuditInterfaceItem(policyAuditVo.getId(), interfaceItemVo.getId(), Action.NEW.getValue());
                    reportCount += 1;
                } else if (interfaceItemVo.getIsNew() == 1 && interfaceItemVo.getIsImported() == 1) {
                    policyMapper.insertAuditInterfaceItem(policyAuditVo.getId(), interfaceItemVo.getId(), Action.UPDATE.getValue());
                    reportCount += 1;
                } else if (interfaceItemVo.getIsDelete() == 1) {
                    policyMapper.insertAuditInterfaceItem(policyAuditVo.getId(), interfaceItemVo.getId(), Action.DELETE.getValue());
                    reportCount += 1;
                }
            }
        }
        if (reportCount == 0) {
            throw new NoDataToReportException();
        }
        JSONObject returnObj = new JSONObject();
        returnObj.put("reportCount", reportCount);
        return returnObj.toString();
    }


}

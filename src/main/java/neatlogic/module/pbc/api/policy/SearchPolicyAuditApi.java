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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.PolicyMapper;
import neatlogic.framework.pbc.dto.PolicyAuditVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchPolicyAuditApi extends PrivateApiComponentBase {

    @Resource
    private PolicyMapper policyMapper;

    @Override
    public String getToken() {
        return "/pbc/policy/audit/search";
    }

    @Override
    public String getName() {
        return "查询同步策略执行记录";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true),
            @Param(name = "policyId", type = ApiParamType.LONG, desc = "策略id"),
            @Param(name = "idList", type = ApiParamType.JSONARRAY, desc = "id列表，用于精确查找"),
    })
    @Output({@Param(explode = PolicyAuditVo[].class), @Param(explode = BasePageVo.class)})
    @Description(desc = "查询同步策略执行记录接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        PolicyAuditVo policyAuditVo = JSONObject.toJavaObject(paramObj, PolicyAuditVo.class);
        List<PolicyAuditVo> policyAuditList = policyMapper.searchPolicyAudit(policyAuditVo);
        int rowNum = policyMapper.searchPolicyAuditCount(policyAuditVo);
        policyAuditVo.setRowNum(rowNum);
        return TableResultUtil.getResult(policyAuditList, policyAuditVo);
    }
}

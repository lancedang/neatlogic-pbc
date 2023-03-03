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

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.scheduler.core.IJob;
import neatlogic.framework.scheduler.core.SchedulerManager;
import neatlogic.framework.scheduler.dto.JobObject;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.PolicyMapper;
import neatlogic.framework.pbc.dto.InterfaceVo;
import neatlogic.framework.pbc.dto.PolicyInterfaceVo;
import neatlogic.framework.pbc.dto.PolicyVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SavePolicyApi extends PrivateApiComponentBase {

    @Resource
    private PolicyMapper policyMapper;
    @Resource
    private SchedulerManager schedulerManager;

    @Override
    public String getToken() {
        return "/pbc/policy/save";
    }

    @Override
    public String getName() {
        return "保存同步策略";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, desc = "id，不提供代表新增"), @Param(name = "name", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "名称"), @Param(name = "description", type = ApiParamType.STRING, xss = true, desc = "名称"), @Param(name = "isActive", type = ApiParamType.INTEGER, isRequired = true, desc = "是否激活"), @Param(name = "interfaceList", type = ApiParamType.JSONARRAY, isRequired = true, desc = "接口列表")})
    @Output({@Param(explode = InterfaceVo[].class), @Param(explode = BasePageVo.class)})
    @Description(desc = "保存同步策略接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        PolicyVo policyVo = JSONObject.toJavaObject(paramObj, PolicyVo.class);
        if (paramObj.getLong("id") == null) {
            policyMapper.insertPolicy(policyVo);
        } else {
            policyMapper.updatePolicy(policyVo);
            policyMapper.deletePolicyInterfaceByPolicyId(policyVo.getId());
        }
        if (CollectionUtils.isNotEmpty(policyVo.getInterfaceList())) {
            for (String interfaceId : policyVo.getInterfaceList()) {
                PolicyInterfaceVo policyInterfaceVo = new PolicyInterfaceVo();
                policyInterfaceVo.setPolicyId(policyVo.getId());
                policyInterfaceVo.setInterfaceId(interfaceId);
                policyMapper.insertPolicyInterface(policyInterfaceVo);
            }
        }
        IJob handler = SchedulerManager.getHandler("neatlogic.module.pbc.schedule.PbcPolicyExecuteScheduleJob");
        JobObject newJobObject = new JobObject.Builder(policyVo.getId().toString(), handler.getGroupName(), handler.getClassName(), TenantContext.get().getTenantUuid()).withCron(policyVo.getCronExpression()).addData("policyId", policyVo.getId()).build();
        if (policyVo.getIsActive().equals(1) && StringUtils.isNotBlank(policyVo.getCronExpression())) {
            schedulerManager.loadJob(newJobObject);
        } else {
            schedulerManager.unloadJob(newJobObject);
        }
        return null;
    }
}

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
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dto.PhaseVo;
import neatlogic.framework.pbc.policy.core.IPhaseHandler;
import neatlogic.framework.pbc.policy.core.PhaseDefine;
import neatlogic.framework.pbc.policy.core.PhaseHandlerFactory;
import com.alibaba.fastjson.JSONObject;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetPhaseListApi extends PrivateApiComponentBase {


    @Override
    public String getToken() {
        return "/pbc/policy/phase/list";
    }

    @Override
    public String getName() {
        return "获取人行上报策略步骤列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Output({@Param(explode = PhaseVo[].class)})
    @Description(desc = "获取人行上报策略步骤列表接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Reflections reflections = new Reflections("neatlogic.module.*");
        Set<Class<? extends PhaseDefine>> modules = reflections.getSubTypesOf(PhaseDefine.class);
        PhaseDefine define = null;
        if (modules.size() > 0) {
            for (Class<? extends PhaseDefine> c : modules) {
                try {
                    define = c.newInstance();
                    break;
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } else {
            define = new PhaseDefine();
        }

        List<PhaseVo> valueList = new ArrayList<>();
        if (define != null && define.getPhaseList() != null) {
            int sort = 1;
            for (String phase : define.getPhaseList()) {
                IPhaseHandler handler = PhaseHandlerFactory.getHandler(phase);
                if (handler != null) {
                    PhaseVo phaseVo = new PhaseVo();
                    phaseVo.setPhase(phase);
                    phaseVo.setName(handler.getPhaseLabel());
                    phaseVo.setSort(sort);
                    valueList.add(phaseVo);
                    sort += 1;
                }
            }
        }
        return valueList;
    }
}

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

package neatlogic.framework.pbc.dto;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.EntityField;
import neatlogic.framework.pbc.policy.core.IPhaseHandler;
import neatlogic.framework.pbc.policy.core.PhaseHandlerFactory;

import java.util.List;

public class PhaseVo {
    @EntityField(name = "阶段唯一标识", type = ApiParamType.STRING)
    private String phase;
    @EntityField(name = "阶段名称", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "排序", type = ApiParamType.INTEGER)
    private int sort;
    @EntityField(name = "高级配置模板", type = ApiParamType.JSONARRAY)
    private List<PolicyConfigVo> configTemplate;

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConfigTemplate(List<PolicyConfigVo> configTemplate) {
        this.configTemplate = configTemplate;
    }

    public List<PolicyConfigVo> getConfigTemplate() {
        IPhaseHandler handler = PhaseHandlerFactory.getHandler(this.getPhase());
        if (handler != null) {
            return handler.getConfigTemplate();
        }
        return null;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }
}

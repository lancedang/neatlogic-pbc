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
import neatlogic.module.pbc.enums.Action;
import org.apache.commons.lang3.StringUtils;

public class PolicyAuditInterfaceItemStatistVo {
    @EntityField(name = "动作", type = ApiParamType.STRING)
    private String action;
    @EntityField(name = "动作名称", type = ApiParamType.STRING)
    private String actionText;
    @EntityField(name = "个数", type = ApiParamType.INTEGER)
    private int count;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionText() {
        if (StringUtils.isBlank(actionText) && StringUtils.isNotBlank(action)) {
            actionText = Action.getText(action);
        }
        return actionText;
    }


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}

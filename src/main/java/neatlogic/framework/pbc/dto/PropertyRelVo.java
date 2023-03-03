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
import neatlogic.framework.util.SnowflakeUtil;

public class PropertyRelVo {
    @EntityField(name = "id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "属性uid", type = ApiParamType.INTEGER)
    private Integer fromPropertyUid;
    @EntityField(name = "目标接口id", type = ApiParamType.STRING)
    private String toInterfaceId;
    @EntityField(name = "值属性id", type = ApiParamType.STRING)
    private String toValuePropertyId;
    @EntityField(name = "文本属性id", type = ApiParamType.STRING)
    private String toTextPropertyId;

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getFromPropertyUid() {
        return fromPropertyUid;
    }

    public void setFromPropertyUid(Integer fromPropertyUid) {
        this.fromPropertyUid = fromPropertyUid;
    }


    public String getToValuePropertyId() {
        return toValuePropertyId;
    }

    public void setToValuePropertyId(String toValuePropertyId) {
        this.toValuePropertyId = toValuePropertyId;
    }

    public String getToTextPropertyId() {
        return toTextPropertyId;
    }

    public void setToTextPropertyId(String toTextPropertyId) {
        this.toTextPropertyId = toTextPropertyId;
    }

    public String getToInterfaceId() {
        return toInterfaceId;
    }

    public void setToInterfaceId(String toInterfaceId) {
        this.toInterfaceId = toInterfaceId;
    }
}

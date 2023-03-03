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

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class EnumVo {

    private String propertyId;
    private String value;
    private String text;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnumVo enumVo = (EnumVo) o;
        return Objects.equals(propertyId, enumVo.propertyId) && Objects.equals(value, enumVo.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyId, value);
    }

    public String getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(String propertyId) {
        this.propertyId = propertyId;
    }

    public String getValue() {

        return value;
    }

    public void setValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            value = value.trim();
        }
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (StringUtils.isNotBlank(text)) {
            text = text.trim();
        }
        this.text = text;
    }

}

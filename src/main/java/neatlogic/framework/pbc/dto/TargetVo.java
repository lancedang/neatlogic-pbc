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

/**
 * @Title: TargetVo
 * @Package neatlogic.module.pbc.dto
 * @Description: 映射目标实体类
 * @Author: yangy
 * @Date: 2021/7/20 17:37
 **/
public class TargetVo {
    private String targetType;
    private String targetId;
    private String targetAttrId;

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetAttrId() {
        return targetAttrId;
    }

    public void setTargetAttrId(String targetAttrId) {
        this.targetAttrId = targetAttrId;
    }
}

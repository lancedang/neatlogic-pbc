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
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.restful.annotation.EntityField;
import org.apache.commons.lang3.StringUtils;

public class CategoryVo extends BasePageVo {
    @EntityField(name = "一级分类标识符（两位）、二级分类标识符（两位）、三级分类标识符（三位）和四级分类标识符（三位）", type = ApiParamType.STRING)
    private String id;
    @EntityField(name = "名称", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "数据源名称", type = ApiParamType.STRING)
    private String interfaceName;
    @EntityField(name = "数据元传输标识", type = ApiParamType.STRING)
    private String interfaceId;
    @EntityField(name = "一级分类标识符", type = ApiParamType.STRING)
    private String id1;
    @EntityField(name = "一级分类标", type = ApiParamType.STRING)
    private String name1;
    @EntityField(name = "二级分类标识符", type = ApiParamType.STRING)
    private String id2;
    @EntityField(name = "二级分类", type = ApiParamType.STRING)
    private String name2;
    @EntityField(name = "三级分类标识符", type = ApiParamType.STRING)
    private String id3;
    @EntityField(name = "三级分类", type = ApiParamType.STRING)
    private String name3;
    @EntityField(name = "四级分类标识符", type = ApiParamType.STRING)
    private String id4;
    @EntityField(name = "四级分类", type = ApiParamType.STRING)
    private String name4;
    @EntityField(name = "报送是否符合要求", type = ApiParamType.STRING)
    private Integer isMatch;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getId1() {
        return id1;
    }

    public void setId1(String id1) {
        this.id1 = id1;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getId2() {
        return id2;
    }

    public void setId2(String id2) {
        this.id2 = id2;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getId3() {
        return id3;
    }

    public void setId3(String id3) {
        this.id3 = id3;
    }

    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
    }

    public String getId4() {
        return id4;
    }

    public void setId4(String id4) {
        this.id4 = id4;
    }

    public String getName4() {
        return name4;
    }

    public void setName4(String name4) {
        this.name4 = name4;
    }

    public Integer getIsMatch() {
        return isMatch;
    }

    public void setIsMatch(Integer isMatch) {
        this.isMatch = isMatch;
    }

    public String getName() {
        if (StringUtils.isNotBlank(name1)) {
            name = name1;
            if (StringUtils.isNotBlank(name2)) {
                name += "->" + name2;
                if (StringUtils.isNotBlank(name3)) {
                    name += "->" + name3;
                    if (StringUtils.isNotBlank(name4)) {
                        name += "->" + name4;
                    }
                }
            }
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

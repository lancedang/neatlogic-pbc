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
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import neatlogic.framework.util.I18nUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertyVo extends BasePageVo {
    public enum InputType {
        TEXT("text", "enum.pbc.inputtype.text"),
        DATE("date", "enum.pbc.inputtype.date"),
        DATETIME("datetime", "enum.pbc.inputtype.datetime"),
        SELECT("select", "enum.pbc.inputtype.select"),
        UUID("uuid", "enum.pbc.inputtype.uuid"),
        BOOLEAN("boolean", "enum.pbc.inputtype.boolean"),
        RELSELECT("relselect", "enum.pbc.inputtype.relselect"),
        CATEGORY("category", "enum.pbc.inputtype.category");
        private final String type;
        private final String text;

        InputType(String _type, String _text) {
            this.type = _type;
            this.text = _text;
        }

        public String getValue() {
            return type;
        }

        public String getText() {
            return I18nUtils.getMessage(text);
        }

        public static String getText(String name) {
            for (InterfaceVo.Status s : InterfaceVo.Status.values()) {
                if (s.getValue().equals(name)) {
                    return s.getText();
                }
            }
            return "";
        }
    }

    // 由于相同的propertyId可以被不同的接口引用，所以需要生成一个唯一uid，方便做删除和引用
    @EntityField(name = "唯一id", type = ApiParamType.INTEGER)
    private Integer uid;
    @EntityField(name = "复杂属性id", type = ApiParamType.STRING)
    private String complexId;
    @EntityField(name = "复杂属性名称", type = ApiParamType.STRING)
    private String complexName;
    @EntityField(name = "属性id", type = ApiParamType.STRING)
    private String id;
    @EntityField(name = "属性名称", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "接口id", type = ApiParamType.STRING)
    private String interfaceId;
    @EntityField(name = "接口名称", type = ApiParamType.STRING)
    private String interfaceName;
    @EntityField(name = "别名", type = ApiParamType.STRING)
    private String alias;
    @EntityField(name = "描述", type = ApiParamType.STRING)
    private String description;
    @EntityField(name = "定义", type = ApiParamType.STRING)
    private String definition;
    @EntityField(name = "数据类型", type = ApiParamType.STRING)
    private String dataType;
    @EntityField(name = "值范围", type = ApiParamType.STRING)
    private String valueRange;
    @EntityField(name = "校验规则", type = ApiParamType.STRING)
    private String restraint;
    @EntityField(name = "映射设置", type = ApiParamType.JSONOBJECT)
    private JSONObject mapping;
    @JSONField(serialize = false)
    private String mappingStr;
    @EntityField(name = "默认值", type = ApiParamType.STRING)
    private String propDefaultValue;
    @EntityField(name = "转换规则", type = ApiParamType.STRING)
    private String transferRule;
    @EntityField(name = "取值范例", type = ApiParamType.STRING)
    private String example;
    @EntityField(name = "是否主键", type = ApiParamType.INTEGER)
    private Integer isKey = 0;
    @JSONField(serialize = false)
    private String inputType = InputType.TEXT.getValue();
    @JSONField(serialize = false)
    private List<EnumVo> enumList = new ArrayList<>();
    private List<PropertyRelVo> relList;
    @EntityField(name = "排序", type = ApiParamType.INTEGER)
    private int sort;

    public Integer getUid() {
        if (uid == null) {
            uid = Objects.hash(this.getId(), this.getComplexId(), this.interfaceId);
        }
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyVo that = (PropertyVo) o;
        return id.equals(that.id) && interfaceId.equals(that.interfaceId) && Objects.equals(complexId, that.complexId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, interfaceId);
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public JSONObject getMapping() {
        if (mapping == null && StringUtils.isNotBlank(mappingStr)) {
            try {
                mapping = JSONObject.parseObject(mappingStr);
            } catch (Exception ignored) {

            }
        }
        return mapping;
    }

    public void setMapping(JSONObject mapping) {
        this.mapping = mapping;
    }

    public String getMappingStr() {
        if (StringUtils.isBlank(mappingStr) && mapping != null) {
            mappingStr = mapping.toString();
        }
        return mappingStr;
    }

    public void setMappingStr(String mappingStr) {
        this.mappingStr = mappingStr;
    }

    public String getComplexId() {
        return complexId;
    }

    public void setComplexId(String complexId) {
        this.complexId = complexId;
    }

    public String getComplexName() {
        return complexName;
    }

    public void setComplexName(String complexName) {
        this.complexName = complexName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getValueRange() {
        return valueRange;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setValueRange(String valueRange) {
        this.valueRange = valueRange;
    }


    public void setValueRange(String valueRange, boolean check) {
        valueRange = valueRange.replace("（", "(").replace("）", ")");
        Pattern p = Pattern.compile("(([fxoce][\\d]*\\.\\.[\\d]*)(\\([\\d]\\))?|(YYYY-MM-DD(\\sHH:MM)?)|(True/False))", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(valueRange);
        while (matcher.find()) {
            this.valueRange = matcher.group();
        }
        //检查是否有枚举数据
        Pattern enumP = Pattern.compile("([\\d]+)[:：]([^—\\n\\s\\t]+)");
        Matcher enumMatcher = enumP.matcher(valueRange);
        while (enumMatcher.find()) {
            if (enumMatcher.groupCount() == 2) {
                EnumVo enumVo = new EnumVo();
                enumVo.setPropertyId(this.getId());
                enumVo.setValue(enumMatcher.group(1));
                enumVo.setText(enumMatcher.group(2));
                if (!this.enumList.contains(enumVo)) {
                    this.enumList.add(enumVo);
                }
            }
        }
    }

    public static void main(String[] a) {
        String valueRange = "o2..2，其中每个有效取值对应的含义如下： \n" +
                "00：是 \n" +
                "01：否 \n" +
                "02：不适用";
        Pattern enumP = Pattern.compile("([\\d]+)[:：]([^—\\n\\s\\t]+)");
        Matcher enumMatcher = enumP.matcher(valueRange);
        while (enumMatcher.find()) {
            System.out.println(enumMatcher.group(1) + "=" + enumMatcher.group(2));
        }
    }


    public String getRestraint() {
        return restraint;
    }

    public void setRestraint(String restraint) {
        this.restraint = restraint;
    }

    public void setRestraint(String restraint, boolean check) {
        Pattern p = Pattern.compile("([MOC])", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(restraint);
        while (matcher.find()) {
            this.restraint = matcher.group();
        }
    }

    public String getTransferRule() {
        return transferRule;
    }

    public void setTransferRule(String transferRule) {
        if (StringUtils.isNotBlank(transferRule)) {
            transferRule = transferRule.replace("，", ",").replace("：", ":");
        }
        this.transferRule = transferRule;
    }

    public String getPropDefaultValue() {
        return propDefaultValue;
    }

    public void setPropDefaultValue(String propDefaultValue) {
        this.propDefaultValue = propDefaultValue;
    }

    public String getInputType() {
        if (StringUtils.isNotBlank(dataType)) {
            if (dataType.equals("枚举类型")) {
                inputType = InputType.SELECT.getValue();
            } else if (dataType.equals("日期字符串")) {
                inputType = InputType.DATE.getValue();
            } else if (dataType.equals("时间字符串")) {
                inputType = InputType.DATETIME.getValue();
            } else if (dataType.equals("布尔型")) {
                inputType = InputType.BOOLEAN.getValue();
            } else if (dataType.equals("全局唯一标识符") && name.endsWith("标识符")) {//目前只找到规律是名称以标识符结尾的才代表自身的uuid
                inputType = InputType.UUID.getValue();
            } else if (dataType.contains("分类编码")) {
                inputType = InputType.CATEGORY.getValue();
            }
        }
        if (CollectionUtils.isNotEmpty(relList)) {//配置了关联关系
            inputType = InputType.RELSELECT.getValue();
        }
        return inputType;
    }


    public void setInputType(String inputType) {
        this.inputType = inputType;
    }

    public List<EnumVo> getEnumList() {
        if (CollectionUtils.isNotEmpty(enumList)) {
            enumList.sort(Comparator.comparing(EnumVo::getValue));
        }
        return enumList;
    }

    public void setEnumList(List<EnumVo> enumList) {
        this.enumList = enumList;
    }

    public List<PropertyRelVo> getRelList() {
        return relList;
    }

    public void setRelList(List<PropertyRelVo> relList) {
        this.relList = relList;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public Integer getIsKey() {
        return isKey;
    }

    public void setIsKey(Integer isKey) {
        this.isKey = isKey;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }
}

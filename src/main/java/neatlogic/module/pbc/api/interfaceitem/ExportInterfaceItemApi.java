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

package neatlogic.module.pbc.api.interfaceitem;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateBinaryStreamApiComponentBase;
import neatlogic.framework.util.excel.ExcelBuilder;
import neatlogic.framework.util.excel.SheetBuilder;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dao.mapper.InterfaceMapper;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.EnumVo;
import neatlogic.framework.pbc.dto.InterfaceItemVo;
import neatlogic.framework.pbc.dto.InterfaceVo;
import neatlogic.framework.pbc.dto.PropertyVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Title: DownloadExcelInterfaceItemAuditApi
 * @Package neatlogic.module.pbc.api.item
 * @Description: TODO
 * @Author: yangy
 * @Date: 2021/10/11 16:52
 **/

@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class ExportInterfaceItemApi extends PrivateBinaryStreamApiComponentBase {


    @Resource
    private InterfaceMapper interfaceMapper;

    @Resource
    private InterfaceItemMapper interfaceItemMapper;

    @Resource
    private PropertyMapper propertyMapper;

    @Override
    public String getName() {
        return "导出接口数据";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "interfaceId", type = ApiParamType.STRING, desc = "接口id", isRequired = true), @Param(name = "isUseAlias", type = ApiParamType.INTEGER, desc = "是否使用别名")})
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        OutputStream os = null;
        int isUseAlias = paramObj.getIntValue("isUseAlias");
        try {
            Map<String, PropertyVo> propertyMap = new HashMap<>();
            String interfaceId = paramObj.getString("interfaceId");
            InterfaceVo interfaceVo = interfaceMapper.getInterfaceById(interfaceId);
            List<PropertyVo> propertyList = propertyMapper.getPropertyByInterfaceId(interfaceId);
            propertyList.forEach(d -> {
                if (StringUtils.isBlank(d.getComplexId())) {
                    propertyMap.put(d.getId(), d);
                } else {
                    propertyMap.put(d.getComplexId() + "#" + d.getId(), d);
                }
            });
            List<PropertyVo> simplePropertyList = propertyList.stream().filter(d -> StringUtils.isEmpty(d.getComplexId())).collect(Collectors.toList());//基础属性
            List<PropertyVo> complexPropertyList = propertyList.stream().filter(d -> StringUtils.isNotEmpty(d.getComplexId())).collect(Collectors.toList());//基础属性
            Map<String, PropertyVo> uniqueComplexPropertyMap = new TreeMap<>();
            complexPropertyList.forEach(propertyVo -> {
                if (!uniqueComplexPropertyMap.containsKey(propertyVo.getComplexId())) {
                    uniqueComplexPropertyMap.put(propertyVo.getComplexId(), propertyVo);
                }
            });
            ExcelBuilder excelBuilder = new ExcelBuilder(XSSFWorkbook.class).withColumnWidth(50).withHeadFontColor(HSSFColor.HSSFColorPredefined.WHITE).withHeadBgColor(HSSFColor.HSSFColorPredefined.LIGHT_BLUE);
            List<String> baseHeaderList = new ArrayList<>();
            baseHeaderList.add("主属性id#id");
            baseHeaderList.addAll(simplePropertyList.stream().map(d -> isUseAlias == 0 ? d.getName() + "#" + d.getId() : (StringUtils.isNotBlank(d.getAlias()) ? d.getAlias() : (d.getName() + "#" + d.getId()))).collect(Collectors.toList()));
            List<String> baseColumnList = new ArrayList<>();
            baseColumnList.add("id");
            baseColumnList.addAll(simplePropertyList.stream().map(PropertyVo::getId).collect(Collectors.toList()));
            SheetBuilder baseSheetBuilder = excelBuilder.addSheet("_main", "主属性").withHeaderList(baseHeaderList).withColumnList(baseColumnList);
            for (PropertyVo propertyVo : simplePropertyList) {
                if (propertyVo.getInputType().equals(PropertyVo.InputType.SELECT.getValue())) {
                    List<String> enumTextList = propertyVo.getEnumList().stream().map(EnumVo::getText).collect(Collectors.toList());
                    String[] sList = enumTextList.toArray(new String[]{});
                    baseSheetBuilder.addValidation(propertyVo.getId(), sList);
                }
            }
            for (String complexId : uniqueComplexPropertyMap.keySet()) {
                List<PropertyVo> subPropertyList = propertyList.stream().filter(d -> d.getComplexId().equals(complexId)).collect(Collectors.toList());
                List<String> headerList = new ArrayList<>();
                headerList.add("主属性id#id");
                headerList.addAll(subPropertyList.stream().map(d -> d.getName() + "#" + d.getId()).collect(Collectors.toList()));
                List<String> columnList = new ArrayList<>();
                columnList.add("id");
                columnList.addAll(subPropertyList.stream().map(PropertyVo::getId).collect(Collectors.toList()));
                SheetBuilder sheetBuilder = excelBuilder.addSheet(complexId, uniqueComplexPropertyMap.get(complexId).getComplexName()).withHeaderList(headerList).withColumnList(columnList);
                for (PropertyVo propertyVo : subPropertyList) {
                    if (propertyVo.getInputType().equals(PropertyVo.InputType.SELECT.getValue())) {
                        List<String> enumTextList = propertyVo.getEnumList().stream().map(EnumVo::getText).collect(Collectors.toList());
                        String[] sList = enumTextList.toArray(new String[]{});
                        sheetBuilder.addValidation(propertyVo.getId(), sList);
                    }
                }
            }
            Workbook workbook = excelBuilder.build();


            InterfaceItemVo interfaceItemVo = new InterfaceItemVo();
            interfaceItemVo.setInterfaceId(interfaceId);
            interfaceItemVo.setPageSize(100);
            List<InterfaceItemVo> interfaceItemList = interfaceItemMapper.searchInterfaceItem(interfaceItemVo);
            while (CollectionUtils.isNotEmpty(interfaceItemList)) {
                for (InterfaceItemVo data : interfaceItemList) {
                    if (MapUtils.isNotEmpty(data.getData())) {
                        Map<String, Object> baseRow = new HashMap<>();
                        for (String key : data.getData().keySet()) {
                            baseRow.put("id", data.getId());
                            SheetBuilder sheetBuilder = excelBuilder.getSheetBuilderById(key);
                            if (data.getData().get(key) instanceof JSONArray && sheetBuilder != null) {
                                JSONArray subDataList = data.getData().getJSONArray(key);
                                if (CollectionUtils.isNotEmpty(subDataList)) {
                                    for (int i = 0; i < subDataList.size(); i++) {
                                        Map<String, Object> subData = subDataList.getJSONObject(i);
                                        subData.put("id", data.getId());
                                        for (String subKey : subData.keySet()) {
                                            PropertyVo prop = propertyMap.get(key + "#" + subKey);
                                            if (prop != null) {
                                                if (prop.getInputType().equals(PropertyVo.InputType.SELECT.getValue())) {
                                                    Optional<EnumVo> op = prop.getEnumList().stream().filter(d -> d.getValue().equals(subData.get(subKey))).findFirst();
                                                    op.ifPresent(enumVo -> subData.put(subKey, enumVo.getText()));
                                                }
                                            }
                                        }
                                        sheetBuilder.addData(subData);
                                    }
                                }
                            } else {
                                PropertyVo prop = propertyMap.get(key);
                                if (prop != null) {
                                    if (!prop.getInputType().equals(PropertyVo.InputType.SELECT.getValue())) {
                                        baseRow.put(key, data.getData().get(key));
                                    } else {
                                        Optional<EnumVo> op = prop.getEnumList().stream().filter(d -> d.getValue().equals(data.getData().get(key))).findFirst();
                                        if (op.isPresent()) {
                                            baseRow.put(key, op.get().getText());
                                        } else {
                                            baseRow.put(key, data.getData().get(key));
                                        }
                                    }
                                }
                            }
                        }
                        excelBuilder.getSheetBuilderById("_main").addData(baseRow);
                    }
                }
                interfaceItemVo.setCurrentPage(interfaceItemVo.getCurrentPage() + 1);
                interfaceItemList = interfaceItemMapper.searchInterfaceItem(interfaceItemVo);
            }

            String fileNameEncode = interfaceVo.getName() + "-数据.xlsx";
            boolean flag = request.getHeader("User-Agent").indexOf("Gecko") > 0;
            if (request.getHeader("User-Agent").toLowerCase().indexOf("msie") > 0 || flag) {
                fileNameEncode = URLEncoder.encode(fileNameEncode, "UTF-8");// IE浏览器
            } else {
                fileNameEncode = new String(fileNameEncode.replace(" ", "").getBytes(StandardCharsets.UTF_8), "ISO8859-1");
            }
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setHeader("Content-Disposition", " attachment; filename=\"" + fileNameEncode + "\"");
            os = response.getOutputStream();
            workbook.write(response.getOutputStream());
        } finally {
            if (os != null) {
                os.flush();
                os.close();
            }
        }
        return null;
    }


    @Override
    public String getToken() {
        return "/pbc/interfaceitem/export";
    }
}

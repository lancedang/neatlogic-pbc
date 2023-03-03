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
import neatlogic.framework.pbc.dao.mapper.InterfaceMapper;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.EnumVo;
import neatlogic.framework.pbc.dto.InterfaceVo;
import neatlogic.framework.pbc.dto.PropertyVo;
import com.alibaba.fastjson.JSONObject;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class DownloadItemExcelTemplateApi extends PrivateBinaryStreamApiComponentBase {
    @Resource
    private InterfaceMapper interfaceMapper;

    @Resource
    private PropertyMapper propertyMapper;

    @Override
    public String getToken() {
        return "/pbc/interfaceitem/template/download";
    }

    @Override
    public String getName() {
        return "下载导入上报数据模板";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "interfaceId", type = ApiParamType.STRING, desc = "接口id", isRequired = true)})
    @Override
    public Object myDoService(JSONObject paramObj, HttpServletRequest request, HttpServletResponse response) throws Exception {
        OutputStream os = null;
        try {
            String interfaceId = paramObj.getString("interfaceId");
            InterfaceVo interfaceVo = interfaceMapper.getInterfaceById(interfaceId);

            List<PropertyVo> propertyList = propertyMapper.getPropertyByInterfaceId(interfaceId);
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
            baseHeaderList.addAll(simplePropertyList.stream().map(d -> d.getName() + "#" + d.getId()).collect(Collectors.toList()));
            List<String> baseColumnList = new ArrayList<>();
            baseColumnList.add("id");
            baseColumnList.addAll(simplePropertyList.stream().map(PropertyVo::getId).collect(Collectors.toList()));
            SheetBuilder baseSheetBuilder = excelBuilder.addSheet("主属性").withHeaderList(baseHeaderList).withColumnList(baseColumnList);
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
                SheetBuilder sheetBuilder = excelBuilder.addSheet(uniqueComplexPropertyMap.get(complexId).getComplexName()).withHeaderList(headerList).withColumnList(columnList);
                for (PropertyVo propertyVo : subPropertyList) {
                    if (propertyVo.getInputType().equals(PropertyVo.InputType.SELECT.getValue())) {
                        List<String> enumTextList = propertyVo.getEnumList().stream().map(EnumVo::getText).collect(Collectors.toList());
                        String[] sList = enumTextList.toArray(new String[]{});
                        sheetBuilder.addValidation(propertyVo.getId(), sList);
                    }
                }
            }
            Workbook workbook = excelBuilder.build();

            String fileNameEncode = interfaceVo.getName() + "-导入模板.xlsx";
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

}

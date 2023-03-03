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

package neatlogic.module.pbc.file;

import neatlogic.framework.file.core.FileTypeHandlerBase;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.pbc.dao.mapper.EnumMapper;
import neatlogic.framework.pbc.dao.mapper.InterfaceMapper;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.EnumVo;
import neatlogic.framework.pbc.dto.InterfaceVo;
import neatlogic.framework.pbc.dto.PropertyVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Component
public class InterfaceImportHandler extends FileTypeHandlerBase {
    private final Logger logger = LoggerFactory.getLogger(InterfaceImportHandler.class);

    @Override
    public boolean valid(String userUuid, FileVo fileVo, JSONObject jsonObj) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "导入人行接口定义文件";
    }

    @Override
    public boolean needSave() {
        return false;
    }

    @Resource
    private InterfaceMapper interfaceMapper;

    @Resource
    private PropertyMapper propertyMapper;

    @Resource
    private EnumMapper enumMapper;

    private String getCellValue(Cell cell) {
        String cellValue;
        if (cell == null) {
            cellValue = null;
        } else {
            if (cell.getCellType() == CellType.NUMERIC) {
                double d = cell.getNumericCellValue();
                cellValue = (int) d + "";
            } else if (cell.getCellType() == CellType.STRING) {
                cellValue = cell.getStringCellValue();
            } else {
                cellValue = null;
            }
        }
        if (cellValue != null) {
            return cellValue.trim();
        } else {
            return "";
        }
    }

    @Override
    public void analyze(MultipartFile file, JSONObject paramObj) throws Exception {
        //excel表头定义
        final String HEAD_INTERFACE_NAME = "采集接口";
        final String HEAD_INTERFACE_ID = "采集数据元传输标识";
        final String HEAD_PROPERTY_COMPLEXID = "复合属性传输标识";
        final String HEAD_PROPERTY_COMPLEXNAME = "数据元采集属性";
        final String HEAD_PROPERTY_ID = "属性传输标识";
        final String HEAD_PROPERTY_NAME = "数据元属性";
        final String HEAD_PROPERTY_DESCRIPTION = "备注";
        final String HEAD_PROPERTY_DATATYPE = "数据类型";
        final String HEAD_PROPERTY_VALUERANGE = "值域";
        final String HEAD_PROPERTY_RESTRAINT = "约束条件";
        final String HEAD_PROPERTY_DEFINITION = "定义";
        final String HEAD_PROPERTY_EXAMPLE = "取值示例";
        Workbook ws = null;

        try {
            InputStream input = file.getInputStream();
            if (StringUtils.isNotBlank(file.getOriginalFilename())) {
                if (file.getOriginalFilename().toLowerCase().endsWith("xlsx")) {
                    ws = new XSSFWorkbook(input);
                } else if (file.getOriginalFilename().toLowerCase().endsWith("xls")) {
                    ws = new HSSFWorkbook(input);
                }
            }
            if (ws != null) {
                SHEET:
                for (int i = 0; i < ws.getNumberOfSheets(); i++) {
                    Sheet hssfSheet = ws.getSheetAt(i);
                    if (hssfSheet != null) {
                        Map<String, Integer> keyMap = new HashMap<>();
                        List<String> headerList = Arrays.asList(HEAD_INTERFACE_NAME, HEAD_INTERFACE_ID, HEAD_PROPERTY_COMPLEXNAME, HEAD_PROPERTY_COMPLEXID, HEAD_PROPERTY_NAME, HEAD_PROPERTY_ID, HEAD_PROPERTY_DESCRIPTION, HEAD_PROPERTY_DATATYPE, HEAD_PROPERTY_VALUERANGE, HEAD_PROPERTY_RESTRAINT, HEAD_PROPERTY_DEFINITION, HEAD_PROPERTY_EXAMPLE);
                        Row hssfRow = hssfSheet.getRow(hssfSheet.getFirstRowNum());
                        for (Iterator<Cell> cellIterator = hssfRow.cellIterator(); cellIterator.hasNext(); ) {
                            Cell cell = cellIterator.next();
                            String cellValue = getCellValue(cell);
                            if (StringUtils.isNotBlank(cellValue) && headerList.contains(cellValue)) {
                                keyMap.put(cellValue, cell.getColumnIndex());
                            }
                        }
                        for (String header : headerList) {
                            if (!keyMap.containsKey(header)) {
                                //throw new RuntimeException("导入模板必须包含列“" + header + "”");
                                continue SHEET;
                            }
                        }

                        //保证入库顺序和excel顺序一致，使用linkedHashMap作存储
                        Map<String, InterfaceVo> interfaceMap = new LinkedHashMap<>();
                        Map<String, Integer> interfaceSort = new HashMap<>();
                        for (int j = 1; j <= hssfSheet.getLastRowNum(); j++) {
                            String interfaceId = null, interfaceName = null, propertyComplexId = null, propertyComplexName = null, propertyId = null, propertyName = null, propertyDescription = null, propertyDataType = null, propertyValueRange = null, propertyRestraint = null, propertyDefinition = null, propertyExample = null;
                            Row hssfrp = hssfSheet.getRow(j);
                            for (String head : headerList) {
                                Cell cell = hssfrp.getCell(keyMap.get(head));
                                if (cell != null) {
                                    switch (head) {
                                        case HEAD_INTERFACE_NAME:
                                            interfaceName = getCellValue(cell);
                                            break;
                                        case HEAD_INTERFACE_ID:
                                            interfaceId = getCellValue(cell);
                                            break;
                                        case HEAD_PROPERTY_COMPLEXID:
                                            propertyComplexId = getCellValue(cell);
                                            break;
                                        case HEAD_PROPERTY_COMPLEXNAME:
                                            propertyComplexName = getCellValue(cell);
                                            break;
                                        case HEAD_PROPERTY_ID:
                                            propertyId = getCellValue(cell);
                                            break;
                                        case HEAD_PROPERTY_NAME:
                                            propertyName = getCellValue(cell);
                                            break;
                                        case HEAD_PROPERTY_DESCRIPTION:
                                            propertyDescription = getCellValue(cell);
                                            break;
                                        case HEAD_PROPERTY_DATATYPE:
                                            propertyDataType = getCellValue(cell);
                                            break;
                                        case HEAD_PROPERTY_VALUERANGE:
                                            propertyValueRange = getCellValue(cell);
                                            break;
                                        case HEAD_PROPERTY_RESTRAINT:
                                            propertyRestraint = getCellValue(cell);
                                            break;
                                        case HEAD_PROPERTY_DEFINITION:
                                            propertyDefinition = getCellValue(cell);
                                            break;
                                        case HEAD_PROPERTY_EXAMPLE:
                                            propertyExample = getCellValue(cell);
                                            break;
                                    }
                                }
                            }
                            //组装interface
                            if (StringUtils.isNotBlank(interfaceId) && StringUtils.isNotBlank(interfaceName)) {
                                InterfaceVo interfaceVo = interfaceMap.get(interfaceId);
                                if (interfaceVo == null) {
                                    interfaceVo = new InterfaceVo();
                                    interfaceVo.setId(interfaceId);
                                    interfaceVo.setName(interfaceName);
                                    interfaceMap.put(interfaceId, interfaceVo);
                                    interfaceSort.put(interfaceId, 1);
                                }
                                //如果没有复合属性，则用复合属性名代替属性名（人行的模板就是这样设计的）
                                if (StringUtils.isBlank(propertyComplexId)) {
                                    propertyName = propertyComplexName;
                                    propertyComplexName = "";
                                }
                                if (StringUtils.isNotBlank(propertyId) && StringUtils.isNotBlank(propertyName)) {
                                    PropertyVo propertyVo = new PropertyVo();
                                    propertyVo.setId(propertyId);
                                    propertyVo.setName(propertyName);
                                    propertyVo.setComplexId(propertyComplexId);
                                    propertyVo.setComplexName(propertyComplexName);
                                    propertyVo.setDataType(propertyDataType);
                                    propertyVo.setRestraint(propertyRestraint, true);
                                    propertyVo.setValueRange(propertyValueRange, true);
                                    propertyVo.setInterfaceId(interfaceId);
                                    propertyVo.setDescription(propertyDescription);
                                    propertyVo.setDefinition(propertyDefinition);
                                    propertyVo.setExample(propertyExample);
                                    propertyVo.setIsKey(isKey(propertyId));
                                    propertyVo.setSort(interfaceSort.get(interfaceId));
                                    interfaceVo.addProperty(propertyVo);
                                    interfaceSort.put(interfaceId, interfaceSort.get(interfaceId) + 1);
                                }
                            }
                        }
                        if (MapUtils.isNotEmpty(interfaceMap)) {
                            interfaceMapper.deleteAllInterface();
                            for (String key : interfaceMap.keySet()) {
                                InterfaceVo interfaceVo = interfaceMap.get(key);
                                interfaceMapper.insertInterface(interfaceVo);
                                if (CollectionUtils.isNotEmpty(interfaceVo.getPropertyList())) {
                                    for (PropertyVo propertyVo : interfaceVo.getPropertyList()) {
                                        propertyMapper.insertProperty(propertyVo);
                                        if (CollectionUtils.isNotEmpty(propertyVo.getEnumList())) {
                                            //enumMapper.deleteEnumByPropertyId(propertyVo.getId());
                                            for (EnumVo enumVo : propertyVo.getEnumList()) {
                                                enumMapper.insertEnum(enumVo);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            if (ws != null) {
                try {
                    ws.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    //某些属性是唯一键，需要标记一下
    private int isKey(String propertyId) {
        Set<String> uniKeySet = new HashSet<>();
        uniKeySet.add("applySystemIdentifiers".toLowerCase());
        uniKeySet.add("facilityDescriptor".toLowerCase());
        uniKeySet.add("relationalIdentifier".toLowerCase());
        uniKeySet.add("applicationIdentifier".toLowerCase());
        uniKeySet.add("SoftwareDescriptor".toLowerCase());
        if (uniKeySet.contains(propertyId.toLowerCase())) {
            return 1;
        }
        return 0;
    }

    @Override
    protected boolean myDeleteFile(FileVo fileVo, JSONObject paramObj) {
        return true;
    }


    @Override
    public String getName() {
        return "PBC_INTERFACE_IMPORT";
    }

}

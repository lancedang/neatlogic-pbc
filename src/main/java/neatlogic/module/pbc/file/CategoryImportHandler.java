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
import neatlogic.framework.pbc.dao.mapper.CategoryMapper;
import neatlogic.framework.pbc.dto.CategoryVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.*;

@Component
public class CategoryImportHandler extends FileTypeHandlerBase {
    private final Logger logger = LoggerFactory.getLogger(CategoryImportHandler.class);

    @Override
    public boolean valid(String userUuid, FileVo fileVo, JSONObject jsonObj) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "分类标识定义文件";
    }

    @Override
    public boolean needSave() {
        return false;
    }

    @Resource
    private CategoryMapper categoryMapper;

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
        final String HEAD_INTERFACE_NAME = "数据元名称";
        final String HEAD_INTERFACE_ID = "数据元传输标识";
        final String HEAD_ID_1 = "一级分类标识符";
        final String HEAD_NAME_1 = "一级分类";
        final String HEAD_ID_2 = "二级分类标识符";
        final String HEAD_NAME_2 = "二级分类";
        final String HEAD_ID_3 = "三级分类标识符";
        final String HEAD_NAME_3 = "三级分类";
        final String HEAD_ID_4 = "四级分类标识符";
        final String HEAD_NAME_4 = "四级分类";
        final String HEAD_ID = "分类标识符";
        final String HEAD_IS_MATCH = "报送是否符合要求";

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
                        List<String> headerList = Arrays.asList(HEAD_INTERFACE_NAME, HEAD_INTERFACE_ID, HEAD_ID_1, HEAD_NAME_1, HEAD_ID_2, HEAD_NAME_2, HEAD_ID_3, HEAD_NAME_3, HEAD_ID_4, HEAD_NAME_4, HEAD_ID, HEAD_IS_MATCH);
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
                                // throw new RuntimeException("导入模板必须包含列“" + header + "”");
                                continue SHEET;
                            }
                        }

                        //保证入库顺序和excel顺序一致，使用linkedHashMap作存储
                        Map<String, CategoryVo> categoryMap = new LinkedHashMap<>();
                        for (int j = 1; j <= hssfSheet.getLastRowNum(); j++) {
                            String interfaceId = null, interfaceName = null, id1 = null, name1 = null, id2 = null, name2 = null, id3 = null, name3 = null, id4 = null, name4 = null, id = null;
                            Integer isMatch = null;
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
                                        case HEAD_ID_1:
                                            id1 = getCellValue(cell);
                                            break;
                                        case HEAD_NAME_1:
                                            name1 = getCellValue(cell);
                                            break;
                                        case HEAD_ID_2:
                                            id2 = getCellValue(cell);
                                            break;
                                        case HEAD_NAME_2:
                                            name2 = getCellValue(cell);
                                            break;
                                        case HEAD_ID_3:
                                            id3 = getCellValue(cell);
                                            break;
                                        case HEAD_NAME_3:
                                            name3 = getCellValue(cell);
                                            break;
                                        case HEAD_ID_4:
                                            id4 = getCellValue(cell);
                                            break;
                                        case HEAD_NAME_4:
                                            name4 = getCellValue(cell);
                                            break;
                                        case HEAD_ID:
                                            id = getCellValue(cell);
                                            break;
                                        case HEAD_IS_MATCH:
                                            isMatch = "是".equals(getCellValue(cell)) ? 1 : 0;
                                            break;

                                    }
                                }
                            }
                            if (StringUtils.isNotBlank(id)) {
                                CategoryVo categoryVo = categoryMap.get(id);
                                if (categoryVo == null) {
                                    categoryVo = new CategoryVo();
                                    categoryVo.setId(id);
                                    categoryVo.setInterfaceId(interfaceId);
                                    categoryVo.setInterfaceName(interfaceName);
                                    categoryVo.setId1(id1);
                                    categoryVo.setName1(name1);
                                    categoryVo.setId2(id2);
                                    categoryVo.setName2(name2);
                                    categoryVo.setId3(id3);
                                    categoryVo.setName3(name3);
                                    categoryVo.setId4(id4);
                                    categoryVo.setName4(name4);
                                    categoryVo.setIsMatch(isMatch);
                                    categoryMap.put(id, categoryVo);
                                }
                            }
                        }
                        for (String key : categoryMap.keySet()) {
                            categoryMapper.replaceCategory(categoryMap.get(key));
                        }
                    }
                }
            }
        } finally {
            if (ws != null) {
                ws.close();
            }
        }
    }

    @Override
    protected boolean myDeleteFile(FileVo fileVo, JSONObject paramObj) {
        return true;
    }


    @Override
    public String getName() {
        return "PBC_CATEGORY_IMPORT";
    }

}

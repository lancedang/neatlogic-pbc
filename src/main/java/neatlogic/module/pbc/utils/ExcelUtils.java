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

package neatlogic.module.pbc.utils;

import neatlogic.framework.util.ExcelUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * @Title: ExcelUtils
 * @Package com.neatlogic.module.pbc.utils
 * @Description: TODO
 * @Author: yangy
 * @Date: 2021/4/13 18:34
 **/
public class ExcelUtils {
    private static final Logger logger = LoggerFactory.getLogger(ExcelUtils.class);

    public static Map<String, Object> getExcelData(MultipartFile file) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            Workbook wb = WorkbookFactory.create(file.getInputStream());
            List<String> headerList = new ArrayList<String>();
            List<Map<String, String>> contentList = new ArrayList<Map<String, String>>();
            Map<String, String> contentMap = null;
            resultMap.put("header", headerList);
            resultMap.put("content", contentList);

            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet hssfSheet = wb.getSheetAt(i);
                if (hssfSheet == null) {
                    continue;
                } else {
                    Row headRow = hssfSheet.getRow(hssfSheet.getFirstRowNum());
                    List<Integer> cellIndex = new ArrayList<Integer>();
                    for (Iterator<Cell> cellIterator = (Iterator<Cell>) headRow.cellIterator(); cellIterator.hasNext();) {
                        Cell cell = cellIterator.next();
                        if (cell != null) {
                            String content = ExcelUtil.getCellContent(cell);
                            if (content.contains("[(")) {
                                content = content.substring(0, content.indexOf("[("));
                            }
                            headerList.add(content);
                            cellIndex.add(cell.getColumnIndex());
                        }
                    }
                    for (int r = hssfSheet.getFirstRowNum() + 1; r <= hssfSheet.getLastRowNum(); r++) {
                        Row hssfRow = hssfSheet.getRow(r);
                        if (hssfRow != null) {
                            contentMap = new HashMap<String, String>(cellIndex.size() + 1, 1);
                            for (int ci = 0; ci < cellIndex.size(); ci++) {
                                Cell cell = hssfRow.getCell(cellIndex.get(ci));
                                if (cell != null) {
                                    String content = ExcelUtil.getCellContent(cell);
                                    contentMap.put(headerList.get(ci), content);
                                }else{
                                    String content = "";
                                    contentMap.put(headerList.get(ci), content);
                                }
                            }
                            contentList.add(contentMap);
                        }
                    }
                    break;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        finally {
            try {
                file.getInputStream().close();
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        return resultMap;
    }

    public static JSONArray getExcelDataFile(MultipartFile file) throws Exception {
        JSONArray resultArray = new JSONArray();
        FileInputStream in = null;
        try {
            XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream());
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                JSONObject resultObj = new JSONObject();
                List<String> headerList = new ArrayList<String>();
                JSONArray dataArray = new JSONArray();
                JSONObject dataObj = new JSONObject();


                Sheet hssfSheet = wb.getSheetAt(i);
                String sheetName = hssfSheet.getSheetName();
                if (sheetName.contains("[(")) {
                    sheetName = sheetName.substring(0, sheetName.indexOf("[("));
                }
                resultObj.put("sheetName",sheetName);
                if (hssfSheet == null) {
                    continue;
                } else {
                    Row headRow = hssfSheet.getRow(hssfSheet.getFirstRowNum());
                    List<Integer> cellIndex = new ArrayList<Integer>();
                    for (Iterator<Cell> cellIterator = (Iterator<Cell>) headRow.cellIterator(); cellIterator.hasNext();) {
                        Cell cell = cellIterator.next();
                        if (cell != null) {
                            String content = ExcelUtil.getCellContent(cell);
                            if (content.contains("[(")) {
                                content = content.substring(0, content.indexOf("[("));
                            }
                            headerList.add(content);
                            cellIndex.add(cell.getColumnIndex());
                        }
                    }
                    for (int r = hssfSheet.getFirstRowNum() + 1; r <= hssfSheet.getLastRowNum(); r++) {
                        Row hssfRow = hssfSheet.getRow(r);
                        if (hssfRow != null) {
                            for (int ci = 0; ci < cellIndex.size(); ci++) {
                                Cell cell = hssfRow.getCell(cellIndex.get(ci));
                                if (cell != null) {
                                    String content = ExcelUtil.getCellContent(cell);
                                    dataObj.put(headerList.get(ci), content);
                                }else{
                                    String content = "";
                                    dataObj.put(headerList.get(ci), content);
                                }
                            }
                        }
                        dataArray.add(dataObj);
                    }

                    //resultObj.put("header", headerList);
                    resultObj.put("content", dataArray);
                    resultArray.add(resultObj);
                    continue;
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                file.getInputStream().close();
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        System.out.println(resultArray);
        return resultArray;
    }

    /**
     * @Description: 获取上报item excel第一个sheet页存放基础数据 设置一列用来存行数 后面的sheet用来放复合属性 也设置一列用来关联基础sheet的行数 先遍历基础sheet 然后根据item关联 组装数据
     * @Author: yangy
     * @Date: 2021/4/16 17:14
     * @Params:[file]
     * @Returns:net.sf.json.JSONArray
     **/
    public static JSONArray getAllExcelDataFile(MultipartFile file) throws Exception {
        JSONArray array = new JSONArray();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(file.getInputStream());

            Sheet baseHssfSheet = wb.getSheetAt(0);
            List<String> baseHeaderList = new ArrayList<String>();
            JSONObject baseDataObj = new JSONObject();
            Row firstHeadRow = baseHssfSheet.getRow(baseHssfSheet.getFirstRowNum());
            List<Integer> firstCellIndex = new ArrayList<Integer>();
            for (Iterator<Cell> cellIterator = (Iterator<Cell>) firstHeadRow.cellIterator(); cellIterator.hasNext();) {
                Cell cell = cellIterator.next();
                if (cell != null) {
                    String content = ExcelUtil.getCellContent(cell);
                    if (content.contains("[(")) {
                        content = content.substring(0, content.indexOf("[("));
                    }
                    baseHeaderList.add(content);
                    firstCellIndex.add(cell.getColumnIndex());
                }
            }
            for (int r = baseHssfSheet.getFirstRowNum() + 1; r <= baseHssfSheet.getLastRowNum(); r++) {
                JSONArray itemArray = new JSONArray();
                JSONObject itemObj = new JSONObject();
                String baseRowItem = "";
                JSONArray baseArray = new JSONArray();
                JSONObject baseRawObj = new JSONObject();
                Row hssfRow = baseHssfSheet.getRow(r);
                if (hssfRow != null) {
                    for (int ci = 0; ci < firstCellIndex.size(); ci++) {
                        Cell cell = hssfRow.getCell(firstCellIndex.get(ci));
                        if (cell != null) {
                            String content = ExcelUtil.getCellContent(cell);
                            if(baseHeaderList.get(ci).equals("item_uuid")){
                                baseRowItem = content;
                            }
                            baseDataObj.put(baseHeaderList.get(ci), content);
                        }else{
                            String content = "";
                            baseDataObj.put(baseHeaderList.get(ci), content);
                        }
                    }
                }
                baseDataObj.remove("item_uuid");//用完后删掉
                baseArray.add(baseDataObj);
                baseRawObj.put("sheetName", "主属性");
                baseRawObj.put("content", baseArray);

                itemArray.add(baseRawObj);

                for (int i = 1; i < wb.getNumberOfSheets(); i++) {
                    JSONObject resultObj = new JSONObject();
                    List<String> headerList = new ArrayList<String>();
                    JSONArray complexArray = new JSONArray();
                    JSONObject complexDataObj = new JSONObject();


                    Sheet hssfSheet = wb.getSheetAt(i);
                    String sheetName = hssfSheet.getSheetName();
                    if (hssfSheet == null) {
                        continue;
                    } else {
                        Row headRow = hssfSheet.getRow(hssfSheet.getFirstRowNum());
                        List<Integer> cellIndex = new ArrayList<Integer>();
                        for (Iterator<Cell> cellIterator = (Iterator<Cell>) headRow.cellIterator(); cellIterator.hasNext();) {
                            Cell cell = cellIterator.next();
                            if (cell != null) {
                                String content = ExcelUtil.getCellContent(cell);
                                if (content.contains("[(")) {
                                    content = content.substring(0, content.indexOf("[("));
                                }
                                headerList.add(content);
                                cellIndex.add(cell.getColumnIndex());
                            }
                        }
                        for (int cr = hssfSheet.getFirstRowNum() + 1; cr <= hssfSheet.getLastRowNum(); cr++) {
                            Row complexHssfRow = hssfSheet.getRow(cr);
                            if (hssfRow != null) {
                                for (int ci = 0; ci < cellIndex.size(); ci++) {
                                    Cell cell = complexHssfRow.getCell(cellIndex.get(ci));
                                    if (cell != null) {
                                        String content = ExcelUtil.getCellContent(cell);
                                        complexDataObj.put(headerList.get(ci), content);
                                    }else{
                                        String content = "";
                                        complexDataObj.put(headerList.get(ci), content);
                                    }
                                }
                            }
                            if(complexDataObj.getString("item_uuid").equals(baseRowItem)){
                                complexDataObj.remove("item_uuid");//用完后删掉
                                complexArray.add(complexDataObj);
                            }
                        }
                        JSONObject complexObj = new JSONObject();
                        complexObj.put("sheetName",sheetName);
                        complexObj.put("content",complexArray);
                        itemArray.add(complexObj);
                        continue;
                    }
                }
                array.add(itemArray);

            }
            //System.out.println("array:"+array);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                file.getInputStream().close();
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        return array;
    }


/*    private static String ExcelUtil.getCellContent(Cell cell) {
        String cellContent = "";
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                cellContent = (int) cell.getNumericCellValue() + "";
                break;
            case Cell.CELL_TYPE_STRING:
                cellContent = cell.getStringCellValue() + "";
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                cellContent = cell.getBooleanCellValue() + "";
                break;
            case Cell.CELL_TYPE_BLANK:
                cellContent = "";
                break;
            case Cell.CELL_TYPE_FORMULA:
                cellContent = cell.getCellFormula() + "";
                break;
            case Cell.CELL_TYPE_ERROR:
                cellContent = "error";
                break;
        }
        return cellContent;
    }*/

/*    public static void main(String[] args) throws Exception {
        JSONObject json = new JSONObject();
        File file = new File("C:\\Users\\12486\\Desktop\\0222\\基础软件导入.xlsx");
        MultipartFile file1 = (MultipartFile) file;
        System.out.println(getExcelDataFile(file1));
        JSONArray reportArray = getExcelDataFile(file1);
        if(reportArray!=null && reportArray.size()>0){
            for(int i=0;i<reportArray.size();i++){
                JSONObject obj = reportArray.getJSONObject(i);
                String sheetName = obj.getString("sheetName");
                if("主属性".equals(sheetName)){
                    json = (JSONObject) obj.getJSONArray("content").get(0);
                }else{
                    json.put(sheetName,obj.getJSONArray("content"));
                }
            }
        }
        System.out.println("json:"+json);

    }*/
}

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

package neatlogic.module.pbc.policy.handler;

import neatlogic.framework.asynchronization.threadlocal.TenantContext;
import neatlogic.framework.common.config.Config;
import neatlogic.framework.common.constvalue.SystemUser;
import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.common.util.FileUtil;
import neatlogic.framework.file.dao.mapper.FileMapper;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.*;
import neatlogic.framework.pbc.exception.CannotCreateDirException;
import neatlogic.framework.pbc.exception.CreateFileException;
import neatlogic.framework.pbc.exception.NoDataToCreateException;
import neatlogic.framework.pbc.policy.core.PhaseHandlerBase;
import neatlogic.framework.util.TimeUtil;
import neatlogic.framework.util.excel.ExcelBuilder;
import neatlogic.framework.util.excel.SheetBuilder;
import neatlogic.module.framework.file.handler.LocalFileSystemHandler;
import neatlogic.module.framework.file.handler.MinioFileSystemHandler;
import neatlogic.module.pbc.utils.ConfigManager;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CreateFilePhaseHandler extends PhaseHandlerBase {
    private static final Logger logger = LoggerFactory.getLogger(CreateFilePhaseHandler.class);

    @Override
    public String getPhase() {
        return "createfile";
    }

    @Override
    public String getPhaseLabel() {
        return "生成导入文件";
    }

    @Resource
    private InterfaceItemMapper interfaceItemMapper;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private PropertyMapper propertyMapper;

    @Override
    public List<PolicyConfigVo> getConfigTemplate() {
        List<PolicyConfigVo> configList = new ArrayList<>();
        configList.add(new PolicyConfigVo("filePath", "保存路径", "text", "如果设置保存路径，系统不会提供下载地址，请自行到服务器指定路径获取附件。如不设保存路径，系统自动保存附件，并生成下载链接"));//保存路径
        List<ValueTextVo> dataList = new ArrayList<>();
        dataList.add(new ValueTextVo("json", "json"));
        dataList.add(new ValueTextVo("excel", "excel"));
        configList.add(new PolicyConfigVo("fileType", "附件类型", "radio", "", 1, dataList));//保存路径
        return configList;
    }


    @Override
    protected String myExecute(PolicyAuditVo policyAuditVo, PolicyVo policyVo, PolicyPhaseVo policyPhaseVo, List<InterfaceVo> interfaceList) {
        List<InterfaceVo> interfaceAndItemList = interfaceItemMapper.getInterfaceItemByAuditId(policyAuditVo.getId());
        if (CollectionUtils.isEmpty(interfaceAndItemList)) {
            throw new NoDataToCreateException();
        }
        String tenantUuid = TenantContext.get().getTenantUuid();
        JSONObject returnObj = new JSONObject();
        JSONObject phaseConfig = policyPhaseVo.getConfig();
        String fileType = "json";
        if (MapUtils.isNotEmpty(phaseConfig) && StringUtils.isNotBlank(phaseConfig.getString("fileType"))) {
            fileType = phaseConfig.getString("fileType");
        }
        if (fileType.equals("json")) {
            JSONObject reportData = new JSONObject();
            String facilityOwnerAgency = ConfigManager.getConfig(policyVo.getCorporationId()).getFacilityOwnerAgency();
            reportData.put("facilityOwnerAgency", facilityOwnerAgency);
        /* //如果有结果代表不是第一次执行
        if (StringUtils.isNotBlank(policyPhaseVo.getResult())) {
            try {
                JSONObject result = JSONObject.parseObject(policyPhaseVo.getResult());
                reportData.put("branchId", result.getString("branchId"));
            } catch (Exception ignored) {

            }
        }*/
            reportData.put("branchId", "");
            JSONArray dataList = new JSONArray();

            for (InterfaceVo interfaceVo : interfaceAndItemList) {
                JSONObject interfaceData = new JSONObject();
                interfaceData.put("dataType", interfaceVo.getId());
                JSONArray interfaceDataList = new JSONArray();
                interfaceData.put("dataList", interfaceDataList);
                if (CollectionUtils.isNotEmpty(interfaceVo.getInterfaceItemList())) {
                    for (InterfaceItemVo interfaceItemVo : interfaceVo.getInterfaceItemList()) {
                        JSONObject dataObj = interfaceItemVo.getData();
                        dataObj.put("reportDataType", interfaceItemVo.getAction());
                        interfaceDataList.add(dataObj);
                    }
                }
                dataList.add(interfaceData);
            }

            //reportData.put("data", GzipUtil.compress(dataList.toJSONString()));
            reportData.put("data", dataList);
            //生成附件
            JSONArray filePathList = new JSONArray();
            if (StringUtils.isNotBlank(phaseConfig.getString("filePath"))) {
                String filePath = phaseConfig.getString("filePath") + File.separator + facilityOwnerAgency + File.separator + TimeUtil.yyyymmdd() + File.separator + policyVo.getId();
                writeContent(filePath, reportData.toJSONString());

                filePathList.add(filePath);
            } else {
                FileVo fileVo = new FileVo();
                fileVo.setName(policyVo.getId() + ".json");
                fileVo.setSize(0L);
                fileVo.setUserUuid(SystemUser.SYSTEM.getUserUuid());
                fileVo.setType("PBC_INTERFACE_ITEM_CUSTOM");
                fileVo.setContentType("text/plain");
                String filePath = null;
                InputStream is = new ByteArrayInputStream(reportData.toJSONString().getBytes());
                try {
                    filePath = FileUtil.saveData(MinioFileSystemHandler.NAME, tenantUuid, is, fileVo.getId().toString(), fileVo.getContentType(), fileVo.getType());
                    fileVo.setPath(filePath);
                } catch (Exception ex) {
                    //如果没有配置minioUrl，则表示不使用minio，无需抛异常
                    if (StringUtils.isNotBlank(Config.MINIO_URL())) {
                        logger.error(ex.getMessage(), ex);
                    }
                    try {
                        filePath = FileUtil.saveData(LocalFileSystemHandler.NAME, tenantUuid, is, fileVo.getId().toString(), fileVo.getContentType(), fileVo.getType());
                        fileVo.setPath(filePath);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (StringUtils.isNotBlank(fileVo.getPath())) {
                    fileMapper.insertFile(fileVo);
                    JSONObject fileObj = new JSONObject();
                    fileObj.put("id", fileVo.getId());
                    fileObj.put("name", fileVo.getName());
                    filePathList.add(fileObj);
                }
            }
            returnObj.put("filePathList", filePathList);
        } else {
            String facilityOwnerAgency = ConfigManager.getConfig(policyVo.getCorporationId()).getFacilityOwnerAgency();
            Map<String, ExcelBuilder> builderMap = new HashMap<>();
            Map<String, Map<String, PropertyVo>> propertyMap = new HashMap<>();
            for (InterfaceVo interfaceVo : interfaceAndItemList) {
                if (CollectionUtils.isNotEmpty(interfaceVo.getInterfaceItemList())) {
                    for (InterfaceItemVo interfaceItemVo : interfaceVo.getInterfaceItemList()) {
                        if (!builderMap.containsKey(interfaceItemVo.getAction() + "-" + interfaceVo.getName())) {
                            Map<String, PropertyVo> actionPropertyMap = new HashMap<>();
                            ExcelBuilder builder = new ExcelBuilder(XSSFWorkbook.class).withName(interfaceItemVo.getAction() + "-" + interfaceVo.getId() + ".xlsx").withColumnWidth(50).withHeadFontColor(HSSFColor.HSSFColorPredefined.WHITE).withHeadBgColor(HSSFColor.HSSFColorPredefined.LIGHT_BLUE);
                            if (StringUtils.isNotBlank(phaseConfig.getString("filePath"))) {
                                builder.withFilePath(phaseConfig.getString("filePath") + File.separator + facilityOwnerAgency + File.separator + TimeUtil.yyyymmdd() + File.separator + policyVo.getId() + File.separator + interfaceItemVo.getAction() + "-" + interfaceVo.getId() + ".xlsx");
                            }
                            //SheetBuilder sheetBuilder = builder.addSheet(interfaceVo.getName());
                            List<PropertyVo> propertyList = propertyMapper.getPropertyByInterfaceId(interfaceVo.getId());
                            propertyList.forEach(d -> {
                                if (StringUtils.isBlank(d.getComplexId())) {
                                    actionPropertyMap.put(d.getId(), d);
                                } else {
                                    actionPropertyMap.put(d.getComplexId() + "#" + d.getId(), d);
                                }
                            });

                            List<PropertyVo> simplePropertyList = propertyList.stream().filter(d -> StringUtils.isEmpty(d.getComplexId())).collect(Collectors.toList());//基础属性
                            List<PropertyVo> complexPropertyList = propertyList.stream().filter(d -> StringUtils.isNotEmpty(d.getComplexId())).collect(Collectors.toList());//基础属性
                            Map<String, PropertyVo> uniqueComplexPropertyMap = new LinkedHashMap<>();
                            complexPropertyList.forEach(propertyVo -> {
                                if (!uniqueComplexPropertyMap.containsKey(propertyVo.getComplexId())) {
                                    uniqueComplexPropertyMap.put(propertyVo.getComplexId(), propertyVo);
                                }
                            });
                            //baseHeaderList.add("facilityDescriptor(设施标识符)");
                            List<String> baseHeaderList = simplePropertyList.stream().map(d -> d.getId() + "(" + d.getName() + ")").collect(Collectors.toList());
                            //baseColumnList.add("id");
                            List<String> baseColumnList = simplePropertyList.stream().map(PropertyVo::getId).collect(Collectors.toList());
                            SheetBuilder baseSheetBuilder = builder.addSheet("_main", "主属性").withHeaderList(baseHeaderList).withColumnList(baseColumnList);
                            for (PropertyVo propertyVo : simplePropertyList) {
                                if (propertyVo.getInputType().equals(PropertyVo.InputType.SELECT.getValue())) {
                                    List<String> enumTextList = propertyVo.getEnumList().stream().map(EnumVo::getValue).collect(Collectors.toList());
                                    String[] sList = enumTextList.toArray(new String[]{});
                                    baseSheetBuilder.addValidation(propertyVo.getId(), sList);
                                }
                            }
                            for (String complexId : uniqueComplexPropertyMap.keySet()) {
                                List<PropertyVo> subPropertyList = propertyList.stream().filter(d -> d.getComplexId().equals(complexId)).collect(Collectors.toList());
                                //headerList.add("facilityDescriptor(设施标识符)");
                                List<String> headerList = subPropertyList.stream().map(d -> d.getId() + "(" + d.getName() + ")").collect(Collectors.toList());
                                //columnList.add("id");
                                List<String> columnList = subPropertyList.stream().map(PropertyVo::getId).collect(Collectors.toList());
                                String complexName = uniqueComplexPropertyMap.get(complexId).getComplexName();
                                if (complexName.contains("-")) {
                                    complexName = complexName.substring(complexName.indexOf("-") + 1);
                                }
                                SheetBuilder sheetBuilder = builder.addSheet(complexId, complexName).withHeaderList(headerList).withColumnList(columnList);
                                for (PropertyVo propertyVo : subPropertyList) {
                                    if (propertyVo.getInputType().equals(PropertyVo.InputType.SELECT.getValue())) {
                                        List<String> enumTextList = propertyVo.getEnumList().stream().map(EnumVo::getValue).collect(Collectors.toList());
                                        String[] sList = enumTextList.toArray(new String[]{});
                                        sheetBuilder.addValidation(propertyVo.getId(), sList);
                                    }
                                }
                            }
                            propertyMap.put(interfaceItemVo.getAction() + "-" + interfaceVo.getName(), actionPropertyMap);
                            builderMap.put(interfaceItemVo.getAction() + "-" + interfaceVo.getName(), builder);
                        }
                        ExcelBuilder builder = builderMap.get(interfaceItemVo.getAction() + "-" + interfaceVo.getName());
                        Map<String, PropertyVo> actionPropertyMap = propertyMap.get(interfaceItemVo.getAction() + "-" + interfaceVo.getName());
                        JSONObject data = interfaceItemVo.getData();
                        if (MapUtils.isNotEmpty(data)) {
                            Map<String, Object> baseRow = new HashMap<>();
                            for (String key : data.keySet()) {
                                //baseRow.put("id", interfaceItemVo.getId());
                                SheetBuilder sheetBuilder = builder.getSheetBuilderById(key);
                                if (data.get(key) instanceof JSONArray && sheetBuilder != null) {
                                    JSONArray subDataList = data.getJSONArray(key);
                                    if (CollectionUtils.isNotEmpty(subDataList)) {
                                        for (int i = 0; i < subDataList.size(); i++) {
                                            Map<String, Object> subData = subDataList.getJSONObject(i);
                                            //subData.put("id", interfaceItemVo.getId());
                                            /*for (String subKey : subData.keySet()) {
                                                PropertyVo prop = actionPropertyMap.get(key + "#" + subKey);
                                                if (prop != null) {
                                                    if (prop.getInputType().equals(PropertyVo.InputType.SELECT.getValue())) {
                                                        Optional<EnumVo> op = prop.getEnumList().stream().filter(d -> d.getValue().equals(subData.get(subKey))).findFirst();
                                                        op.ifPresent(enumVo -> subData.put(subKey, enumVo.getText()));
                                                    }
                                                }
                                            }*/
                                            sheetBuilder.addData(subData);
                                        }
                                    }
                                } else {
                                    PropertyVo prop = actionPropertyMap.get(key);
                                    if (prop != null) {
                                        if (!prop.getInputType().equals(PropertyVo.InputType.SELECT.getValue())) {
                                            baseRow.put(key, data.get(key));
                                        } else {
                                            //Optional<EnumVo> op = prop.getEnumList().stream().filter(d -> d.getValue().equals(data.get(key))).findFirst();
                                            //if (op.isPresent()) {
                                            //    baseRow.put(key, op.get().getText());
                                            //} else {
                                            baseRow.put(key, data.get(key));
                                            //}
                                        }
                                    }
                                }
                            }
                            builder.getSheetBuilderById("_main").addData(baseRow);
                        }
                    }
                }
            }
            JSONArray filePathList = new JSONArray();
            for (String key : builderMap.keySet()) {
                ExcelBuilder builder = builderMap.get(key);
                Workbook workbook = builder.build();
                if (StringUtils.isNotBlank(builder.getFilePath())) {
                    File file = new File(builder.getFilePath());
                    boolean hasDir = file.getParentFile().exists();
                    if (!hasDir) {
                        file.getParentFile().mkdirs();
                    }
                    try (OutputStream fis = Files.newOutputStream(file.toPath())) {
                        workbook.write(fis);
                        workbook.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    filePathList.add(builder.getFilePath());
                } else {
                    FileVo fileVo = new FileVo();
                    fileVo.setName(builder.getName());
                    fileVo.setSize(0L);
                    fileVo.setUserUuid(SystemUser.SYSTEM.getUserUuid());
                    fileVo.setType("PBC_INTERFACE_ITEM_CUSTOM");
                    fileVo.setContentType("application/vnd.ms-excel");
                    String filePath;
                    try {
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        workbook.write(bos);
                        byte[] barray = bos.toByteArray();
                        InputStream is = new ByteArrayInputStream(barray);
                        try {
                            filePath = FileUtil.saveData(MinioFileSystemHandler.NAME, tenantUuid, is, fileVo.getId().toString(), fileVo.getContentType(), fileVo.getType());
                            fileVo.setPath(filePath);
                        } catch (Exception ex) {
                            //如果没有配置minioUrl，则表示不使用minio，无需抛异常
                            if (StringUtils.isNotBlank(Config.MINIO_URL())) {
                                logger.error(ex.getMessage(), ex);
                            }
                            filePath = FileUtil.saveData(LocalFileSystemHandler.NAME, tenantUuid, is, fileVo.getId().toString(), fileVo.getContentType(), fileVo.getType());
                            fileVo.setPath(filePath);
                        }
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                    if (StringUtils.isNotBlank(fileVo.getPath())) {
                        fileMapper.insertFile(fileVo);
                        JSONObject fileObj = new JSONObject();
                        fileObj.put("id", fileVo.getId());
                        fileObj.put("name", fileVo.getName());
                        filePathList.add(fileObj);
                    }
                }
            }
            returnObj.put("filePathList", filePathList);
        }

        //处理成功需要修改interfaceItem状态
        interfaceItemMapper.updateInterfaceItemDataHashByAuditId(policyAuditVo.getId());
        //删除需要删除的数据
        List<Long> deleteInterfaceIdList = interfaceItemMapper.getNeedDeleteInterfaceItemIdByAuditId(policyAuditVo.getId());
        if (CollectionUtils.isNotEmpty(deleteInterfaceIdList)) {
            for (Long id : deleteInterfaceIdList) {
                interfaceItemMapper.deleteInterfaceItemById(id);
            }
        }
        //更新policy的last_action_date字段
        policyMapper.updatePolicyLastExecDate(policyAuditVo.getPolicyId());
        return returnObj.toString();
    }

    private void writeContent(String savePath, String content) {
        OutputStreamWriter fw;
        File file = new File(savePath);
        boolean hasDir = file.getParentFile().exists();
        if (!hasDir) {
            hasDir = file.getParentFile().mkdirs();
        }
        if (!hasDir) {
            throw new CannotCreateDirException(file.getParentFile().getAbsolutePath());
        }
        try {
            OutputStream fis = Files.newOutputStream(file.toPath());
            fw = new OutputStreamWriter(fis, StandardCharsets.UTF_8);
            fw.write(content);
            fw.close();
            fis.close();
        } catch (Exception e) {
            throw new CreateFileException(e.getMessage());
        }
    }


}

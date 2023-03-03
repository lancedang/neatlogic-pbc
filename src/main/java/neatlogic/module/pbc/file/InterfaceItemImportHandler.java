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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.file.core.FileTypeHandlerBase;
import neatlogic.framework.file.dto.FileVo;
import neatlogic.framework.pbc.dao.mapper.InterfaceItemMapper;
import neatlogic.framework.pbc.dao.mapper.InterfaceMapper;
import neatlogic.framework.pbc.dao.mapper.PropertyMapper;
import neatlogic.framework.pbc.dto.EnumVo;
import neatlogic.framework.pbc.dto.InterfaceItemVo;
import neatlogic.framework.pbc.dto.InterfaceVo;
import neatlogic.framework.pbc.dto.PropertyVo;
import neatlogic.framework.pbc.exception.InterfaceIdIsEmptyException;
import neatlogic.framework.pbc.exception.InterfaceNotFoundException;
import neatlogic.framework.pbc.exception.InterfacePropertyNotFoundException;
import neatlogic.framework.util.excel.ExcelParser;
import neatlogic.framework.util.excel.ExcelVo;
import neatlogic.module.pbc.utils.InterfaceItemUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InterfaceItemImportHandler extends FileTypeHandlerBase {
    @Resource
    private InterfaceMapper interfaceMapper;

    @Resource
    private InterfaceItemMapper interfaceItemMapper;

    @Resource
    private PropertyMapper propertyMapper;

    @Override
    public boolean valid(String userUuid, FileVo fileVo, JSONObject jsonObj) {
        return true;
    }

    @Override
    public String getDisplayName() {
        return "导入人行接口数据文件";
    }

    @Override
    public boolean needSave() {
        return false;
    }


    @Override
    public void analyze(MultipartFile file, JSONObject paramObj) throws Exception {
        String interfaceId = paramObj.getString("interfaceId");
        Long corporationId = paramObj.getLong("corporationId");
        if (StringUtils.isBlank(interfaceId)) {
            throw new InterfaceIdIsEmptyException();
        }
        InterfaceVo interfaceVo = interfaceMapper.getInterfaceById(interfaceId);
        if (interfaceVo == null) {
            throw new InterfaceNotFoundException(interfaceId);
        }
        List<PropertyVo> propertyList = propertyMapper.getPropertyByInterfaceId(interfaceId);
        if (CollectionUtils.isEmpty(propertyList)) {
            throw new InterfacePropertyNotFoundException(interfaceId);
        }
        Map<String, List<EnumVo>> propertyEnumMap = new HashMap<>();
        Map<String, String> complexNameMap = new HashMap<>();
        for (PropertyVo propertyVo : propertyList) {
            if (StringUtils.isNotBlank(propertyVo.getComplexId())) {
                complexNameMap.put(propertyVo.getComplexName(), propertyVo.getComplexId());
            }
            if (CollectionUtils.isNotEmpty(propertyVo.getEnumList())) {
                if (StringUtils.isNotBlank(propertyVo.getComplexId())) {
                    propertyEnumMap.put(propertyVo.getComplexId() + "#" + propertyVo.getId(), propertyVo.getEnumList());
                } else {
                    propertyEnumMap.put(propertyVo.getId(), propertyVo.getEnumList());
                }
            }
        }

        InputStream input = file.getInputStream();
        ExcelParser parser = new ExcelParser(input);
        ExcelVo excelVo = parser.parseToObject();
        JSONObject data = excelVo.toJson();
        JSONArray baseDataList = data.getJSONArray("主属性");
        for (int i = 0; i < baseDataList.size(); i++) {
            InterfaceItemVo interfaceItemVo = new InterfaceItemVo();
            interfaceItemVo.setInterfaceId(interfaceId);
            interfaceItemVo.setCorporationId(corporationId);
            interfaceItemVo.setFcu(UserContext.get().getUserUuid(true));
            interfaceItemVo.setLcu(UserContext.get().getUserUuid(true));
            JSONObject baseDataObj = baseDataList.getJSONObject(i);
            Long id = baseDataObj.getLong("主属性id#id");
            JSONObject finalBaseDataObj = new JSONObject();
            for (String key : baseDataObj.keySet()) {
                String propertyId = key.split("#").length == 2 ? key.split("#")[1] : "";
                if (StringUtils.isNotBlank(propertyId) && baseDataObj.get(key) != null) {
                    if (propertyEnumMap.containsKey(propertyId)) {
                        List<EnumVo> enumList = propertyEnumMap.get(propertyId);
                        Optional<EnumVo> op = enumList.stream().filter(d -> d.getText().equalsIgnoreCase(baseDataObj.get(key).toString())).findFirst();
                        if (op.isPresent()) {
                            finalBaseDataObj.put(propertyId, op.get().getValue());
                        } else {
                            finalBaseDataObj.put(propertyId, baseDataObj.get(key));
                        }
                    } else {
                        finalBaseDataObj.put(propertyId, baseDataObj.get(key));
                    }
                }
            }
            for (String sheetName : data.keySet()) {
                if (!sheetName.equals("主属性")) {
                    String complexId = complexNameMap.get(sheetName);
                    if (StringUtils.isNotBlank(complexId)) {
                        JSONArray subDataList = data.getJSONArray(complexId);
                        if (CollectionUtils.isNotEmpty(subDataList)) {
                            JSONArray finalSubDataList = new JSONArray();
                            for (int j = 0; j < subDataList.size(); j++) {
                                JSONObject subDataObj = subDataList.getJSONObject(j);
                                if (subDataObj.getLong("主属性id#id").equals(id)) {
                                    JSONObject finalSubDataObj = new JSONObject();
                                    for (String subKey : subDataObj.keySet()) {
                                        String propertyId = subKey.split("#").length == 2 ? subKey.split("#")[1] : "";
                                        if (StringUtils.isNotBlank(propertyId) && subDataObj.get(subKey) != null) {
                                            if (propertyEnumMap.containsKey(complexId + "#" + propertyId)) {
                                                List<EnumVo> enumList = propertyEnumMap.get(complexId + "#" + propertyId);
                                                Optional<EnumVo> op = enumList.stream().filter(d -> d.getText().equalsIgnoreCase(subDataObj.get(subKey).toString())).findFirst();
                                                if (op.isPresent()) {
                                                    finalSubDataObj.put(propertyId, op.get().getValue());
                                                } else {
                                                    finalSubDataObj.put(propertyId, subDataObj.get(subKey));
                                                }
                                            } else {
                                                finalSubDataObj.put(propertyId, subDataObj.get(subKey));
                                            }
                                        }
                                    }
                                    finalSubDataList.add(finalSubDataObj);
                                }
                            }
                            finalBaseDataObj.put(complexId, finalSubDataList);
                        }
                    }
                }
            }
            interfaceItemVo.setData(finalBaseDataObj);
            //如果datahash不一样才更新，datahash会在ReportPhaser处理完成后被更新掉
            InterfaceItemVo oldInterfaceItemVo = interfaceItemMapper.getInterfaceItemById(id);
            if (oldInterfaceItemVo == null) {
                InterfaceItemUtil.validData(interfaceItemVo, propertyList);
                interfaceItemMapper.insertInterfaceItem(interfaceItemVo);
            } else if (StringUtils.isBlank(oldInterfaceItemVo.getDataHash()) || !oldInterfaceItemVo.getDataHash().equalsIgnoreCase(interfaceItemVo.getCurrentDataHash())) {
                interfaceItemVo.setId(oldInterfaceItemVo.getId());
                InterfaceItemUtil.validData(interfaceItemVo, propertyList);
                interfaceItemMapper.updateInterfaceItem(interfaceItemVo);
            }
        }
    }


    @Override
    protected boolean myDeleteFile(FileVo fileVo, JSONObject paramObj) {
        return true;
    }


    @Override
    public String getName() {
        return "PBC_INTERFACE_ITEM_IMPORT";
    }

}

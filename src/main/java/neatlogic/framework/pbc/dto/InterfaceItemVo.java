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
import neatlogic.framework.common.dto.BaseEditorVo;
import neatlogic.framework.restful.annotation.EntityField;
import neatlogic.framework.util.GzipUtil;
import neatlogic.framework.util.Md5Util;
import neatlogic.framework.util.SnowflakeUtil;
import neatlogic.module.pbc.utils.SqlRunner;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class InterfaceItemVo extends BaseEditorVo {


    public InterfaceItemVo() {

    }

    public InterfaceItemVo(String _interfaceId) {
        this.interfaceId = _interfaceId;
    }

    @EntityField(name = "id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "接口id", type = ApiParamType.STRING)
    private String interfaceId;
    @EntityField(name = "接口uid", type = ApiParamType.LONG)
    private Long interfaceUid;
    @EntityField(name = "配置项id", type = ApiParamType.LONG)
    private Long ciEntityId;
    @EntityField(name = "视图id", type = ApiParamType.LONG)
    private Long customViewId;
    @EntityField(name = "模型id", type = ApiParamType.LONG)
    private Long ciId;
    @EntityField(name = "异常", type = ApiParamType.JSONOBJECT)
    private JSONObject error;
    @EntityField(name = "异常数量", type = ApiParamType.INTEGER)
    private int errorCount;
    @JSONField(serialize = false)
    private String errorStr;
    @JSONField(serialize = false)
    private String dataStr;
    @EntityField(name = "数据散列", type = ApiParamType.STRING)
    private String dataHash;
    @EntityField(name = "数据", type = ApiParamType.JSONOBJECT)
    private JSONObject data;
    private JSONObject dataText;
    @EntityField(name = "更新时间", type = ApiParamType.LONG)
    private Date updateTime;
    @EntityField(name = "是否新数据", type = ApiParamType.INTEGER)
    private int isNew;
    @EntityField(name = "是否删除", type = ApiParamType.INTEGER)
    private int isDelete;
    @EntityField(name = "是否上报过", type = ApiParamType.INTEGER)
    private int isImported;

    @EntityField(name = "机构id", type = ApiParamType.LONG)
    private Long corporationId;
    @EntityField(name = "数据主键，格式是32位散列值", type = ApiParamType.STRING)
    private String primaryKey;

    @EntityField(name = "上报操作，有new,update和delete", type = ApiParamType.STRING)
    private String action;

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomViewId() {
        return customViewId;
    }

    public void setCustomViewId(Long customViewId) {
        this.customViewId = customViewId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Long getCiId() {
        return ciId;
    }

    public void setCiId(Long ciId) {
        this.ciId = ciId;
    }

    public Long getInterfaceUid() {
        return interfaceUid;
    }

    public void setInterfaceUid(Long interfaceUid) {
        this.interfaceUid = interfaceUid;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public Long getCiEntityId() {
        return ciEntityId;
    }

    public void setCiEntityId(Long ciEntityId) {
        this.ciEntityId = ciEntityId;
    }


    public String getDataStr() {
        if (StringUtils.isEmpty(dataStr) && MapUtils.isNotEmpty(data)) {
            dataStr = data.toString();
        }
        return dataStr;
    }

    public String getCompressData() {
        return GzipUtil.compress(dataStr);
    }

    public void setDataStr(String dataStr) {
        this.dataStr = dataStr;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public int getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(int isDelete) {
        this.isDelete = isDelete;
    }

    public int getIsImported() {
        return isImported;
    }

    public void setIsImported(int isImported) {
        this.isImported = isImported;
    }

    public JSONObject getData() {
        if (StringUtils.isNotBlank(dataStr)) {
            try {
                data = JSONObject.parseObject(dataStr);
            } catch (Exception ignored) {

            }
        }
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
        this.dataStr = null;//有新数据时清空旧值
    }

    public Long getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(Long corporationId) {
        this.corporationId = corporationId;
    }

    public JSONObject getDataText() {
        if (dataText == null && StringUtils.isNotBlank(dataStr)) {
            try {
                dataText = JSONObject.parseObject(dataStr);
                for (String k : dataText.keySet()) {
                    if (dataText.get(k) instanceof JSONArray) {
                        for (int i = 0; i < dataText.getJSONArray(k).size(); i++) {
                            for (String sk : dataText.getJSONArray(k).getJSONObject(i).keySet()) {
                                if (SqlRunner.checkPropertyHasEnum(sk)) {
                                    String v = SqlRunner.getEnumText(sk, dataText.getJSONArray(k).getJSONObject(i).getString(sk));
                                    /*if (StringUtils.isNotBlank(v)) {
                                        v = "<span class=\"enum\">" + v + "</span>";
                                    }*/
                                    dataText.getJSONArray(k).getJSONObject(i).put(sk, v);
                                }
                            }
                        }
                    } else {
                        if (SqlRunner.checkPropertyHasEnum(k)) {
                            String v = SqlRunner.getEnumText(k, dataText.getString(k));
                            /*if (StringUtils.isNotBlank(v)) {
                                v = "<span class=\"enum\">" + v + "</span>";
                            }*/
                            dataText.put(k, v);
                        }
                    }
                }
            } catch (Exception ignored) {

            }
        }
        return dataText;
    }

    public void setDataText(JSONObject dataText) {
        this.dataText = dataText;
    }

    public JSONObject getError() {
        if (error == null && StringUtils.isNotBlank(errorStr)) {
            try {
                error = JSONObject.parseObject(errorStr);
            } catch (Exception ignored) {

            }
        }
        return error;
    }

    public void setError(JSONObject error) {
        this.error = error;
        this.errorStr = null;
    }

    public String getErrorStr() {
        if (StringUtils.isBlank(errorStr) && MapUtils.isNotEmpty(error)) {
            errorStr = error.toString();
        }
        return errorStr;
    }

    public void setErrorStr(String errorStr) {
        this.errorStr = errorStr;
    }


    public String getDataHash() {
        return dataHash;
    }

    public String getCurrentDataHash() {
        if (MapUtils.isNotEmpty(data)) {
            return Md5Util.encryptMD5(data.toString());
        } else {
            return "";
        }
    }

    public void setDataHash(String dataHash) {
        this.dataHash = dataHash;
    }


    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public int getIsNew() {
        return isNew;
    }

    public void setIsNew(int isNew) {
        this.isNew = isNew;
    }

    public int getErrorCount() {
        if (MapUtils.isNotEmpty(this.error)) {
            for (String key : this.error.keySet()) {
                if (this.error.get(key) instanceof String) {
                    errorCount += 1;
                } else if (this.error.get(key) instanceof JSONObject) {
                    JSONObject subError = this.error.getJSONObject(key);
                    for (String index : subError.keySet()) {
                        for (String subKey : subError.getJSONObject(index).keySet()) {
                            errorCount += 1;
                        }
                    }
                }
            }
        }
        return errorCount;
    }

    public String getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
    }
}

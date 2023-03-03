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
import neatlogic.framework.restful.annotation.EntityField;
import neatlogic.framework.util.SnowflakeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

public class CorporationVo implements Serializable {
    @EntityField(name = "id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "名称", type = ApiParamType.STRING)
    private String name;
    @EntityField(name = "配置文本", type = ApiParamType.STRING)
    private String configStr;
    @EntityField(name = "配置", type = ApiParamType.JSONOBJECT)
    private JSONObject config;
    @EntityField(name = "数据量", type = ApiParamType.INTEGER)
    private Integer itemCount;

    @JSONField(serialize = false)
    private CorporationConfigVo corporationConfigVo;

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfigStr() {
        if (StringUtils.isBlank(configStr) && config != null) {
            configStr = config.toString();
        }
        return configStr;
    }

    public void setConfigStr(String configStr) {
        this.configStr = configStr;
    }

    public JSONObject getConfig() {
        if (config == null && StringUtils.isNotBlank(configStr)) {
            try {
                config = JSONObject.parseObject(configStr);
            } catch (Exception ignored) {

            }
        }
        return config;
    }

    public void setConfig(JSONObject config) {
        this.config = config;
    }

    public CorporationConfigVo getCorporationConfigVo() {
        if (corporationConfigVo == null) {
            JSONObject config = this.getConfig();
            if (MapUtils.isNotEmpty(config)) {
                corporationConfigVo = JSON.toJavaObject(config, CorporationConfigVo.class);
            }
        }
        return corporationConfigVo;
    }

    public void setCorporationConfigVo(CorporationConfigVo _corporationConfigVo) {
        this.corporationConfigVo = _corporationConfigVo;
    }
}

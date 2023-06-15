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

package neatlogic.module.pbc.api.corporation;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.CorporationMapper;
import neatlogic.framework.pbc.dto.CorporationConfigVo;
import neatlogic.framework.pbc.dto.CorporationVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.UPDATE)
public class SaveCorporationApi extends PrivateApiComponentBase {

    @Resource
    private CorporationMapper corporationMapper;

    @Override
    public String getToken() {
        return "/pbc/corporation/save";
    }

    @Override
    public String getName() {
        return "保存上报配置";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "id，不提供代表添加"),
            @Param(name = "name", type = ApiParamType.STRING, isRequired = true, desc = "名称"),
            @Param(name = "loginUrl", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "认证地址"),
            @Param(name = "loginUrl", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "认证地址"),
            @Param(name = "reportUrl", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "上报数据地址"),
            //@Param(name = "importUrl", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "入库申请地址"),
            @Param(name = "validUrl", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "申请检核地址"),
            @Param(name = "selectDataUrl", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "查询数据处理状态"),
            @Param(name = "validResultUrl", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "查询检核结果地址"),
            @Param(name = "authType", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "认证类型"),
            @Param(name = "clientId", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "客户端ID"),
            @Param(name = "clientSecret", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "客户端密码"),
            @Param(name = "facilityOwnerAgency", type = ApiParamType.STRING, isRequired = true, xss = true, desc = "金融机构编码"),
            //@Param(name = "pollCount", type = ApiParamType.INTEGER, isRequired = true, desc = "轮询次数"),
            //@Param(name = "pollInterval", type = ApiParamType.INTEGER, isRequired = true, desc = "轮询间隔(分钟)")
    })
    @Description(desc = "保存上报配置接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        CorporationVo corporationVo = JSONObject.toJavaObject(paramObj, CorporationVo.class);
        CorporationConfigVo corporationConfigVo = JSONObject.toJavaObject(paramObj, CorporationConfigVo.class);
        corporationVo.setConfigStr(JSONObject.toJSONString(corporationConfigVo));
        corporationMapper.saveCorporation(corporationVo);
        return null;
    }

}

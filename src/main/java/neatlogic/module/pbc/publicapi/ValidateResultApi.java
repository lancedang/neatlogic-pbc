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

package neatlogic.module.pbc.publicapi;

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;


@Service
@OperationType(type = OperationTypeEnum.OPERATE)
public class ValidateResultApi extends PublicApiComponentBase {


    @Override
    public String getToken() {
        return "/webproxy/fig2fics/pshare/api/prod/FICS/api/fics/dataElementInstance/selectUploadData";
    }

    @Override
    public String getName() {
        return "金融机构端使用此接口发送数据元检核请求";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Override
    public boolean isRaw() {
        return true;
    }


    @Input({@Param(name = "grant_type", type = ApiParamType.STRING, desc = "grant_type"),
            @Param(name = "client_id", type = ApiParamType.STRING, desc = "client_id"),
            @Param(name = "client_secret", type = ApiParamType.STRING, desc = "client_secret")})
    @Description(desc = "金融机构端使用此接口发送数据元检核请求")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        System.out.println("结果审核：" + paramObj.toJSONString());
        return JSONObject.parse("{\n" +
                "\"branchId\": \"5f11d9471e33ff0ec08b970c\",\n" +
                "\"code\": \"WL-10008\",\n" +
                "\"msg\": \"部分失败\",\n" +
                "\"data\": [{\n" +
                "\"code\": \"WL-20001\",\n" +
                "\"msg\": \"[facilityUsedState]不在填报范围\",\n" +
                "\"facilityCategory\": \"FAITSERPCS\",\n" +
                "\"facilityDescriptor\": \"5f11db861e33ff0ec08ba546\"\n" +
                "},\n" +
                "{\n" +
                "\"code\": \"WL-20002 \",\n" +
                "\"msg\": \"数据处理失败\",\n" +
                "\"facilityCategory\": \"FAITSERPCS\",\n" +
                "\"facilityDescriptor\": \"5f14ff501e33ff0ec08dd312\"\n" +
                "}\n" +
                "]\n" +
                "}\n");
    }
}

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
public class TokenApi extends PublicApiComponentBase {


    @Override
    public String getToken() {
        return "/webproxy/fig2fics/oauth2/v1/pshare/oauth/token";
    }

    @Override
    public String getName() {
        return "登陆";
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
    @Description(desc = "获取动态令牌接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        System.out.println("申请令牌：" + paramObj.toString());
        return JSONObject.parseObject("{\n" +
                "            \"access_token\": \"30a8f75012e84a40823c9722dfc23d5c\",\n" +
                "                \"token_type\": \"bearer\",\n" +
                "                \"expires_in\": 593,\n" +
                "                \"scope\": \"read writer\",\n" +
                "                \"tenant_id\": \"8a8080546e86d42e016f2c8481a00936\",\n" +
                "                \"client_short_name\": \"A1000*********1\",\n" +
                "                \"name\": \"A1000153000931_PBC\",\n" +
                "                \"description\": \"中国人民银行**分行\",\n" +
                "                \"platform\": \"pshare\",\n" +
                "                \"client_id\": \"A1000*********1_PBC\"\n" +
                "        }");
    }
}

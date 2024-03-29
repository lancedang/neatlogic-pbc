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

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.exception.core.ApiRuntimeException;
import neatlogic.framework.pbc.exception.LoginFailedException;
import neatlogic.framework.util.HttpRequestUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

public class TokenUtil {
    public static String getToken(Long corporationId) throws LoginFailedException {
        JSONObject paramObj = new JSONObject();
        paramObj.put("grant_type", ConfigManager.getConfig(corporationId).getAuthType());
        paramObj.put("client_id", ConfigManager.getConfig(corporationId).getClientId());
        paramObj.put("client_secret", ConfigManager.getConfig(corporationId).getClientSecret());
        HttpRequestUtil httpRequestUtil = HttpRequestUtil.post(ConfigManager.getConfig(corporationId).getLoginUrl())
                //.setAuthType(AuthenticateType.BASIC)
                //.setUsername("neatlogic")
                //.setTenant(TenantContext.get().getTenantUuid())
                //.setPassword("x15wDEzSbBL6tV1W")
                .setContentType(HttpRequestUtil.ContentType.CONTENT_TYPE_APPLICATION_FORM)
                .setFormData(paramObj)
                //.setPayload(paramObj.toJSONString())
                .sendRequest();
        if (StringUtils.isNotBlank(httpRequestUtil.getError())) {
            throw new ApiRuntimeException(httpRequestUtil.getError());
        } else {
            JSONObject result = httpRequestUtil.getResultJson();
            if (MapUtils.isNotEmpty(result)) {
                return result.getString("access_token");
            }
        }
        return null;
    }
}

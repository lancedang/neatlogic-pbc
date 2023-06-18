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

package neatlogic.module.pbc.api.test;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.publicapi.PublicApiComponentBase;
import neatlogic.framework.util.UuidUtil;
import org.springframework.stereotype.Service;


@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class RequestCheckApi extends PublicApiComponentBase {

    @Override
    public boolean isRaw() {
        return true;
    }

    @Override
    public String getToken() {
        return "/pbc/test/requestcheck";
    }

    @Override
    public String getName() {
        return "数据元检核请求测试接口";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "branchIdList", type = ApiParamType.STRING, isRequired = true, desc = "批次id"),
            @Param(name = "branchNumber", type = ApiParamType.INTEGER, isRequired = true, desc = "批次数量"),
            @Param(name = "facilityOwnerAgency", type = ApiParamType.STRING, isRequired = true, desc = "金融机构编码")})
    @Description(desc = "数据元检核请求测试接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        JSONObject data = new JSONObject();
        data.put("groupId", UuidUtil.randomUuid());
        data.put("code", "WL-30000");
        data.put("msg", "接收成功");
        return data;
    }


}

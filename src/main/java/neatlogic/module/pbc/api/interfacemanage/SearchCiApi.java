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

package neatlogic.module.pbc.api.interfacemanage;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.cmdb.crossover.ICiCrossoverMapper;
import neatlogic.framework.cmdb.dto.ci.CiVo;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchCiApi extends PrivateApiComponentBase {


    @Override
    public String getName() {
        return "查询模型";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true)
    })
    @Output({@Param(explode = CiVo[].class)})
    @Description(desc = "查询所有公共自定义视图")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        CiVo ciVo = JSONObject.toJavaObject(paramObj, CiVo.class);
        ICiCrossoverMapper ciCrossoverMapper = CrossoverServiceFactory.getApi(ICiCrossoverMapper.class);
        return ciCrossoverMapper.searchCi(ciVo);
    }

    @Override
    public String getToken() {
        return "/pbc/ci/search";
    }
}

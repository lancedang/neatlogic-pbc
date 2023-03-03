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
import neatlogic.framework.cmdb.crossover.ICustomViewCrossoverService;
import neatlogic.framework.cmdb.dto.customview.CustomViewAttrVo;
import neatlogic.framework.cmdb.dto.customview.CustomViewConstAttrVo;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetCustomViewAttrApi extends PrivateApiComponentBase {


    @Override
    public String getName() {
        return "获取自定义视图属性";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "customViewId", type = ApiParamType.LONG, desc = "自定义视图id")})
    @Output({@Param(explode = CustomViewAttrVo[].class)})
    @Description(desc = "获取自定义视图属性接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        CustomViewAttrVo customViewAttrVo = JSONObject.toJavaObject(paramObj, CustomViewAttrVo.class);
        customViewAttrVo.setIsHidden(0);
        CustomViewConstAttrVo customViewConstAttrVo = JSONObject.toJavaObject(paramObj, CustomViewConstAttrVo.class);
        customViewConstAttrVo.setIsHidden(0);
        ICustomViewCrossoverService customViewCrossoverService = CrossoverServiceFactory.getApi(ICustomViewCrossoverService.class);
        List<CustomViewAttrVo> customViewAttrList = customViewCrossoverService.getCustomViewAttrByCustomViewId(customViewAttrVo);
        List<CustomViewConstAttrVo> customViewConstAttrList = customViewCrossoverService.getCustomViewConstAttrByCustomViewId(customViewConstAttrVo);
        List<JSONObject> returnList = new ArrayList<>();
        boolean hasPrimaryKey = false;
        for (CustomViewAttrVo customViewAttr : customViewAttrList) {
            if (StringUtils.isNotBlank(customViewAttr.getName())) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("name", customViewAttr.getName());
                jsonObj.put("label", customViewAttr.getAlias());
                jsonObj.put("sort", customViewAttr.getSort());
                jsonObj.put("type", "attr");
                if (customViewAttr.getIsPrimary() != null && customViewAttr.getIsPrimary().equals(1)) {
                    hasPrimaryKey = true;
                }
                returnList.add(jsonObj);
            }
        }
        for (CustomViewConstAttrVo customViewConstAttr : customViewConstAttrList) {
            if (StringUtils.isNotBlank(customViewConstAttr.getName())) {
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("name", customViewConstAttr.getName());
                jsonObj.put("label", customViewConstAttr.getAlias());
                jsonObj.put("sort", customViewConstAttr.getSort());
                jsonObj.put("type", "constattr");
                if (customViewConstAttr.getIsPrimary() != null && customViewConstAttr.getIsPrimary().equals(1)) {
                    hasPrimaryKey = true;
                }
                returnList.add(jsonObj);
            }
        }
        if (hasPrimaryKey) {
            JSONObject pkObj = new JSONObject();
            pkObj.put("name", "_primarykey");
            pkObj.put("label", "主键");
            pkObj.put("sort", -1);//主键排在第一
            pkObj.put("type", "pk");
            returnList.add(pkObj);
        }
        returnList.sort(Comparator.comparingInt(o -> o.getIntValue("sort")));
        returnList.forEach(d -> d.remove("sort"));
        return returnList;
    }

    @Override
    public String getToken() {
        return "/pbc/customview/attr/list";
    }
}

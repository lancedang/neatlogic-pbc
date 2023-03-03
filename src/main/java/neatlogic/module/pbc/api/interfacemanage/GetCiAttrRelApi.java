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
import neatlogic.framework.cmdb.crossover.IAttrCrossoverMapper;
import neatlogic.framework.cmdb.crossover.IRelCrossoverMapper;
import neatlogic.framework.cmdb.dto.ci.AttrVo;
import neatlogic.framework.cmdb.dto.ci.RelVo;
import neatlogic.framework.cmdb.dto.customview.CustomViewAttrVo;
import neatlogic.framework.cmdb.enums.RelDirectionType;
import neatlogic.framework.cmdb.utils.RelUtil;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class GetCiAttrRelApi extends PrivateApiComponentBase {


    @Override
    public String getName() {
        return "获取模型属性和关系列表";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "ciId", type = ApiParamType.LONG, desc = "模型id")
    })
    @Output({@Param(explode = CustomViewAttrVo[].class)})
    @Description(desc = "获取模型属性和关系列表接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        Long ciId = paramObj.getLong("ciId");
        IAttrCrossoverMapper attrCrossoverMapper = CrossoverServiceFactory.getApi(IAttrCrossoverMapper.class);
        IRelCrossoverMapper relCrossoverMapper = CrossoverServiceFactory.getApi(IRelCrossoverMapper.class);
        List<AttrVo> attrList = attrCrossoverMapper.getAttrByCiId(ciId);
        List<RelVo> relList = RelUtil.ClearRepeatRel(relCrossoverMapper.getRelByCiId(ciId));
        JSONArray returnList = new JSONArray();
        JSONObject commonObj = new JSONObject();
        commonObj.put("id", "uuid_" + ciId);
        commonObj.put("type", "uuid");
        commonObj.put("label", "唯一标识");
        commonObj.put("name", "uuid");
        returnList.add(commonObj);

        commonObj = new JSONObject();
        commonObj.put("id", "name_" + ciId);
        commonObj.put("type", "name");
        commonObj.put("label", "名称");
        commonObj.put("name", "name");

        commonObj = new JSONObject();
        commonObj.put("id", "lcd_" + ciId);
        commonObj.put("type", "lcd");
        commonObj.put("label", "最后更新时间");
        commonObj.put("name", "lcd");
        returnList.add(commonObj);
        for (AttrVo attrVo : attrList) {
            if (attrVo.getTargetCiId() == null) {
                JSONObject attrObj = new JSONObject();
                attrObj.put("id", "attr_" + attrVo.getId());
                attrObj.put("type", "attr");
                attrObj.put("label", attrVo.getLabel());
                attrObj.put("name", attrVo.getName());
                attrObj.put("attrId", attrVo.getId());
                returnList.add(attrObj);
            } else {
                List<AttrVo> targetAttrList = attrCrossoverMapper.getAttrByCiId(attrVo.getTargetCiId());
                commonObj = new JSONObject();
                commonObj.put("id", "uuid_" + attrVo.getId() + "." + attrVo.getTargetCiId());
                commonObj.put("type", "uuid");
                commonObj.put("label", attrVo.getLabel() + "->唯一标识");
                commonObj.put("name", "uuid");
                commonObj.put("attrId", attrVo.getId());
                commonObj.put("targetCiId", attrVo.getTargetCiId());
                returnList.add(commonObj);

                commonObj = new JSONObject();
                commonObj.put("id", "name_" + attrVo.getId() + "." + attrVo.getTargetCiId());
                commonObj.put("type", "name");
                commonObj.put("label", attrVo.getLabel() + "->名称");
                commonObj.put("name", "name");
                commonObj.put("attrId", attrVo.getId());
                commonObj.put("targetCiId", attrVo.getTargetCiId());
                returnList.add(commonObj);
                for (AttrVo subAttrVo : targetAttrList) {
                    //引用属性不再支持往下
                    if (subAttrVo.getTargetCiId() == null) {
                        JSONObject attrObj = new JSONObject();
                        attrObj.put("id", "attr_" + attrVo.getId() + "." + subAttrVo.getId());
                        attrObj.put("type", "attr");
                        attrObj.put("label", attrVo.getLabel() + "->" + subAttrVo.getLabel());
                        attrObj.put("name", attrVo.getName() + "->" + subAttrVo.getName());
                        attrObj.put("attrId", attrVo.getId());
                        attrObj.put("targetCiId", attrVo.getTargetCiId());
                        attrObj.put("targetAttrId", subAttrVo.getId());
                        returnList.add(attrObj);
                    }
                }
            }
        }
        for (RelVo relVo : relList) {
            JSONObject relObj = new JSONObject();
            relObj.put("id", "rel" + relVo.getDirection() + "_" + relVo.getId() + ".uuid");
            relObj.put("type", "uuid");
            relObj.put("label", "关系：" + (relVo.getDirection().equals(RelDirectionType.FROM.getValue()) ? relVo.getToLabel() : relVo.getFromLabel()) + "->唯一标识");
            relObj.put("name", "uuid");
            relObj.put("relId", relVo.getId());
            relObj.put("direction", relVo.getDirection());
            relObj.put("targetCiId", relVo.getDirection().equals(RelDirectionType.FROM.getValue()) ? relVo.getToCiId() : relVo.getFromCiId());
            returnList.add(relObj);

            relObj = new JSONObject();
            relObj.put("id", "rel" + relVo.getDirection() + "_" + relVo.getId() + ".name");
            relObj.put("type", "name");
            relObj.put("label", "关系：" + (relVo.getDirection().equals(RelDirectionType.FROM.getValue()) ? relVo.getToLabel() : relVo.getFromLabel()) + "->名称");
            relObj.put("name", "name");
            relObj.put("relId", relVo.getId());
            relObj.put("direction", relVo.getDirection());
            relObj.put("targetCiId", relVo.getDirection().equals(RelDirectionType.FROM.getValue()) ? relVo.getToCiId() : relVo.getFromCiId());
            returnList.add(relObj);

            List<AttrVo> targetAttrList = attrCrossoverMapper.getAttrByCiId(relVo.getDirection().equals(RelDirectionType.FROM.getValue()) ? relVo.getToCiId() : relVo.getFromCiId());
            for (AttrVo subAttrVo : targetAttrList) {
                //引用属性不再支持往下
                if (subAttrVo.getTargetCiId() == null) {
                    relObj = new JSONObject();
                    relObj.put("id", "rel" + relVo.getDirection() + "_" + relVo.getId() + "." + subAttrVo.getId());
                    relObj.put("type", "attr");
                    relObj.put("label", "关系：" + (relVo.getDirection().equals(RelDirectionType.FROM.getValue()) ? relVo.getToLabel() : relVo.getFromLabel()) + "->" + subAttrVo.getLabel());
                    relObj.put("name", relVo.getDirection().equals(RelDirectionType.FROM.getValue()) ? relVo.getToName() : relVo.getFromName() + "->" + subAttrVo.getName());
                    relObj.put("relId", relVo.getId());
                    relObj.put("direction", relVo.getDirection());
                    relObj.put("targetCiId", relVo.getDirection().equals(RelDirectionType.FROM.getValue()) ? relVo.getToCiId() : relVo.getFromCiId());
                    relObj.put("targetAttrId", subAttrVo.getId());
                    returnList.add(relObj);
                }
            }
        }
        return returnList;
    }

    @Override
    public String getToken() {
        return "/pbc/ci/attrrel/list";
    }
}

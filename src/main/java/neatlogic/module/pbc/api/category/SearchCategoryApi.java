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

package neatlogic.module.pbc.api.category;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.TableResultUtil;
import neatlogic.module.pbc.auth.label.PBC_INTERFACE_MODIFY;
import neatlogic.framework.pbc.dao.mapper.CategoryMapper;
import neatlogic.framework.pbc.dto.CategoryVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
@AuthAction(action = PBC_INTERFACE_MODIFY.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class SearchCategoryApi extends PrivateApiComponentBase {

    @Resource
    private CategoryMapper categoryMapper;

    @Override
    public String getToken() {
        return "/pbc/category/search";
    }

    @Override
    public String getName() {
        return "搜索分类标识";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "页数")
    })
    @Output({@Param(name = "tbodyList", explode = CategoryVo[].class), @Param(explode = BasePageVo.class)})
    @Description(desc = "搜索分类标识接口")
    @Override
    public Object myDoService(JSONObject paramObj) throws Exception {
        CategoryVo categoryVo = JSONObject.toJavaObject(paramObj, CategoryVo.class);
        int rowNum = categoryMapper.searchCategoryCount(categoryVo);
        categoryVo.setRowNum(rowNum);
        List<CategoryVo> categoryList = categoryMapper.searchCategory(categoryVo);
        return TableResultUtil.getResult(categoryList, categoryVo);
    }


}

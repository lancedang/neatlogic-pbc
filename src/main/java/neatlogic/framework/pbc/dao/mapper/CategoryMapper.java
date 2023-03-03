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

package neatlogic.framework.pbc.dao.mapper;

import neatlogic.framework.pbc.dto.CategoryVo;

import java.util.List;

public interface CategoryMapper {
    List<CategoryVo> getCategoryByInterfaceId(String interfaceId);

    CategoryVo getCategoryById(String id);

    List<CategoryVo> searchCategory(CategoryVo categoryVo);

    int searchCategoryCount(CategoryVo categoryVo);

    void replaceCategory(CategoryVo categoryVo);

    void updateCategory(CategoryVo categoryVo);

    void deleteCategoryById(Long id);
}

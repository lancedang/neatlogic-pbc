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

import neatlogic.framework.common.dto.ValueTextVo;
import neatlogic.framework.pbc.dto.EnumVo;
import neatlogic.framework.pbc.dto.PropertyRelVo;
import neatlogic.framework.pbc.dto.PropertyVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PropertyMapper {
    int checkPropertyRelIsExists(PropertyRelVo propertyRelVo);

    List<ValueTextVo> getPropertyValueRangeList();

    List<ValueTextVo> getPropertyDataTypeList();

    Integer getPropertyMaxSortByInterfaceId(String interfaceId);

    List<EnumVo> getEnumByPropertyId(String propertyId);

    List<ValueTextVo> getPropertyRestraintList();

    List<PropertyVo> getPropertyByInterfaceId(String interfaceId);

    List<PropertyVo> getPropertyByInterfaceComplexId(@Param("interfaceId") String interfaceId, @Param("complexId") String complexId);

    List<PropertyVo> searchProperty(PropertyVo propertyVo);

    List<String> getPropertyDataType(@Param("dataType") String dataType);

    List<String> getPropertyValueRange(@Param("valueRange") String valueRange);

    List<String> getPropertyRestraint(@Param("restraint") String restraint);


    PropertyVo getPropertyByUid(@Param("uid") Long uid);

    int checkPropertyIsExists(@Param("interfaceId") String interfaceId, @Param("propertyId") String propertyId, @Param("complexId") String complexId);

    int searchPropertyCount(PropertyVo propertyVo);

    void updateProperty(PropertyVo propertyVo);

    void updatePropertySort(PropertyVo propertyVo);

    void updatePropertyByUid(PropertyVo propertyVo);

    void insertProperty(PropertyVo propertyVo);

    void insertPropertyMapping(PropertyVo propertyVo);

    void deletePropertyMappingByInterfaceId(String interfaceId);

    void deletePropertyByUid(Long uid);

    void insertEnum(EnumVo enumVo);

    void insertPropertyRel(PropertyRelVo relVo);

    void updatePropertyRel(PropertyRelVo relVo);

    void deleteEnumByPropertyId(String propertyId);

    List<PropertyRelVo> getPropertyRelByFromPropertyUid(Long propertyUid);

    void deletePropertyRelByFromPropertyUid(Integer propertyUid);
}

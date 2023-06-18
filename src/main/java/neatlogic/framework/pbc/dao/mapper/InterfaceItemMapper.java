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

import neatlogic.framework.pbc.dto.InterfaceItemVo;
import neatlogic.framework.pbc.dto.InterfaceVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InterfaceItemMapper {

    List<Long> getNeedDeleteInterfaceItemIdByAuditId(Long auditId);

    List<InterfaceItemVo> getNeedReportInterfaceItemList(@Param("interfaceId") String interfaceId, @Param("corporationId") Long corporationId);

    List<InterfaceVo> getInterfaceItemByAuditId(Long policyAuditId);

    List<InterfaceItemVo> getInterfaceItemNotInAudit(Long auditId);

    InterfaceItemVo getInterfaceItemById(Long id);

    List<InterfaceItemVo> getInterfaceItemByIdList(@Param("idList") List<Long> idList);

    InterfaceItemVo getInterfaceItemByPrimaryKeyAndCorporationId(@Param("interfaceId") String interfaceId, @Param("primaryKey") String primaryKey, @Param("corporationId") Long corporationId);

    InterfaceItemVo getInterfaceItemByInterfaceIdAndCiEntityIdAndCorporationId(@Param("interfaceId") String interfaceId, @Param("ciEntityId") Long ciEntityId, @Param("corporationId") Long corporationId);


    List<InterfaceItemVo> searchInterfaceItem(InterfaceItemVo interfaceItemVo);

    void updateInterfaceItemError(InterfaceItemVo interfaceItemVo);

    int checkInterfaceHasAudit(String interfaceId);

    int checkCientityIfExist(@Param("ciEntityId") long ciEntityId);

    int getItemCountByCorporationId(@Param("corporationId") Long corporationId, @Param("interfaceId") String interfaceId);

    int getInterfaceItemCountByInterfaceId(String interfaceId);

    void updateInterfaceItemIsDeleteByInterfaceIdAndCustomViewId(@Param("interfaceId") String interfaceId, @Param("customViewId") Long customViewId);

    void updateInterfaceItemIsDeleteByInterfaceIdAndCiId(@Param("interfaceId") String interfaceId, @Param("ciId") Long ciId);

    void deleteInterfaceItemByInterfaceIdAndCiId(@Param("interfaceId") String interfaceId, @Param("ciId") Long ciId);

    void deleteInterfaceItemByInterfaceIdAndCustomViewId(@Param("interfaceId") String interfaceId, @Param("customViewId") Long customViewId);

    int searchInterfaceItemCount(InterfaceItemVo interfaceItemVo);

    void insertInterfaceItem(InterfaceItemVo interfaceItemVo);


    void updateInterfaceItem(InterfaceItemVo interfaceItemVo);

    void updateInterfaceItemDataHashByAuditId(Long auditId);

    void updateInterfaceItemDataHashById(Long interfaceItemId);

    void deleteInterfaceItemById(Long id);
}

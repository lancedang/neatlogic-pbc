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

import neatlogic.framework.pbc.dto.InterfaceCorporationVo;
import neatlogic.framework.pbc.dto.InterfaceVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InterfaceMapper {
    List<InterfaceCorporationVo> getInterfaceCorporationByInterfaceId(String interfaceId);

    InterfaceCorporationVo getInterfaceCorporationByInterfaceIdAndCorporationId(@Param("interfaceId") String interfaceId, @Param("corporationId") Long corporationId);

    List<InterfaceVo> getAllInterfaceList();

    List<InterfaceVo> getInterfaceByPolicyId(Long policyId);

    InterfaceVo getInterfaceById(String id);

    int checkInterfaceById(String id);

    int searchInterfaceCount(InterfaceVo interfaceVo);

    List<InterfaceVo> searchInterface(InterfaceVo interfaceVo);

    void updateInterface(InterfaceVo interfaceVo);

    void updateInterfaceStatus(InterfaceVo interfaceVo);

    void updateInterfaceMapping(InterfaceVo interfaceVo);

    void insertInterface(InterfaceVo interfaceVo);

    void insertCorporationRule(InterfaceCorporationVo interfaceCorporationVo);

    void deleteAllInterface();

    void deleteCorporationRuleByInterfaceId(String interfaceId);

    void deleteInterfaceById(String interfaceId);
}

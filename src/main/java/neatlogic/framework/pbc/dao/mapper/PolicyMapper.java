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

import neatlogic.framework.pbc.dto.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PolicyMapper {
    PolicyAuditVo getPolicyAuditById(Long auditId);

    List<PolicyAuditInterfaceItemStatistVo> getPolicyAuditInterfaceItemInfoByAuditId(Long auditId);

    List<PolicyPhaseVo> getPolicyPhaseByAuditId(Long policyAuditId);

    List<PolicyAuditVo> searchPolicyAudit(PolicyAuditVo policyAuditVo);

    int searchPolicyAuditCount(PolicyAuditVo policyAuditVo);

    PolicyPhaseVo getPolicyPhaseByAuditIdAndPhase(@Param("auditId") Long auditId, @Param("phase") String phase);

    List<PolicyAuditVo> getRunningPolicyAuditByServerId(Integer serverId);

    List<PolicyVo> getAllCronPolicy();

    List<PolicyVo> searchPolicy(PolicyVo policyVo);

    PolicyVo getPolicyById(Long id);

    int searchPolicyCount(PolicyVo policyVo);

    void insertAuditInterfaceItem(@Param("auditId") Long auditId, @Param("interfaceItemId") Long interfaceItemId, @Param("action") String action);

    void insertPolicyPhase(PolicyPhaseVo policyPhaseVo);

    void insertPolicyAudit(PolicyAuditVo policyAuditVo);

    void insertPolicy(PolicyVo policyVo);

    void insertPolicyInterface(PolicyInterfaceVo policyInterfaceVo);

    void updatePolicyPhase(PolicyPhaseVo policyPhaseVo);

    void updatePolicyAudit(PolicyAuditVo policyAuditVo);

    void updatePolicy(PolicyVo policyVo);

    void updatePolicyLastExecDate(Long policyId);

    void deletePolicyInterfaceByPolicyId(Long policyId);

    void deletePolicy(Long policyId);

    void deletePolicyAudit(Long policyAuditId);

    void deleteAuditByDayBefore(int dayBefore);

    void deletePolicyAuditInterfaceItemByAuditId(Long policyAuditId);
}

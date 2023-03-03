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

import neatlogic.framework.pbc.dto.InterfaceBranchVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Title: BranchMapper
 * @Package neatlogic.module.pbc.dao.mapper
 * @Description: TODO
 * @Author: yangy
 * @Date: 2021/7/20 17:43
 **/
public interface BranchMapper {
    InterfaceBranchVo getBranchById(Long id);

    InterfaceBranchVo getBranchByBranchId(@Param("branchId")String branchId);

    List<InterfaceBranchVo> searchBranch(InterfaceBranchVo interfaceBranchVo);

    int searchBranchCount(InterfaceBranchVo interfaceBranchVo);

    void insertInterfaceBranch(InterfaceBranchVo interfaceBranchVo);

    void insertBranchItem(@Param("branchId") Long branchId, @Param("itemId") Long itemId);

    void updateInterfaceBranchResult(@Param("id") Long id,@Param("result") String result);

    void updateInterfaceBranch(@Param("branchId") String branchId,@Param("status") String status,@Param("result") String result);

    String getBranchReportData(@Param("id") Long id);

    List<InterfaceBranchVo> getAllBranchListByStatus(@Param("status") String status);

    void updateInterfaceItemByBranchId(@Param("branchId") String branchId,@Param("status") String status);

    void deleteInterfaceBranch(@Param("id") Long id);
}

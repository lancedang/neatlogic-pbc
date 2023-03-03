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

package neatlogic.module.pbc.utils;

import neatlogic.framework.pbc.dao.mapper.CorporationMapper;
import neatlogic.framework.pbc.dto.CorporationConfigVo;
import neatlogic.framework.pbc.dto.CorporationVo;
import neatlogic.framework.pbc.exception.ConfigNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConfigManager {
    private static CorporationMapper configMapper;

    @Autowired
    public ConfigManager(CorporationMapper _configMapper) {
        configMapper = _configMapper;
    }

    public static CorporationConfigVo getConfig(Long id) {
        CorporationVo corporationVo = configMapper.getCorporationById(id);
        if (corporationVo != null) {
            return corporationVo.getCorporationConfigVo();
        } else {
            throw new ConfigNotFoundException();
        }
    }
}

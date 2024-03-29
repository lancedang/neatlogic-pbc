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

package neatlogic.framework.pbc.exception;

import neatlogic.framework.exception.core.ApiRuntimeException;

public class ReportResultLackParamException extends ApiRuntimeException {
    private static final long serialVersionUID = -8101125042160971435L;

    public ReportResultLackParamException(String param) {
        super("上报反馈结果缺少参数：{0}", param);
    }

}

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

package neatlogic.framework.pbc.dto;

import neatlogic.framework.common.dto.BasePageVo;
import neatlogic.framework.util.I18nUtils;
import neatlogic.framework.util.SnowflakeUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * @Title: InterfaceBranchVo
 * @Package neatlogic.module.pbc.dto
 * @Description: 人行上报批次实体类
 * @Author: yangy
 * @Date: 2021/7/20 11:25
 **/
public class InterfaceBranchVo extends BasePageVo {

    public enum Status {
        SUCCESS("success", "enum.pbc.status.success.a"),
        FAILED("failed", "enum.pbc.status.failed.a"),
        VALIDATING("validating", "enum.pbc.status.validating.b"),
        VALID("valid", "enum.pbc.status.valid.a"),
        INVALID("invalid", "enum.pbc.status.invalid"),
        HANDLER_SUCCESS("handler_success","enum.pbc.status.handler_success"),
        HANDLER_FAILED("handler_failed","enum.pbc.status.handler_failed"),
        PART_HANDLER_FAILED("part_handler_failed","enum.pbc.status.part_handler_failed"),
        HANDLER_DOING("handler_doing","common.doing");

        private final String type;
        private final String text;

        Status(String _type, String _text) {
            this.type = _type;
            this.text = _text;
        }

        public String getValue() {
            return type;
        }

        public String getText() {
            return I18nUtils.getMessage(text);
        }

        public static String getText(String name) {
            for (Status s : Status.values()) {
                if (s.getValue().equals(name)) {
                    return s.getText();
                }
            }
            return "";
        }
    }

    private Long id;
    private String interfaceId;
    private String branchId;
    private String startTime;
    private String endTime;
    private String status;
    private String statusText;
    private String result;
    private String reportItemData;

    public Long getId() {
        if (id == null) {
            id = SnowflakeUtil.uniqueLong();
        }
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusText() {
        if (StringUtils.isNotBlank(status)) {
            statusText = Status.getText(status);
        }
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getReportItemData() {
        return reportItemData;
    }

    public void setReportItemData(String reportItemData) {
        this.reportItemData = reportItemData;
    }
}

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

import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.EntityField;

import java.io.Serializable;

public class CorporationConfigVo implements Serializable {
    @EntityField(name = "认证地址", type = ApiParamType.STRING)
    private String loginUrl;
    @EntityField(name = "上报数据地址", type = ApiParamType.STRING)
    private String reportUrl;
    @EntityField(name = "入库申请地址", type = ApiParamType.STRING)
    private String importUrl;
    @EntityField(name = "查询数据处理状态地址", type = ApiParamType.STRING)
    private String selectDataUrl;
    @EntityField(name = "申请检核地址", type = ApiParamType.STRING)
    private String validUrl;
    @EntityField(name = "查询检核结果地址", type = ApiParamType.STRING)
    private String validResultUrl;
    @EntityField(name = "认证类型", type = ApiParamType.STRING)
    private String authType;
    @EntityField(name = "客户端ID", type = ApiParamType.STRING)
    private String clientId;
    @EntityField(name = "客户端密码", type = ApiParamType.STRING)
    private String clientSecret;
    @EntityField(name = "金融机构编码", type = ApiParamType.STRING)
    private String facilityOwnerAgency;
    @EntityField(name = "轮询次数", type = ApiParamType.INTEGER)
    private Integer pollCount;
    @EntityField(name = "轮询间隔", type = ApiParamType.INTEGER)
    private Integer pollInterval;

    public String getSelectDataUrl() {
        return selectDataUrl;
    }

    public void setSelectDataUrl(String selectDataUrl) {
        this.selectDataUrl = selectDataUrl;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    public String getImportUrl() {
        return importUrl;
    }

    public void setImportUrl(String importUrl) {
        this.importUrl = importUrl;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getFacilityOwnerAgency() {
        return facilityOwnerAgency;
    }

    public void setFacilityOwnerAgency(String facilityOwnerAgency) {
        this.facilityOwnerAgency = facilityOwnerAgency;
    }

    public String getValidUrl() {
        return validUrl;
    }

    public void setValidUrl(String validUrl) {
        this.validUrl = validUrl;
    }

    public String getValidResultUrl() {
        return validResultUrl;
    }

    public void setValidResultUrl(String validResultUrl) {
        this.validResultUrl = validResultUrl;
    }

    public Integer getPollCount() {
        return pollCount;
    }

    public void setPollCount(Integer pollCount) {
        this.pollCount = pollCount;
    }

    public Integer getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Integer pollInterval) {
        this.pollInterval = pollInterval;
    }
}

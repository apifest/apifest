/*
 * Copyright 2013-2014, ApiFest project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apifest.api;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;


/**
 * Represents an access token.
 *
 * @author Rossitsa Borissova
 */
@JsonPropertyOrder({ "access_token", "refresh_token", "token_type", "expires_in" })
@JsonSerialize(include = Inclusion.NON_EMPTY)
public class AccessToken implements Serializable {

    private static final long serialVersionUID = 4322523635887085378L;

    @JsonProperty("access_token")
    private String token = "";

    // not included when client_credentials
    @JsonProperty("refresh_token")
    private String refreshToken = "";

    @JsonProperty("expires_in")
    private String expiresIn = "";

    // bearer or mac
    @JsonProperty("token_type")
    private String type = "";

    @JsonProperty("scope")
    private String scope = "";

    @JsonIgnore
    private boolean valid;

    @JsonIgnore
    private String clientId = "";

    @JsonIgnore
    private String codeId = "";

    @JsonIgnore
    private String userId = "";

    @JsonIgnore
    private Map<String, String> applicationDetails = null;

    @JsonIgnore
    private Map<String, String> details = null;

    @JsonIgnore
    private Long created;

    @JsonProperty("refresh_expires_in")
    private String refreshExpiresIn = "";

    public AccessToken(String token, String tokenType, String expiresIn, String scope, String refreshToken, String refreshExpiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.type = tokenType;
        this.scope = scope;
        this.valid = true;
        this.created = (new Date()).getTime();
        this.refreshToken = refreshToken;
        this.refreshExpiresIn = (refreshExpiresIn != null && !refreshExpiresIn.isEmpty()) ? refreshExpiresIn : expiresIn;
    }

    public AccessToken() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String accessToken) {
        this.token = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(String expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getType() {
        return type;
    }

    public void setType(String tokenType) {
        this.type = tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getCodeId() {
        return codeId;
    }

    public void setCodeId(String codeId) {
        this.codeId = codeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, String> getDetails() {
        return details;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public Map<String, String> getApplicationDetails() {
        return applicationDetails;
    }

    public void setApplicationDetails(Map<String, String> applicationDetails) {
        this.applicationDetails = applicationDetails;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public void setRefreshExpiresIn(String refreshExpiresIn) {
        this.refreshExpiresIn = refreshExpiresIn;
    }

    public boolean tokenExpired() {
        // expires_in is in seconds
        Long expiresInSec = Long.valueOf(getExpiresIn()) * 1000;
        Long currentTime = System.currentTimeMillis();
        if (expiresInSec + getCreated() < currentTime) {
            return true;
        }
        return false;
    }

    public boolean refreshTokenExpired() {
        Long refreshExpiresInSec = Long.valueOf(getRefreshExpiresIn()) * 1000;
        Long currentTime = System.currentTimeMillis();
        if (refreshExpiresInSec + getCreated() < currentTime) {
            return true;
        }
        return false;
    }

}

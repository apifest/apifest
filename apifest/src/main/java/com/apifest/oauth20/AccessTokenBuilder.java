/*
 * Copyright 2013-2018, ApiFest project
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
package com.apifest.oauth20;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.apifest.api.AccessToken;

public class AccessTokenBuilder {

    /**
     * Creates access token along with its refresh token.
     *
     * @param tokenType
     * @param expiresIn
     * @param scope
     */
    public static AccessToken createAccessToken(String tokenType, String expiresIn, String scope, String refreshExpiresIn) {
        return AccessTokenBuilder.createAccessToken(tokenType, expiresIn, scope, true, refreshExpiresIn);
    }

    /**
     * Creates access token. Used for generation of client_credentials type
     * tokens with no refreshToken.
     *
     * @param tokenType
     * @param expiresIn
     * @param scope
     * @param createRefreshToken
     */
    public static AccessToken createAccessToken(String tokenType, String expiresIn, String scope,
            boolean createRefreshToken, String refreshExpiresIn) {
        AccessToken token = new AccessToken();
        token.setToken(RandomGenerator.generateRandomString());
        if (createRefreshToken) {
            token.setRefreshToken(RandomGenerator.generateRandomString());
            token.setRefreshExpiresIn(
                    (refreshExpiresIn != null && !refreshExpiresIn.isEmpty()) ? refreshExpiresIn : expiresIn);
        }
        token.setExpiresIn(expiresIn);
        token.setType(tokenType);
        token.setScope(scope);
        token.setValid(true);
        token.setCreated((new Date()).getTime());
        return token;
    }

    /**
     * Creates access token with already generated refresh token.
     *
     * @param tokenType
     * @param expiresIn
     * @param scope
     * @param createRefreshToken
     * @param refreshToken
     */
    public static AccessToken createAccessToken(String tokenType, String expiresIn, String scope, String refreshToken,
            String refreshExpiresIn) {
        AccessToken token = new AccessToken();
        token.setToken(RandomGenerator.generateRandomString());
        token.setExpiresIn(expiresIn);
        token.setType(tokenType);
        token.setScope(scope);
        token.setValid(true);
        token.setCreated((new Date()).getTime());
        token.setRefreshToken(refreshToken);
        token.setRefreshExpiresIn(
                (refreshExpiresIn != null && !refreshExpiresIn.isEmpty()) ? refreshExpiresIn : expiresIn);
        return token;
    }

    public static AccessToken loadFromMap(Map<String, Object> map) {
        AccessToken accessToken = new AccessToken();
        accessToken.setToken((String) map.get("token"));
        accessToken.setRefreshToken((String) map.get("refreshToken"));
        accessToken.setExpiresIn((String) map.get("expiresIn"));
        accessToken.setType((String) map.get("type"));
        accessToken.setScope((String) map.get("scope"));
        accessToken.setValid((Boolean) map.get("valid"));
        accessToken.setClientId((String) map.get("clientId"));
        accessToken.setCodeId((String) map.get("codeId"));
        accessToken.setUserId((String) map.get("userId"));
        accessToken.setCreated((Long) map.get("created"));
        accessToken.setDetails(JsonUtils.convertStringToMap((String) map.get("details")));
        accessToken.setApplicationDetails(JsonUtils.convertStringToMap((String) map.get("applicationDetails")));
        accessToken.setRefreshExpiresIn((String) ((map.get("refreshExpiresIn") != null ? map.get("refreshExpiresIn")
                : accessToken.getExpiresIn())));
        return accessToken;
    }

    public static AccessToken loadFromStringMap(Map<String, String> map) {
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(map.get("token"));
        accessToken.setRefreshToken(map.get("refreshToken"));
        accessToken.setExpiresIn(map.get("expiresIn"));
        accessToken.setType(map.get("type"));
        accessToken.setScope(map.get("scope"));
        accessToken.setValid(Boolean.parseBoolean(map.get("valid")));
        accessToken.setClientId(map.get("clientId"));
        accessToken.setCodeId(map.get("codeId"));
        accessToken.setUserId(map.get("userId"));
        accessToken.setCreated(Long.parseLong(map.get("created")));
        accessToken.setDetails(JsonUtils.convertStringToMap(map.get("details")));
        accessToken.setApplicationDetails(JsonUtils.convertStringToMap(map.get("applicationDetails")));
        accessToken.setRefreshExpiresIn(
                map.get("refreshExpiresIn") != null ? map.get("refreshExpiresIn") : accessToken.getExpiresIn());
        return accessToken;
    }

    public static AccessToken loadFromStringList(List<String> list) {
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(list.get(0));
        accessToken.setRefreshToken(list.get(1));
        ;
        accessToken.setExpiresIn(list.get(2));
        accessToken.setType(list.get(3));
        accessToken.setScope(list.get(4));
        accessToken.setValid(Boolean.parseBoolean(list.get(5)));
        accessToken.setClientId(list.get(6));
        accessToken.setCodeId(list.get(7));
        accessToken.setUserId(list.get(8));
        accessToken.setCreated(Long.parseLong(list.get(9)));
        accessToken.setDetails(JsonUtils.convertStringToMap(list.get(10)));
        accessToken.setRefreshExpiresIn(list.get(11) != null ? list.get(11) : accessToken.getExpiresIn());
        accessToken.setApplicationDetails(JsonUtils.convertStringToMap(list.get(12)));
        return accessToken;
    }

}

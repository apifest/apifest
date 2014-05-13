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

package com.apifest;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts access token and validates it against endpoint scope.
 *
 * @author Rossitsa Borissova
 */
public class AccessTokenValidator {

    private static final Pattern AUTH_BEARER_PATTERN = Pattern.compile("(Bearer )(\\w*)");

    protected static Logger log = LoggerFactory.getLogger(AccessTokenValidator.class);

    protected static boolean validateTokenScope(String tokenContent, String endpointScope) {
        String tokenScope = extractTokenScope(tokenContent);
        if (tokenScope == null) {
            return false;
        }
        // tokenScope should be always not null
        List<String> allowedScopes = Arrays.asList(endpointScope.split(" "));
        String [] scopes = tokenScope.split(" ");
        for (String scope : scopes) {
            if (allowedScopes.contains(scope)) {
                return true;
            }
        }
        return false;
    }

    protected static String extractTokenScope(String tokenContent) {
        String scope = null;
        try {
            JSONObject object = new JSONObject(tokenContent);
            Object rs = object.get("scope");
            if (rs != null && !rs.toString().equals("null")) {
                scope = (String) rs;
            }
        } catch (JSONException e) {
            log.error("Cannot extract scope from content {}", tokenContent);
        }
        return scope;
    }

    protected static String extractAccessToken(String header) {
        Matcher m = AUTH_BEARER_PATTERN.matcher(header);
        if (m.find()) {
            return m.group(2);
        }
        return null;
    }

}

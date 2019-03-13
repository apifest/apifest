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

package com.apifest.oauth20;

import java.util.List;
import java.util.Map;

/**
 * Utility class for extracting query parameters.
 *
 * @author Rossitsa Borissova
 */
public final class QueryParameter {

    public static final String TOKEN = "token";
    public static final String CLIENT_ID = "client_id";
    public static final String USER_ID = "user_id";

    public static String getFirstElement(Map<String, List<String>> map, String key) {
        String value = null;
        if (map.get(key) != null) {
            value = map.get(key).get(0);
        }
        return value;
    }
}

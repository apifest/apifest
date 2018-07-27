/*
 * Copyright 2014, ApiFest project
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

import io.netty.handler.codec.http.HttpRequest;

/**
 * Interface for custom grant_type handler.
 *
 * @author Rossitsa Borissova
 */
public interface ICustomGrantTypeHandler {

    /**
     * Executes what is required for that grant_type and returns user details.
     * @param request issue token request
     * @return user details that will be associated with the access token
     */
    UserDetails execute(final HttpRequest request) throws AuthenticationException;

}

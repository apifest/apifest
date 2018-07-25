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

import com.apifest.api.AccessToken;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


/**
 * Parent class for all mapping actions. Actions are executed before a request from the client application is sent to
 * the backend. An action can manipulate the request body or the requested uri if that is denoted in the mapping. For
 * instance, one action may add customerId in request body that is expected to be in JSON format. Or an action can
 * replace some values (denoted in curly brackets) in the internal/backend uri, e.g. in internal uri
 * /customer/{customerId}, {customerId} could be replaced with an appropriate customer id value.
 *
 * @author Rossitsa Borissova
 *
 */
public abstract class BasicAction {


    /**
     * Maps the request to the internal URI passed as a parameter. Modifies the request body/headers, if necessary.
     * @param req request received from client application
     * @param internalURI the internal URI to which the request should be mapped
     * @param validToken access token validation response
     * @return the modified request
     * @throws MappingException if something goes wrong with request mapping
     * @throws UpstreamException if the upstream should be stopped and a response should be returned directly
     */
    public HttpRequest execute(HttpRequest req, String internalURI, AccessToken validToken)
            throws MappingException, UpstreamException {
        return req;
    }

    /**
     * Maps the request to the internal URI passed as a parameter. Modifies the request body/headers, if necessary.
     * @param req request received from client application
     * @param validToken access token validation response
     * @param mappingEndpoint configuration for this endpoint from the mapping xml
     * @return the modified request
     * @throws MappingException if something goes wrong with request mapping
     * @throws UpstreamException if the upstream should be stopped and a response should be returned directly
     */
    public HttpRequest execute(HttpRequest req, AccessToken validToken, MappingEndpoint mappingEndpoint)
            throws MappingException, UpstreamException {
        return execute(req, req.getUri(), validToken);
    }
}

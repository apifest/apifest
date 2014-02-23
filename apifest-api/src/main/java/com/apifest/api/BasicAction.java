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

import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * Parent class for all mapping actions.
 * Actions are executed before a request from the client application is sent to
 * the backend. An action can manipulate the request body or the requested uri if that is denoted in the mapping.
 * For instance, one action may add customerId in request body that is expected to be in JSON format.
 * Or an action can replace some values (denoted in curly brackets) in the internal/backend uri, e.g.
 * in internal uri /customer/{customerId}, {customerId} could be replaced with an appropriate customer id value.
 * @author Rossitsa Borissova
 *
 */
public abstract class BasicAction {

    public abstract HttpRequest execute(HttpRequest req, String internalURI, String userId);
}

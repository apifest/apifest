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

package com.apifest.example;

import org.jboss.netty.handler.codec.http.HttpRequest;

import com.apifest.BasicAction;

/**
 * Action that replaces {customerId} with actual customer id value.
 *
 * @author Rossitsa Borissova
 */
public class ReplaceCustomerIdAction extends BasicAction {

    protected static final String CUSTOMER_ID = "{customerId}";

    @Override
    public HttpRequest execute (HttpRequest req, String internalURI, String userId) {
        String newURI = internalURI.replace(CUSTOMER_ID, userId);
        req.setUri(newURI);
        return req;
    }

}

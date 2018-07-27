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

import com.apifest.api.AccessToken;

import com.apifest.api.BasicAction;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Action that replaces {customerId} with actual customer id value.
 *
 * @author Rossitsa Borissova
 */
public class ReplaceCustomerIdAction extends BasicAction {

    protected static final String CUSTOMER_ID = "{customerId}";

    @Override
    public FullHttpRequest execute(FullHttpRequest req, String internalURI, AccessToken validToken) {
        String newURI = internalURI.replace(CUSTOMER_ID, validToken.getUserId());
        req.setUri(newURI);
        return req;
    }

}

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

import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;
import static org.mockito.Mockito.*;
import org.testng.*;
import org.mockito.*;

import com.apifest.example.ReplaceCustomerIdAction;

/**
 * @author Rossitsa Borissova
 */
public class ReplaceCustomerIdActionTest {

    ReplaceCustomerIdAction action;
    HttpRequest req;

    @BeforeMethod
    public void setup() {
        action = spy(new ReplaceCustomerIdAction());
        req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/customer/{customerId}");
    }

    @Test
    public void when_execute_replace_customerId() {
        // GIVEN

        // WHEN
        HttpRequest request = action.execute(req, "/customer/" + ReplaceCustomerIdAction.CUSTOMER_ID, "1223");

        // THEN
        assertEquals(request.getUri(), "/customer/1223");
    }
}

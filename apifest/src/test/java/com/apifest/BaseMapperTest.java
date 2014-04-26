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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Rossitsa Borissova
 */
public class BaseMapperTest {

    BaseMapper mapper;

    @BeforeMethod
    public void setup() {
        mapper = spy(new BaseMapper());
        mapper.log = mock(Logger.class);
    }

    @Test
    public void when_request_map_it_to_internalUri() throws Exception {
        // GIVEN
        String internalUri = "http://api.example.com/customer/1234";
        HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "http://api.example.com/me");

        // WHEN
        HttpRequest res = mapper.map(req, internalUri);

        // THEN
        assertEquals(res.getUri(), internalUri);
    }

    @Test
    public void when_query_params_add_it_to_internal_uri() throws Exception {
        // GIVEN
        String uri = "/validation/mobile?mobile=89343430&code=359";
        String newUri = "/user/validation/mobile";

        // WHEN
        String resultUri = mapper.constructNewUri(uri, newUri);

        // THEN
        assertEquals(resultUri, "/user/validation/mobile?mobile=89343430&code=359");
    }

    @Test
    public void when_map_invoke_constructUri() throws Exception {
        String internalUri = "http://api.example.com/customer/1234";
        HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "http://api.example.com/me");

        // WHEN
        HttpRequest res = mapper.map(req, internalUri);

        // THEN
        verify(mapper).constructNewUri("http://api.example.com/me", internalUri);
    }
}

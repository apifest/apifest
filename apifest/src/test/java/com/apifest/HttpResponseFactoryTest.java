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

import static org.testng.Assert.*;

import org.apache.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

/**
 * @author Rossitsa Borissova
 */
public class HttpResponseFactoryTest {

    @Test
    public void when_ise_response_set_header_and_body() throws Exception {
        // WHEN
        HttpResponse response = HttpResponseFactory.createISEResponse();

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
        assertEquals(response.headers().get(HttpHeaders.CONTENT_TYPE), HttpResponseFactory.APPLICATION_JSON);
        String error = new String(response.getContent().array());
        assertEquals(error, "");
    }

    @Test
    public void when_unauthorized_response_set_header_and_body() throws Exception {
        // WHEN
        String errorMsg = "no access token";
        HttpResponse response = HttpResponseFactory.createUnauthorizedResponse(errorMsg);

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.UNAUTHORIZED);
        assertEquals(response.headers().get(HttpHeaders.CONTENT_TYPE), HttpResponseFactory.APPLICATION_JSON);
        String error = new String(response.getContent().array());
        assertEquals(error, errorMsg);
    }

    @Test
    public void when_create_not_found_response_set_header_and_body() throws Exception {
        // WHEN
        HttpResponse response = HttpResponseFactory.createNotFoundResponse();

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.NOT_FOUND);
        assertEquals(response.headers().get(HttpHeaders.CONTENT_TYPE), HttpResponseFactory.APPLICATION_JSON);
        String error = new String(response.getContent().array());
        assertEquals(error, HttpResponseFactory.NOT_FOUND_CONTENT);
    }

    @Test
    public void when_OK_response_set_header_and_body() throws Exception {
        // WHEN
        String message = "OK message";
        HttpResponse response = HttpResponseFactory.createOKResponse(message);

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.OK);
        assertEquals(response.headers().get(HttpHeaders.CONTENT_TYPE), HttpResponseFactory.APPLICATION_JSON);
        String responseMsg = new String(response.getContent().array());
        assertEquals(responseMsg, message);
    }

}

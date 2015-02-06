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


import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;

/**
 * Creates HTTP response with appropriate HTTP status and message.
 *
 * @author Rossitsa Borissova
 */
public class HttpResponseFactory {

    public static final String NOT_FOUND_CONTENT = "{\"error\":\"Not found\"}";
    public static final String APPLICATION_JSON = "application/json";

    /**
     * Creates HTTP response with HTTP status 500.
     *
     * @return HTTP response created
     */
    public static HttpResponse createISEResponse() {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        return response;
    }

    /**
     * Creates HTTP response with HTTP status 401 and the message passed.
     *
     * @param message
     *            message returned in the response
     * @return HTTP response created
     */
    public static HttpResponse createUnauthorizedResponse(String message) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(message.getBytes(CharsetUtil.UTF_8));
        response.setContent(buf);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.array().length);
        return response;
    }

    /**
     * Creates HTTP response with HTTP status 404.
     *
     * @return HTTP response created
     */
    public static HttpResponse createNotFoundResponse() {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(NOT_FOUND_CONTENT.getBytes(CharsetUtil.UTF_8));
        response.setContent(buf);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.array().length);
        return response;
    }

    /**
     * Creates HTTP response with HTTP status 200 and the message passed.
     *
     * @param message message returned in the response
     * @return HTTP response created
     */
    public static HttpResponse createOKResponse(String message) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(message.getBytes(CharsetUtil.UTF_8));
        response.setContent(buf);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.array().length);
        return response;
    }

    /**
     * Create HTTP response with a given HTTP status and message.
     *
     * @param httpStatus HTTP response status
     * @param message response message
     * @return HTTP response created
     */
    public static HttpResponse createResponse(HttpResponseStatus httpStatus, String message) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, httpStatus);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(message.getBytes(CharsetUtil.UTF_8));
        response.setContent(buf);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.array().length);
        return response;
    }
}

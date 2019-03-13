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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;

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
    public static FullHttpResponse createISEResponse() {

        String errorMessage = ConfigLoader.getLoadedGlobalErrors().get(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
        if (errorMessage == null) {
            errorMessage = "";
        }
        ByteBuf buf = Unpooled.copiedBuffer(errorMessage.getBytes(CharsetUtil.UTF_8));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR, buf);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.array().length);
        return response;
    }

    /**
     * Creates HTTP response with HTTP status 401 and the message passed.
     *
     * @param message message returned in the response
     * @return HTTP response created
     */
    public static FullHttpResponse createUnauthorizedResponse(String message) {
        String errorMessage = ConfigLoader.getLoadedGlobalErrors().get(HttpResponseStatus.UNAUTHORIZED.code());
        if (errorMessage == null) {
            errorMessage = message;
        }
        ByteBuf buf = Unpooled.copiedBuffer(errorMessage.getBytes(CharsetUtil.UTF_8));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.UNAUTHORIZED, buf);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.array().length);
        return response;
    }

    /**
     * Creates HTTP response with HTTP status 404.
     *
     * @return HTTP response created
     */
    public static FullHttpResponse createNotFoundResponse() {

        String errorMessage = ConfigLoader.getLoadedGlobalErrors().get(HttpResponseStatus.NOT_FOUND.code());
        if (errorMessage == null) {
            errorMessage = HttpResponseFactory.NOT_FOUND_CONTENT;
        }
        ByteBuf buf = Unpooled.copiedBuffer(errorMessage.getBytes(CharsetUtil.UTF_8));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND, buf);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.array().length);
        return response;
    }

    /**
     * Creates HTTP response with HTTP status 200 and the message passed.
     *
     * @param message message returned in the response
     * @return HTTP response created
     */
    public static FullHttpResponse createOKResponse(String message) {
        ByteBuf buf = Unpooled.copiedBuffer(message.getBytes(CharsetUtil.UTF_8));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.array().length);
        return response;
    }

    /**
     * Create HTTP response with a given HTTP status and message.
     *
     * @param httpStatus HTTP response status
     * @param message    response message
     * @return HTTP response created
     */
    public static FullHttpResponse createResponse(HttpResponseStatus httpStatus, String message) {
        ByteBuf buf = Unpooled.copiedBuffer(message.getBytes(CharsetUtil.UTF_8));
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, httpStatus, buf);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, APPLICATION_JSON);
        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, buf.array().length);
        return response;
    }
}

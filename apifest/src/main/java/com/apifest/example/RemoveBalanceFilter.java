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

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.api.BasicFilter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Filter that removes balance field from response.
 *
 * @author Rossitsa Borissova
 */
public class RemoveBalanceFilter extends BasicFilter {

    public static Logger log = LoggerFactory.getLogger(RemoveBalanceFilter.class);

    @Override
    public HttpResponse execute(FullHttpResponse response) {
        JsonParser parser = new JsonParser();
        JsonObject json= parser.parse(response.content().toString(CharsetUtil.UTF_8)).getAsJsonObject();
        log.info("response body: " + json.toString());
        json.remove("balance");
        log.info("modified response body: " + json.toString());
        byte[] newContent = json.toString().getBytes(CharsetUtil.UTF_8);
        response.replace(Unpooled.copiedBuffer(newContent));
        HttpHeaders.setContentLength(response, newContent.length);
        return response;
    }

}

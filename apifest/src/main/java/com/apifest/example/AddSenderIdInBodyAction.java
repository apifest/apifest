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
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.api.BasicAction;
import com.apifest.api.MappingException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Action that adds senderId in request body.
 *
 * @author Rossitsa Borissova
 */
public class AddSenderIdInBodyAction extends BasicAction {

    public static Logger log = LoggerFactory.getLogger(AddSenderIdInBodyAction.class);

    /*
     * @see com.apifest.api.BasicAction#execute(org.jboss.netty.handler.codec.http.HttpRequest, java.lang.String, org.jboss.netty.handler.codec.http.HttpResponse)
     */
    @Override
    public HttpRequest execute(HttpRequest req, String internalURI, AccessToken tokenValidationResponse) throws MappingException {
        JsonParser parser = new JsonParser();
        JsonObject json= parser.parse(new String(req.getContent().toString(CharsetUtil.UTF_8))).getAsJsonObject();
        log.info("request body: " + json);
        json.addProperty("senderId", "1232");
        byte[] newContent = json.toString().getBytes(CharsetUtil.UTF_8);
        ChannelBuffer buf = ChannelBuffers.copiedBuffer(newContent);
        req.setContent(buf);
        HttpHeaders.setContentLength(req, newContent.length);
        return req;
    }

}

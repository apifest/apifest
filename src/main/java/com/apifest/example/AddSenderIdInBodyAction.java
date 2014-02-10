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

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.BasicAction;

/**
 * Action that adds senderId in request body.
 *
 * @author Rossitsa Borissova
 */
public class AddSenderIdInBodyAction extends BasicAction {

    public static Logger log = LoggerFactory.getLogger(AddSenderIdInBodyAction.class);

    @Override
    public HttpRequest execute(HttpRequest req, String internalURI, String userId) {
        try {
            JSONObject json = new JSONObject(req.getContent().toString(CharsetUtil.UTF_8));
            log.info("request body: " + json.toString());
            json.put("senderId", "1232");
            byte [] newContent = json.toString().getBytes();
            ChannelBuffer buf = ChannelBuffers.copiedBuffer(newContent);
            req.setContent(buf);
            HttpHeaders.setContentLength(req, newContent.length);
        } catch (JSONException e) {
            log.info(e.getMessage());
        }
        return req;
    }
}

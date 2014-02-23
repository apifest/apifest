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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apifest.example.AddSenderIdInBodyAction;

/**
 * @author Rossitsa Borissova
 */
public class AddSenderIdInBodyActionTest {

    AddSenderIdInBodyAction action;
    HttpRequest req;

    @BeforeMethod
    public void setup() {
        action = spy(new AddSenderIdInBodyAction());
        req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/payments");
        req.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        String content = "{}";
        req.setContent(ChannelBuffers.copiedBuffer(content.getBytes()));
    }

    @Test
    public void when_execute_add_senderId_in_request_body() {
        // WHEN
        HttpRequest request = action.execute(req, "/payments", "1232");

        // THEN
        JSONObject json = null;
        try {
            json = new JSONObject(request.getContent().toString(CharsetUtil.UTF_8));
            assertEquals(json.get("senderId"), "1232");
        } catch (JSONException e) {
            assertEquals(true, false);
            e.printStackTrace();
        }
    }

}

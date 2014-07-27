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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.hazelcast.core.IMap;

/**
 * @author Rossitsa Borissova
 */
public class HttpResponseHandlerTest {

    HttpResponse response = null;
    HttpResponseHandler handler = null;
    ChannelHandlerContext ctx;
    MessageEvent e;

    @BeforeMethod
    public void setup() {
        handler = spy(new HttpResponseHandler());
        handler.log = mock(Logger.class);
        ctx = mock(ChannelHandlerContext.class);
        e = mock(MessageEvent.class);

        Channel channel = mock(Channel.class);
        doReturn(channel).when(ctx).getChannel();
        doReturn(null).when(channel).close();
        ResponseListener listener = mock(ResponseListener.class);
        doReturn(listener).when(ctx).getAttachment();
        doNothing().when(listener).responseReceived(response);

        String path = getClass().getClassLoader().getResource("test_mapping.xml").getPath();
        ServerConfig.mappingsPath = path.replace("/test_mapping.xml", "");

        HazelcastConfigInstance.configInstance = mock(HazelcastConfigInstance.class);
        IMap<String, com.apifest.MappingConfig> map = mock(IMap.class);
        doReturn(map).when(HazelcastConfigInstance.configInstance).getMappingConfigs();
    }

    @Test
    public void when_status_eror_not_mapped_return_backend_response() throws Exception {
        // GIVEN
        response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
        doReturn(response).when(e).getMessage();
        MappingConfigLoader.load(false);

        // WHEN
        handler.messageReceived(ctx, e);

        // THEN
        assertEquals(new String(response.getContent().array()), "");
    }

    @Test
    public void when_status_error_404_mapped_return_customized_response() throws Exception {
        // GIVEN
        response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
        doReturn(response).when(e).getMessage();

        MappingConfigLoader.load(false);

        // WHEN
        handler.messageReceived(ctx, e);

        // THEN
        assertEquals(new String(response.getContent().array()), "{\"error\":\"resource not found\"}");
    }

    @Test
    public void when_status_error_500_mapped_return_customized_response() throws Exception {
        // GIVEN
        response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        doReturn(response).when(e).getMessage();

        MappingConfigLoader.load(false);

        // WHEN
        handler.messageReceived(ctx, e);

        // THEN
        assertEquals(new String(response.getContent().array()), "{\"error\":\"ops...something wrong\"}");
    }

    @Test
    public void when_status_error_300_mapped_return_customized_response() throws Exception {
        // GIVEN
        response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.MULTIPLE_CHOICES);
        doReturn(response).when(e).getMessage();

        MappingConfigLoader.load(false);

        // WHEN
        handler.messageReceived(ctx, e);

        // THEN
        assertEquals(new String(response.getContent().array()), "{\"error\":\"further actions required\"}");
    }

    @Test
    public void when_status_201_mapped_do_not_return_customized_response() throws Exception {
        // GIVEN
        response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CREATED);
        doReturn(response).when(e).getMessage();

        MappingConfigLoader.load(false);

        // WHEN
        handler.messageReceived(ctx, e);

        // THEN
        assertNotEquals(new String(response.getContent().array()), "{\"message\":\"resource created\"}");
    }
}

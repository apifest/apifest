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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelConfig;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author Rossitsa Borissova
 */
public class HttpRequestHandlerTest {

    HttpRequestHandler handler;

    @BeforeTest
    public void setup() {
        handler = spy(new HttpRequestHandler());

        HttpRequestHandler.log = mock(Logger.class);
        BaseMapper.log = mock(Logger.class);
    }

    @Test
    public void when_uri_is_apifest_reload_invoke_reload_mapping_configs() throws Exception {
        // GIVEN
        MessageEvent message = mock(MessageEvent.class);
        HttpRequest req = mock(HttpRequest.class);
        doReturn(req).when(message).getMessage();
        Channel channel = mock(Channel.class);
        ChannelConfig channelConfig = mock(ChannelConfig.class);
        ServerConfig.setDefaultConfigs();
        when(channel.getConfig()).thenReturn(channelConfig);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.getChannel()).thenReturn(channel);
        doReturn(ChannelBuffers.EMPTY_BUFFER).when(req).getContent();
        doReturn(HttpRequestHandler.RELOAD_URI).when(req).getUri();
        doReturn(HttpMethod.GET).when(req).getMethod();
        doNothing().when(handler).reloadMappingConfig(any(Channel.class));

        // WHEN
        handler.messageReceived(ctx, message);

        // THEN
        verify(handler).reloadMappingConfig(any(Channel.class));
    }

    @Test
    public void when_apifest_mappings_invoke_getLoadedMappings() throws Exception {
        // GIVEN
        MessageEvent message = mock(MessageEvent.class);
        HttpRequest req = mock(HttpRequest.class);
        doReturn(req).when(message).getMessage();
        Channel channel = mock(Channel.class);
        ChannelConfig channelConfig = mock(ChannelConfig.class);
        ServerConfig.setDefaultConfigs();
        when(channel.getConfig()).thenReturn(channelConfig);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.getChannel()).thenReturn(channel);
        doReturn(ChannelBuffers.EMPTY_BUFFER).when(req).getContent();
        doReturn(HttpRequestHandler.MAPPINGS_URI).when(req).getUri();
        doReturn(HttpMethod.GET).when(req).getMethod();
        doNothing().when(handler).getLoadedMappings(any(Channel.class));

        // WHEN
        handler.messageReceived(ctx, message);

        // THEN
        verify(handler).getLoadedMappings(any(Channel.class));
    }

    @Test
    public void when_apifest_global_errors_invoke_getLoadedGlobalErrors() throws Exception {
        // GIVEN
        MessageEvent message = mock(MessageEvent.class);
        HttpRequest req = mock(HttpRequest.class);
        Channel channel = mock(Channel.class);
        doReturn(req).when(message).getMessage();
        ChannelConfig channelConfig = mock(ChannelConfig.class);
        ServerConfig.setDefaultConfigs();
        when(channel.getConfig()).thenReturn(channelConfig);
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        when(ctx.getChannel()).thenReturn(channel);
        doReturn(ChannelBuffers.EMPTY_BUFFER).when(req).getContent();
        doReturn(HttpRequestHandler.GLOBAL_ERRORS_URI).when(req).getUri();
        doReturn(HttpMethod.GET).when(req).getMethod();
        doNothing().when(handler).getLoadedGlobalErrors(any(Channel.class));

        // WHEN
        handler.messageReceived(ctx, message);

        // THEN
        verify(handler).getLoadedGlobalErrors(any(Channel.class));
    }
}

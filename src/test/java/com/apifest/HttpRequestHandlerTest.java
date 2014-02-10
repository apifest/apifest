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

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelConfig;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.testng.annotations.Test;

/**
 * @author Rossitsa Borissova
 */
public class HttpRequestHandlerTest {

    @Test
    public void when_uri_is_apifest_reload_invoke_reload_mapping_configs() throws Exception {
        // GIVEN
        HttpRequestHandler handler = spy(new HttpRequestHandler());
        handler.log = mock(Logger.class);
        MessageEvent message = mock(MessageEvent.class);
        HttpRequest req = mock(HttpRequest.class);
        doReturn(req).when(message).getMessage();
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        doReturn(ChannelBuffers.EMPTY_BUFFER).when(req).getContent();
        doReturn(HttpRequestHandler.RELOAD_URI).when(req).getUri();
        doReturn(HttpMethod.GET).when(req).getMethod();
        doNothing().when(handler).reloadMappingConfig(any(Channel.class));
        doNothing().when(handler).setConnectTimeout(any(Channel.class));

        // WHEN
        handler.messageReceived(ctx, message);

        // THEN
        verify(handler).reloadMappingConfig(any(Channel.class));
    }

}

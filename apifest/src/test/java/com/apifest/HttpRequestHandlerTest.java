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

import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.apifest.api.MappingAction;
import com.apifest.api.MappingEndpoint;
import com.apifest.example.ReplaceCustomerIdAction;

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

    @Test
    public void when_invoke_action_pass_request_uri() throws Exception {
        // GIVEN
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/v0.1/countries?id=BUL");
        MappingEndpoint mapping = mock(MappingEndpoint.class);
        willReturn("http://example.com").given(mapping).getBackendHost();
        MappingConfig config = mock(MappingConfig.class);
        HttpResponse validationResponse = mock(HttpResponse.class);
        willReturn("/countries").given(mapping).getInternalEndpoint();
        MappingAction action = mock(MappingAction.class);
        willReturn(action).given(mapping).getAction();
        ReplaceCustomerIdAction replaceAction = mock(ReplaceCustomerIdAction.class);
        willReturn(replaceAction).given(config).getAction(action);
        willReturn(request).given(replaceAction).execute(request, "/countries?id=BUL", validationResponse);

        // WHEN
        handler.mapRequest(request, mapping, config, validationResponse);

        // THEN
        verify(replaceAction).execute(request, "/countries?id=BUL", validationResponse);
    }

    @Test
    public void when_apifest_mappings_invoke_getLoadedMappings() throws Exception {
        // GIVEN
        MessageEvent message = mock(MessageEvent.class);
        HttpRequest req = mock(HttpRequest.class);
        doReturn(req).when(message).getMessage();
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        doReturn(ChannelBuffers.EMPTY_BUFFER).when(req).getContent();
        doReturn(HttpRequestHandler.MAPPINGS_URI).when(req).getUri();
        doReturn(HttpMethod.GET).when(req).getMethod();
        doNothing().when(handler).getLoadedMappings(any(Channel.class));
        doNothing().when(handler).setConnectTimeout(any(Channel.class));

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
        doReturn(req).when(message).getMessage();
        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        doReturn(ChannelBuffers.EMPTY_BUFFER).when(req).getContent();
        doReturn(HttpRequestHandler.GLOBAL_ERRORS_URI).when(req).getUri();
        doReturn(HttpMethod.GET).when(req).getMethod();
        doNothing().when(handler).getLoadedGlobalErrors(any(Channel.class));
        doNothing().when(handler).setConnectTimeout(any(Channel.class));

        // WHEN
        handler.messageReceived(ctx, message);

        // THEN
        verify(handler).getLoadedGlobalErrors(any(Channel.class));
    }
}

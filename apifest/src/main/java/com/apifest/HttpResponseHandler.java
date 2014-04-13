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

import java.net.ConnectException;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for backend responses.
 *
 * @author Rossitsa Borissova
 */
public class HttpResponseHandler extends SimpleChannelUpstreamHandler {

    protected Logger log = LoggerFactory.getLogger(HttpResponseHandler.class);
    private static final int  HTTP_STATUS_300 = 300;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        HttpResponse response = null;
        if(e.getMessage() instanceof HttpResponse){
            response = (HttpResponse) e.getMessage();
            int statusCode = response.getStatus().getCode();
            // TODO: check error response for version
            if (statusCode >= HTTP_STATUS_300 && (MappingConfigLoader.getConfig().get(0).getErrorMessage(statusCode) != null)) {
                String content = MappingConfigLoader.getConfig().get(0).getErrorMessage(statusCode);
                if (content != null) {
                    response.setContent(ChannelBuffers.copiedBuffer(content.getBytes()));
                    response.headers().add("Content-Length", content.getBytes().length);
                }
            }
            log.info("response: {}", new String(ChannelBuffers.copiedBuffer(response.getContent()).array()));
        }
        Channel channel = ctx.getChannel();
        channel.close();
        if(ctx.getAttachment() instanceof TokenValidationListener) {
            TokenValidationListener listener = (TokenValidationListener) ctx.getAttachment();
            listener.responseReceived(response);
        } else {
            ResponseListener listener = (ResponseListener) ctx.getAttachment();
            listener.responseReceived(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        if (e.getCause() instanceof ConnectException) {
            log.error("Cannot connect to {}", ctx.getChannel().getRemoteAddress());
        }
        log.error("response handler error: {}", e);
        ctx.sendUpstream(e);
    }
}


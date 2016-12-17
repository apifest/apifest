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

import org.apache.http.HttpStatus;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;
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
        Integer statusCode = null;
        if(e.getMessage() instanceof HttpResponse){
            response = (HttpResponse) e.getMessage();
            statusCode = response.getStatus().getCode();
        }
        Channel channel = ctx.getChannel();
        // do not close the channel if 100 Continue
        if (statusCode != null && statusCode.intValue() == HttpStatus.SC_CONTINUE) {
            //do not close the channel
        } else {
            channel.close();
        }
        if(ctx.getAttachment() instanceof TokenValidationListener) {
            TokenValidationListener listener = (TokenValidationListener) ctx.getAttachment();
            listener.responseReceived(response);
        } else {
            ResponseListener listener = (ResponseListener) ctx.getAttachment();
            // check listener errors map
            if (statusCode != null && statusCode >= HTTP_STATUS_300 && (listener.getErrorMessage(statusCode) != null)) {
                String content = listener.getErrorMessage(statusCode);
                if (content != null) {
                    response.headers().set(HttpHeaders.Names.CONTENT_TYPE, HttpResponseFactory.APPLICATION_JSON);
                    response.setContent(ChannelBuffers.copiedBuffer(content.getBytes(CharsetUtil.UTF_8)));
                    response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, content.getBytes(CharsetUtil.UTF_8).length);
                }
            }
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


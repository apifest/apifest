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

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.pool.ChannelPool;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Map;

/**
 * Handler for backend responses.
 *
 * @author Rossitsa Borissova
 */
@ChannelHandler.Sharable
public class HttpResponseHandler extends ChannelInboundHandlerAdapter {

    public static final AttributeKey<ResponseListener> responseListenerAttachmentKey = AttributeKey.newInstance("responseListenerAttachmentKey");
    public static final AttributeKey<ChannelPool> poolAttachmentKey = AttributeKey.newInstance("pool");
    protected Logger log = LoggerFactory.getLogger(HttpResponseHandler.class);
    private static final int HTTP_STATUS_300 = 300;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object e) {
        FullHttpResponse response = null;
        Integer statusCode = null;

        if (e instanceof FullHttpResponse) {
            response = (FullHttpResponse) e;
            statusCode = response.status().code();
        }
        Channel channel = ctx.channel();
        // do not close the channel if 100 Continue or the client connection is keep-alive
        if (((statusCode == null) || (statusCode != null && statusCode.intValue() != HttpStatus.SC_CONTINUE)) && !HttpUtil.isKeepAlive(response)) {
            channel.close();
        }

        ResponseListener listener = channel.attr(responseListenerAttachmentKey).get();
        // check listener errors map
        if (statusCode != null && statusCode >= HTTP_STATUS_300 && (listener.getErrorMessage(statusCode) != null)) {
            String content = listener.getErrorMessage(statusCode);
            if (content != null) {
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpResponseFactory.APPLICATION_JSON);
                response.replace(Unpooled.copiedBuffer(content.getBytes(CharsetUtil.UTF_8)));
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.getBytes(CharsetUtil.UTF_8).length);
            }
        }
        listener.responseReceived(response, channel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) throws Exception {
        if (e.getCause() instanceof ConnectException) {
            log.error("Cannot connect to {}", ctx.channel().remoteAddress());
        }
        log.error("response handler error: {}", e);
        ctx.fireExceptionCaught(e);
    }
}


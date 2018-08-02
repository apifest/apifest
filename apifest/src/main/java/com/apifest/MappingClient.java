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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Client that re-sends the requests to the backend and handles the responses.
 *
 * @author Rossitsa Borissova
 */
public final class MappingClient {

    private static final int MAX_CONTENT_LEN = 10 * 1024 * 1024;

    private static volatile MappingClient client;

    protected Logger log = LoggerFactory.getLogger(MappingClient.class);
    Bootstrap b = new Bootstrap();

    private MappingClient(EventLoopGroup group) {
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast("codec", new HttpClientCodec());
                        p.addLast("aggregator", new HttpObjectAggregator(MAX_CONTENT_LEN));
                        p.addLast("handler", new HttpResponseHandler());
                    }
                });
        b.option(ChannelOption.TCP_NODELAY, true);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.SO_LINGER, -1);
        b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ServerConfig.getConnectTimeout());
    }

    public synchronized static MappingClient getClient(EventLoopGroup group) {
        if (client == null) {
            client = new MappingClient(group);
        }
        return client;
    }

    /**
     * Sends the request to the given backend.
     *
     * @param request request that should be sent to the given backend
     * @param host backend host
     * @param port backend port
     * @param responseListener listener that will handles the backend response
     */
    public void send(final FullHttpRequest request, String host, int port, final ResponseListener responseListener) {
        ChannelFuture future = b.clone().connect(new InetSocketAddress(host, port));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                final Channel channel = future.channel();
                channel.config().setConnectTimeoutMillis(ServerConfig.getConnectTimeout());
                if (channel.isWritable()) {
                    channel.attr(HttpResponseHandler.responseListenerAttachmentKey).set(responseListener);
                    if (future.isSuccess() && channel.isOpen()) {
                        channel.writeAndFlush(request);
                        LifecycleEventHandlers.invokeRequestEventHandlers(request, null);
                    } else {
                        // if cannot connect
                        channel.disconnect();
                        channel.close();
                        FullHttpResponse response = HttpResponseFactory.createISEResponse();
                        // HttpResponse response = HttpResponseFactory.createNotFoundResponse();
                        responseListener.responseReceived(response);
                    }
                } else {
                    channel.disconnect();
                    channel.close();
                    FullHttpResponse response = HttpResponseFactory.createISEResponse();
                    // HttpResponse response = HttpResponseFactory.createNotFoundResponse();
                    responseListener.responseReceived(response);
                }
            }
        });
    }
}
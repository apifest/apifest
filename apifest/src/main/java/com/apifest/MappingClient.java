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
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.SimpleChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Client that re-sends the requests to the backend and handles the responses.
 *
 * @author Rossitsa Borissova
 */
public final class MappingClient {

    private static final int MAX_CONTENT_LEN = 10 * 1024 * 1024;

    private static volatile MappingClient client;

    public static Bootstrap b = new Bootstrap()
            .channel(NioSocketChannel.class)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.SO_LINGER, -1)
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, ServerConfig.getConnectTimeout())
            .group(MappingServer.workerGroup);

    public static ChannelPoolMap<InetSocketAddress, SimpleChannelPool> poolMap = new AbstractChannelPoolMap<InetSocketAddress, SimpleChannelPool>() {
        @Override
        protected SimpleChannelPool newPool(InetSocketAddress key) {
            // The SimpleChannelPool internally clones the bootstrap object so it's fine to set a new remote address every time
            return new SimpleChannelPool(b.remoteAddress(key), new ChannelPoolHandler() {
                public void channelReleased(Channel ch) throws Exception {

                }

                public void channelAcquired(Channel ch) throws Exception {

                }

                public void channelCreated(Channel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new HttpClientCodec());
                    p.addLast(new HttpObjectAggregator(MAX_CONTENT_LEN));
                    p.addLast(new HttpResponseHandler());
                }
            });
        }
    };

    protected Logger log = LoggerFactory.getLogger(MappingClient.class);


    public synchronized static MappingClient getClient() {
        if (client == null) {
            client = new MappingClient();
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
        final ChannelPool channelPool = poolMap.get(new InetSocketAddress(host, port));
        channelPool.acquire().addListener(new FutureListener<Channel>() {
            @Override
            public void operationComplete(Future<Channel> future) {
                final Channel channel = future.getNow();
                if (channel == null) {
                    responseListener.responseReceived(HttpResponseFactory.createISEResponse(), channel);
                }
                if (channel.isWritable()) {
                    channel.attr(HttpResponseHandler.responseListenerAttachmentKey).set(responseListener);
                    channel.attr(HttpResponseHandler.poolAttachmentKey).set(channelPool);
                    if (future.isSuccess() && channel.isOpen()) {
                        channel.writeAndFlush(request);
                        LifecycleEventHandlers.invokeRequestEventHandlers(request, null);
                    } else {
                        // if cannot connect
                        channel.disconnect();
                        channel.close();
                        FullHttpResponse response = HttpResponseFactory.createISEResponse();
                        // HttpResponse response = HttpResponseFactory.createNotFoundResponse();
                        responseListener.responseReceived(response, channel);
                    }
                } else {
                    channel.disconnect();
                    channel.close();
                    FullHttpResponse response = HttpResponseFactory.createISEResponse();
                    // HttpResponse response = HttpResponseFactory.createNotFoundResponse();
                    responseListener.responseReceived(response, channel);
                }
            }
        });
    }
}
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

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client that re-sends the requests to the backend and handles the responses.
 *
 * @author Rossitsa Borissova
 */
public final class MappingClient {

    private ClientBootstrap bootstrap;
    private static MappingClient client;

    protected Logger log = LoggerFactory.getLogger(MappingClient.class);

    private MappingClient() {
        ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());
        bootstrap = new ClientBootstrap(factory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("codec", new HttpClientCodec());
                pipeline.addLast("aggregator", new HttpChunkAggregator(4096));
                pipeline.addLast("handler", new HttpResponseHandler());
                return pipeline;
            }
        });

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("child.soLinger", -1);
        bootstrap.setOption("child.connectTimeoutMillis", ServerConfig.getConnectTimeout());
    }

    public static MappingClient getClient() {
        if(client == null) {
            client = new MappingClient();
        }
        return client;
    }

    /**
     * Sends the request to the given backend.
     * @param request request that should be sent to the given backend
     * @param host backend host
     * @param port backend port
     * @param responseListener listener that will handles the backend response
     */
    public void send(final HttpRequest request, String host, int port, final ResponseListener responseListener) {
        ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                final Channel channel = future.getChannel();
                channel.getConfig().setConnectTimeoutMillis(ServerConfig.getConnectTimeout());
                if(channel.isConnected()) {
                    channel.getPipeline().getContext("handler").setAttachment(responseListener);
                    if (future.isSuccess() && channel.isOpen()){
                       channel.write(request);
                    } else {
                        //if cannot connect
                        channel.disconnect();
                        channel.close();
                        HttpResponse response = HttpResponseFactory.createISEResponse();
                        // HttpResponse response = HttpResponseFactory.createNotFoundResponse();
                        responseListener.responseReceived(response);
                    }
                } else {
                    channel.disconnect();
                    channel.close();
                    HttpResponse response = HttpResponseFactory.createISEResponse();
                    // HttpResponse response = HttpResponseFactory.createNotFoundResponse();
                    responseListener.responseReceived(response);
                }
            }
        });
    }
}
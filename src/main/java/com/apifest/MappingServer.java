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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class responsible for ApiFest Mapping Server.
 *
 * @author Rossitsa Borissova
 */
public final class MappingServer {

    private static Logger log = LoggerFactory.getLogger(MappingServer.class);

    private MappingServer() {
    }

    public static void main(String[] args) {
        if(!serverSetupChecks()){
            System.exit(1);
        }

        ChannelFactory factory = new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool());

        ServerBootstrap bootstrap = new ServerBootstrap(factory);
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

            @Override
            public ChannelPipeline getPipeline() {
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", new HttpRequestDecoder());
                pipeline.addLast("aggregator", new HttpChunkAggregator(4096));
                pipeline.addLast("encoder", new HttpResponseEncoder());
                pipeline.addLast("handler", new HttpRequestHandler());
                return pipeline;
            }
        });

        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.keepAlive", true);
        bootstrap.setOption("child.soLinger", -1);
        bootstrap.setOption("child.connectTimeoutMillis", ServerConfig.getConnectTimeout());

        bootstrap.bind(new InetSocketAddress(ServerConfig.getHost(), ServerConfig.getPort()));

        try {
            MappingConfigLoader.load();
        } catch (IllegalArgumentException e) {
            System.exit(1);
        }
        log.info("ApiFest Mapping Server started at " + ServerConfig.getHost() + ":" + ServerConfig.getPort());
    }

    /**
     * Checks whether all required server setup properties are passed as system properties.
     */
    private static boolean serverSetupChecks() {
        try {
            ServerConfig.readProperties();
        }catch (NumberFormatException e) {
            log.error("Property values not valid");
            return false;
        } catch (IOException e) {
            log.error("Cannot load properties file");
            return false;
        }
        return true;
    }
}
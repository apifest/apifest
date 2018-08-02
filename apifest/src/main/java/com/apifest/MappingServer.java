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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.api.MappingException;

/**
 * Class responsible for ApiFest Mapping Server.
 *
 * @author Rossitsa Borissova
 */
public final class MappingServer {

    private static Logger log = LoggerFactory.getLogger(MappingServer.class);

    private static final int MAX_CONTENT_LEN = 10 * 1024 * 1024;

    public static MappingClient client;

    private MappingServer() {
    }

    public static void main(String[] args) {
        if (!serverSetupChecks()) {
            System.exit(1);
        }

        if (ServerConfig.getCustomJarPath() != null && !ServerConfig.getCustomJarPath().isEmpty()) {
            try {
                ConfigLoader.loadCustomHandlers();
            } catch (MappingException e) {
                log.error("Cannot load custom jar", e);
                System.exit(1);
            }
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(8);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();

                            p.addLast(new HttpRequestDecoder());
                            p.addLast(new HttpObjectAggregator(MAX_CONTENT_LEN));
                            p.addLast(new HttpResponseEncoder());
                            p.addLast(new HttpRequestHandler());
                        }
                    });

            bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childOption(ChannelOption.SO_LINGER, -1);
            bootstrap.childOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, ServerConfig.getConnectTimeout());

            if (ServerConfig.getMappingsPath() != null && !ServerConfig.getMappingsPath().isEmpty()) {
                try {
                    ConfigLoader.loadMappingsConfig(false);
                } catch (MappingException e) {
                    log.error("Cannot load mappings config", e);
                    System.exit(1);
                } catch (NumberFormatException e) {
                    log.error("Cannot load mappings config", e);
                    System.exit(1);
                }
            }

            if (ServerConfig.getGlobalErrorsFile() != null && !ServerConfig.getGlobalErrorsFile().isEmpty()) {
                try {
                    ConfigLoader.loadGlobalErrorsConfig(false);
                } catch (MappingException e) {
                    log.error("Cannot load global errors config", e);
                    System.exit(1);
                }
            }
            log.info("ApiFest Mapping Server started at " + ServerConfig.getHost() + ":" + ServerConfig.getPort());

            client = MappingClient.getClient(workerGroup);

            bootstrap.bind(new InetSocketAddress(ServerConfig.getHost(), ServerConfig.getPort())).channel().closeFuture().sync();
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * Checks whether all required server setup properties are passed as system properties.
     */
    private static boolean serverSetupChecks() {
        try {
            ServerConfig.readProperties();
        } catch (NumberFormatException e) {
            log.error("Property value not valid", e);
            return false;
        } catch (IOException e) {
            log.error("Cannot load properties file");
            return false;
        }
        return true;
    }
}
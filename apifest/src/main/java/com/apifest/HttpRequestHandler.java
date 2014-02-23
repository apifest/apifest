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

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.api.BasicAction;
import com.apifest.api.BasicFilter;
import com.apifest.api.MappingAction;
import com.apifest.api.MappingEndpoint;


/**
 * Handler for requests received on the server.
 *
 * @author Rossitsa Borissova
 */
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {

    protected static final String RELOAD_URI = "/apifest-reload";

    protected Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);

    private MappingClient client = MappingClient.getClient();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        final Channel channel = ctx.getChannel();
        /*SocketChannelConfig cfg = (SocketChannelConfig) channel.getConfig();
        cfg.setSoLinger(-1);
        cfg.setTcpNoDelay(true);*/
        setConnectTimeout(channel);
        Object message = e.getMessage();
        if(message instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) message;
            String uri = req.getUri();
            HttpMethod method = req.getMethod();
            if(RELOAD_URI.equals(uri) && method.equals(HttpMethod.GET)) {
                reloadMappingConfig(channel);
                return;
            }

            MappingConfig config = MappingConfigLoader.getConfig();
            MappingEndpoint mapping = config.getMappingEndpoint(uri, method.toString());
            String userId = null;
            if (mapping != null) {
                log.debug("authRequired: {}", mapping.getAuthRequired());
                if("true".equals(mapping.getAuthRequired())) {
                    AccessTokenValidator tokenValidator = new AccessTokenValidator();
                    HttpResponse tokenCheckResponse = tokenValidator.checkAcessToken(req, mapping.getScope());
                    if(!HttpResponseStatus.OK.equals(tokenCheckResponse.getStatus())) {
                        ChannelFuture future = channel.write(tokenCheckResponse);
                        log.debug(tokenCheckResponse.getContent().toString());
                        future.addListener(ChannelFutureListener.CLOSE);
                        return;
                    }
                    userId = getUserId(tokenCheckResponse);
                }

                // if several filters, create them all
                BasicFilter filter = null;
                if (mapping.getFilters() != null && mapping.getFilters().size() > 0 ){
                    try {
                        filter = config.getFilter(mapping.getFilters().get(0));
                    } catch (MappingException e1) {
                        log.error("cannot map request", e1);
                        HttpResponse response = HttpResponseFactory.createISEResponse();
                        ChannelFuture future = channel.write(response);
                        future.addListener(ChannelFutureListener.CLOSE);
                        return;
                    }
                }

                ResponseListener responseListener = new ResponseListener(filter) {
                    @Override
                    public void responseReceived(HttpMessage response) {
                        HttpMessage newResponse = response;
                        if(response instanceof HttpResponse) {
                            HttpResponse res = (HttpResponse) response;
                            if(res.getStatus().getCode() >= 300) {
                                //return error response
                                newResponse = res;
                            }
                            if (res.getStatus().getCode() < 300 && getFilter() != null){
                                newResponse = getFilter().execute((HttpResponse) response);
                            }
                        }
                        ChannelFuture future = channel.write(newResponse);
                        setConnectTimeout(channel);
                        future.addListener(ChannelFutureListener.CLOSE);
                    }
                };

                channel.getPipeline().getContext("handler").setAttachment(responseListener);

                BaseMapper mapper = new BaseMapper();
                req = mapper.map(req, mapping.getInternalEndpoint());

                if(mapping.getActions() != null) {
                    for(MappingAction mappingAction : mapping.getActions()) {
                        BasicAction action;
                        try {
                            action = config.getAction(mappingAction);
                            req = action.execute(req, mapping.getInternalEndpoint(), userId);
                        } catch (MappingException e1) {
                            log.error("cannot map request", e1);
                            HttpResponse response = HttpResponseFactory.createISEResponse();
                            ChannelFuture future = channel.write(response);
                            future.addListener(ChannelFutureListener.CLOSE);
                            return;
                        }
                    }
                }
                client.send(req, ServerConfig.getBackendHost(), ServerConfig.getBackendPort(), responseListener);
            } else {
                // if no mapping found
                HttpResponse response = HttpResponseFactory.createNotFoundResponse();
                ChannelFuture future = channel.write(response);
                future.addListener(ChannelFutureListener.CLOSE);
                return;
            }
        } else {
            log.info("write response here from the BE");
        }
    }

    protected void setConnectTimeout(final Channel channel) {
        channel.getConfig().setConnectTimeoutMillis(ServerConfig.getConnectTimeout());
        channel.getConfig().setOption("soLinger", -1);
    }

    protected void reloadMappingConfig(final Channel channel) {
        MappingConfigLoader.reloadConfigs();
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        ChannelFuture future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
        return;
    }

    protected String getUserId(HttpResponse response) {
        JSONObject json;
        String userId = null;
        try {
            json = new JSONObject(new String(response.getContent().array()));
            userId = json.getString("userId");
        } catch (JSONException e1) {
            log.info("Cannot parse JSON", e1);
        }
        return userId;
    }

}

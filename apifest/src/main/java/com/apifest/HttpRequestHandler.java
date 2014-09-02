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

import java.net.URISyntaxException;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.api.BasicAction;
import com.apifest.api.BasicFilter;
import com.apifest.api.MappingAction;
import com.apifest.api.MappingEndpoint;
import com.apifest.api.MappingException;

/**
 * Handler for requests received on the server.
 *
 * @author Rossitsa Borissova
 */
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {

    protected static final String RELOAD_URI = "/apifest-reload";

    protected static final String ACCESS_TOKEN_REQUIRED = "{\"error\":\"access token required\"}";
    protected static final String INVALID_ACCESS_TOKEN_SCOPE = "{\"error\":\"access token scope not valid\"}";
    protected static final String INVALID_ACCESS_TOKEN = "{\"error\":\"access token not valid\"}";
    protected static final String INVALID_ACCESS_TOKEN_TYPE = "{\"error\":\"access token type not valid\"}";

    protected static final String OAUTH_TOKEN_VALIDATE_URI = "/oauth20/token/validate";

    protected static Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);

    private MappingClient client = MappingClient.getClient();

    private static final int HTTP_STATUS_300 = 300;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        final Channel channel = ctx.getChannel();

        setConnectTimeout(channel);
        Object message = e.getMessage();
        if (message instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) message;
            String uri = req.getUri();
            HttpMethod method = req.getMethod();
            if (RELOAD_URI.equals(uri) && method.equals(HttpMethod.GET)) {
                reloadMappingConfig(channel);
                return;
            }

            List<MappingConfig> configList = MappingConfigLoader.getConfig();
            MappingEndpoint mapping = null;
            MappingConfig config = null;
            for (MappingConfig mconfig : configList) {
                mapping = mconfig.getMappingEndpoint(uri, method.toString());
                if (mapping != null) {
                    config = mconfig;
                    break;
                }
            }
            if (mapping != null) {
                if (mapping.getAuthType() != null) {
                    String accessToken = null;
                    List<String> authorizationHeaders = req.headers().getAll(HttpHeaders.Names.AUTHORIZATION);
                    for (String header : authorizationHeaders) {
                        accessToken = AccessTokenValidator.extractAccessToken(header);
                        if (accessToken != null) {
                            break;
                        }
                    }

                    if(accessToken == null) {
                        writeResponseToChannel(channel, HttpResponseFactory.createUnauthorizedResponse(ACCESS_TOKEN_REQUIRED));
                        return;
                    }

                    BasicFilter filter;
                    try {
                        filter = getMappingFilter(mapping, config, channel);
                    } catch (MappingException e2) {
                        log.error("cannot map request", e2);
                        writeResponseToChannel(channel, HttpResponseFactory.createISEResponse());
                        return;
                    }

                    final ResponseListener responseListener = createResponseListener(filter, channel);

                    final HttpRequest request = req;
                    final MappingEndpoint endpoint = mapping;
                    final MappingConfig conf = config;

                    // validates access token
                    TokenValidationListener validatorListener = new TokenValidationListener() {
                        @Override
                        public void responseReceived(HttpMessage response) {
                            HttpMessage tokenResponse = response;
                            if (response instanceof HttpResponse) {
                                HttpResponse tokenValidationResponse = (HttpResponse) response;
                                if (!HttpResponseStatus.OK.equals(tokenValidationResponse.getStatus())) {
                                    writeResponseToChannel(channel, HttpResponseFactory.createUnauthorizedResponse(INVALID_ACCESS_TOKEN));
                                    return;
                                }
                                String tokenContent = new String(ChannelBuffers.copiedBuffer(tokenValidationResponse.getContent()).array());
                                boolean scopeOk = AccessTokenValidator.validateTokenScope(tokenContent, endpoint.getScope());
                                if(!scopeOk) {
                                     log.debug("access token scope not valid");
                                     writeResponseToChannel(channel, HttpResponseFactory.createUnauthorizedResponse(INVALID_ACCESS_TOKEN_SCOPE));
                                     return;
                                }

                                String userId = BasicAction.getUserId(tokenValidationResponse);
                                if((MappingEndpoint.AUTH_TYPE_USER.equals(endpoint.getAuthType()) && (userId != null && userId.length() > 0)) ||
                                        MappingEndpoint.AUTH_TYPE_CLIENT_APP.equals(endpoint.getAuthType())) {
                                    try {
                                        HttpRequest mappedReq = mapRequest(request, endpoint, conf, tokenValidationResponse);
                                        channel.getPipeline().getContext("handler").setAttachment(responseListener);
                                        client.send(mappedReq, endpoint.getBackendHost(), Integer.valueOf(endpoint.getBackendPort()), responseListener);
                                    } catch (MappingException e) {
                                        log.error("cannot map request", e);
                                        writeResponseToChannel(channel, HttpResponseFactory.createISEResponse());
                                        return;
                                    }
                                } else {
                                    writeResponseToChannel(channel, HttpResponseFactory.createUnauthorizedResponse(INVALID_ACCESS_TOKEN_TYPE));
                                    return;
                                }
                            } else {
                                ChannelFuture future = channel.write(tokenResponse);
                                setConnectTimeout(channel);
                                future.addListener(ChannelFutureListener.CLOSE);
                            }
                        }
                    };

                    channel.getPipeline().getContext("handler").setAttachment(validatorListener);
                    HttpRequest validateReq = createTokenValidateRequest(accessToken);
                    client.sendValidation(validateReq, ServerConfig.tokenValidateHost, ServerConfig.tokenValidatePort, validatorListener);
                } else {
                    try {
                        BasicFilter filter = getMappingFilter(mapping, config, channel);
                        ResponseListener responseListener = createResponseListener(filter, channel);

                        channel.getPipeline().getContext("handler").setAttachment(responseListener);

                        HttpRequest mappedReq = mapRequest(req, mapping, config, null);
                        client.send(mappedReq, mapping.getBackendHost(), Integer.valueOf(mapping.getBackendPort()), responseListener);
                    } catch (MappingException e2) {
                        log.error("cannot map request", e2);
                        writeResponseToChannel(channel, HttpResponseFactory.createISEResponse());
                        return;
                    }
                }
            } else {
                // if no mapping found
                HttpResponse response = HttpResponseFactory.createNotFoundResponse();
                ChannelFuture future = channel.write(response);
                future.addListener(ChannelFutureListener.CLOSE);
                return;
            }
        } else {
            log.debug("write response here from the BE");
        }
    }

    protected ResponseListener createResponseListener( BasicFilter filter, final Channel channel) {
        ResponseListener responseListener = new ResponseListener(filter) {
            @Override
            public void responseReceived(HttpMessage response) {
                HttpMessage newResponse = response;
                if (response instanceof HttpResponse) {
                    HttpResponse res = (HttpResponse) response;
                    // status greater than HTTP 300
                    if (res.getStatus().getCode() >= HTTP_STATUS_300) {
                        // return error response
                        newResponse = res;
                    }
                    if (res.getStatus().getCode() < HTTP_STATUS_300 && getFilter() != null) {
                        newResponse = getFilter().execute((HttpResponse) response);
                    }
                }
                ChannelFuture future = channel.write(newResponse);
                setConnectTimeout(channel);
                future.addListener(ChannelFutureListener.CLOSE);
            }
        };
        return responseListener;
    }

    protected HttpRequest mapRequest(HttpRequest request, MappingEndpoint mapping, MappingConfig config, HttpResponse tokenValidationResponse) throws MappingException {
        BaseMapper mapper = new BaseMapper();
        HttpRequest req = mapper.map(request, mapping.getInternalEndpoint());
        if (mapping.getActions() != null) {
            for (MappingAction mappingAction : mapping.getActions()) {
                BasicAction action = config.getAction(mappingAction);
                req = action.execute(req, req.getUri(), tokenValidationResponse);
            }
        }
        return req;
    }

    protected BasicFilter getMappingFilter(MappingEndpoint mapping, MappingConfig config, final Channel channel) throws MappingException {
        BasicFilter filter = null;
        if (mapping.getFilters() != null && mapping.getFilters().size() > 0) {
            filter = config.getFilter(mapping.getFilters().get(0));
        }
        return filter;
    }

    protected void writeResponseToChannel(Channel channel, HttpResponse response) {
        ChannelFuture future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
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

    protected HttpRequest createTokenValidateRequest(String accessToken) {
        QueryStringEncoder enc = new QueryStringEncoder(OAUTH_TOKEN_VALIDATE_URI);
        enc.addParam("token", accessToken);
        String uri = OAUTH_TOKEN_VALIDATE_URI;
        try {
            uri = enc.toUri().toString();
        } catch (URISyntaxException e) {
            log.error("cannot build token validation URI", e);
        }
        return new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
    }
}

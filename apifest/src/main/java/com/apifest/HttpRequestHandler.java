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
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
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
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.api.BasicAction;
import com.apifest.api.BasicFilter;
import com.apifest.api.MappingEndpoint;
import com.apifest.api.MappingException;
import com.apifest.api.UpstreamException;
import com.google.gson.Gson;

/**
 * Handler for requests received on the server.
 *
 * @author Rossitsa Borissova
 */
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {

    protected static final String RELOAD_URI = "/apifest-reload";
    protected static final String MAPPINGS_URI = "/apifest-mappings";
    protected static final String GLOBAL_ERRORS_URI = "/apifest-global-errors";

    protected static final String ACCESS_TOKEN_REQUIRED = "{\"error\":\"access token required\"}";
    protected static final String INVALID_ACCESS_TOKEN_SCOPE = "{\"error\":\"access token scope not valid\"}";
    protected static final String INVALID_ACCESS_TOKEN = "{\"error\":\"access token not valid\"}";
    protected static final String INVALID_ACCESS_TOKEN_TYPE = "{\"error\":\"access token type not valid\"}";

    protected static final String OAUTH_TOKEN_VALIDATE_URI = "/oauth20/tokens/validate";

    protected static Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);

    private MappingClient client = MappingClient.getClient();

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
        final Channel channel = ctx.getChannel();

        setConnectTimeout(channel);
        Object message = e.getMessage();
        if (message instanceof HttpRequest) {
            HttpRequest req = (HttpRequest) message;
            LifecycleEventHandlers.invokeRequestEventHandlers(req, null);
            String uri = req.getUri();
            HttpMethod method = req.getMethod();
            if (RELOAD_URI.equals(uri) && method.equals(HttpMethod.GET)) {
                reloadMappingConfig(channel);
                return;
            }

            if (MAPPINGS_URI.equals(uri) && method.equals(HttpMethod.GET)) {
                getLoadedMappings(channel);
                return;
            }

            if (GLOBAL_ERRORS_URI.equals(uri) && method.equals(HttpMethod.GET)) {
                getLoadedGlobalErrors(channel);
                return;
            }

            List<MappingConfig> configList = ConfigLoader.getConfig();
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

                    if (accessToken == null) {
                        writeResponseToChannel(channel, req, HttpResponseFactory.createUnauthorizedResponse(ACCESS_TOKEN_REQUIRED));
                        return;
                    }

                    BasicFilter filter;
                    try {
                        filter = getMappingFilter(mapping, config, channel);
                    } catch (MappingException e2) {
                        log.error("cannot map request", e2);
                        LifecycleEventHandlers.invokeExceptionHandler(e2, req);
                        writeResponseToChannel(channel, req, HttpResponseFactory.createISEResponse());
                        return;
                    }

                    final ResponseListener responseListener = createResponseListener(filter, config.getErrors(), channel, req);

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
                                    writeResponseToChannel(channel, request, HttpResponseFactory.createUnauthorizedResponse(INVALID_ACCESS_TOKEN));
                                    return;
                                }
                                String tokenContent = tokenValidationResponse.getContent().toString(CharsetUtil.UTF_8);
                                boolean scopeOk = AccessTokenValidator.validateTokenScope(tokenContent, endpoint.getScope());
                                if (!scopeOk) {
                                    log.debug("access token scope not valid");
                                    writeResponseToChannel(channel, request, HttpResponseFactory.createUnauthorizedResponse(INVALID_ACCESS_TOKEN_SCOPE));
                                    return;
                                }

                                String userId = BasicAction.getUserId(tokenValidationResponse);
                                if ((MappingEndpoint.AUTH_TYPE_USER.equals(endpoint.getAuthType()) && (userId != null && userId.length() > 0)) ||
                                        MappingEndpoint.AUTH_TYPE_CLIENT_APP.equals(endpoint.getAuthType())) {
                                    try {
                                        HttpRequest mappedReq = mapRequest(request, endpoint, conf, tokenValidationResponse);
                                        if (mappedReq == null) {
                                            throw new UpstreamException(HttpResponseFactory.createISEResponse());
                                        }
                                        channel.getPipeline().getContext("handler").setAttachment(responseListener);
                                        client.send(mappedReq, endpoint.getBackendHost(), Integer.valueOf(endpoint.getBackendPort()), responseListener);
                                    } catch (MappingException e) {
                                        log.error("cannot map request", e);
                                        LifecycleEventHandlers.invokeExceptionHandler(e, request);

                                        writeResponseToChannel(channel, request, HttpResponseFactory.createISEResponse());
                                        return;
                                    } catch (UpstreamException ue) {
                                        writeResponseToChannel(channel, request, ue.getResponse());
                                        return;
                                    } catch (Exception e) {  // Not nice but ensures we ALWAYS respond to the client
                                        writeResponseToChannel(channel, request, HttpResponseFactory.createISEResponse());
                                        return;
                                    }
                                } else {
                                    writeResponseToChannel(channel, request, HttpResponseFactory.createUnauthorizedResponse(INVALID_ACCESS_TOKEN_TYPE));
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
                    if (ServerConfig.tokenValidateHost == null || ServerConfig.tokenValidateHost.isEmpty() || ServerConfig.tokenValidatePort == null) {
                        log.error("token.validation.host and token.validation.port properties are not set. Cannot validate access token.");
                        writeResponseToChannel(channel, request, HttpResponseFactory.createUnauthorizedResponse(INVALID_ACCESS_TOKEN));
                    } else {
                        HttpRequest validateReq = createTokenValidateRequest(accessToken);
                        client.sendValidation(validateReq, ServerConfig.tokenValidateHost, ServerConfig.tokenValidatePort, validatorListener);
                    }
                } else {
                    try {
                        BasicFilter filter = getMappingFilter(mapping, config, channel);
                        ResponseListener responseListener = createResponseListener(filter, config.getErrors(), channel, req);

                        channel.getPipeline().getContext("handler").setAttachment(responseListener);

                        HttpRequest mappedReq = mapRequest(req, mapping, config, null);
                        client.send(mappedReq, mapping.getBackendHost(), Integer.valueOf(mapping.getBackendPort()), responseListener);
                    } catch (MappingException e2) {
                        log.error("cannot map request", e2);
                        LifecycleEventHandlers.invokeExceptionHandler(e2, req);

                        writeResponseToChannel(channel, req, HttpResponseFactory.createISEResponse());
                        return;
                    } catch (UpstreamException ue) {
                        LifecycleEventHandlers.invokeResponseEventHandlers(req, ue.getResponse());
                        writeResponseToChannel(channel, req, ue.getResponse());
                        return;
                    }
                }
            } else {
                // if no mapping found
                HttpResponse response = HttpResponseFactory.createNotFoundResponse();
                writeResponseToChannel(channel, req, response);
                return;
            }
        } else {
            log.debug("write response here from the BE");
        }
    }

    protected ResponseListener createResponseListener(BasicFilter filter, Map<String, String> errors, final Channel channel, final HttpRequest request) {
        ResponseListener responseListener = new ResponseListener(filter, errors) {
            @Override
            public void responseReceived(HttpMessage response) {
                HttpMessage newResponse = response;
                if (response instanceof HttpResponse) {
                    if (getFilter() != null) {
                        newResponse = getFilter().execute((HttpResponse) response);
                    }
                }
                LifecycleEventHandlers.invokeResponseEventHandlers(request, (HttpResponse) newResponse);
                ChannelFuture future = channel.write(newResponse);
                if (!HttpHeaders.isKeepAlive(request)) {
                    future.addListener(ChannelFutureListener.CLOSE);
                }
            }
        };
        return responseListener;
    }

    protected HttpRequest mapRequest(HttpRequest request, MappingEndpoint mapping, MappingConfig config, HttpResponse tokenValidationResponse)
            throws MappingException, UpstreamException {
        BaseMapper mapper = new BaseMapper();
        request.headers().set(HttpHeaders.Names.HOST, mapping.getBackendHost());
        HttpRequest req = mapper.map(request, mapping.getInternalEndpoint());
        if (mapping.getAction() != null) {
            BasicAction action = config.getAction(mapping.getAction());
            req = action.execute(req, tokenValidationResponse, mapping);
        }
        return req;
    }

    protected BasicFilter getMappingFilter(MappingEndpoint mapping, MappingConfig config, final Channel channel) throws MappingException {
        BasicFilter filter = null;
        if (mapping.getFilter() != null) {
            filter = config.getFilter(mapping.getFilter());
        }
        return filter;
    }

    protected void writeResponseToChannel(Channel channel, HttpRequest request, HttpResponse response) {
        LifecycleEventHandlers.invokeResponseEventHandlers(request, response);
        ChannelFuture future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    protected void setConnectTimeout(final Channel channel) {
        channel.getConfig().setConnectTimeoutMillis(ServerConfig.getConnectTimeout());
        channel.getConfig().setOption("soLinger", -1);
    }

    protected void reloadMappingConfig(final Channel channel) {
        HttpResponse response = null;
        try {
            ConfigLoader.reloadConfigs();
            response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        } catch (MappingException e) {
            response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            ChannelBuffer content = ChannelBuffers.copiedBuffer(e.getMessage().getBytes(CharsetUtil.UTF_8));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            response.setContent(content);
        }
        ChannelFuture future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
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
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri);
        request.headers().add(HttpHeaders.Names.HOST, ServerConfig.tokenValidateHost);
        // REVISIT: propagate all custom headers?
        return request;
    }

    protected void getLoadedMappings(Channel channel) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        Map<String, MappingConfig> mappings = ConfigLoader.getLoadedMappings();
        Gson gson = new Gson();
        String jsonObj = gson.toJson(mappings);
        response.setContent(ChannelBuffers.copiedBuffer(jsonObj.getBytes(CharsetUtil.UTF_8)));
        ChannelFuture future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    protected void getLoadedGlobalErrors(Channel channel) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        Map<Integer, String> mappings = ConfigLoader.getLoadedGlobalErrors();
        Gson gson = new Gson();
        String jsonObj = gson.toJson(mappings);
        response.setContent(ChannelBuffers.copiedBuffer(jsonObj.getBytes(CharsetUtil.UTF_8)));
        ChannelFuture future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);

    }
}

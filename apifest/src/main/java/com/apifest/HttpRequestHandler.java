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

import com.apifest.api.AccessToken;
import com.apifest.api.BasicAction;
import com.apifest.api.BasicFilter;
import com.apifest.api.ExceptionEventHandler;
import com.apifest.api.LifecycleHandler;
import com.apifest.api.MappingEndpoint;
import com.apifest.api.MappingException;
import com.apifest.api.UpstreamException;
import com.apifest.oauth20.ApplicationInfo;
import com.apifest.oauth20.AuthorizationServer;
import com.apifest.oauth20.ClientCredentials;
import com.apifest.oauth20.DBManagerFactory;
import com.apifest.oauth20.OAuthException;
import com.apifest.oauth20.QueryParameter;
import com.apifest.oauth20.Response;
import com.apifest.oauth20.ScopeService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handler for requests received on the server.
 *
 * @author Rossitsa Borissova
 */
@ChannelHandler.Sharable
public class HttpRequestHandler extends ChannelInboundHandlerAdapter {

    public static final String RELOAD_URI = "/apifest-reload";
    public static final String MAPPINGS_URI = "/apifest-mappings";
    public static final String GLOBAL_ERRORS_URI = "/apifest-global-errors";

    public static final String ACCESS_TOKEN_REQUIRED = "{\"error\":\"access token required\"}";
    public static final String INVALID_ACCESS_TOKEN_SCOPE = "{\"error\":\"access token scope not valid\"}";
    public static final String INVALID_ACCESS_TOKEN = "{\"error\":\"access token not valid\"}";
    public static final String INVALID_ACCESS_TOKEN_TYPE = "{\"error\":\"access token type not valid\"}";

    public static final String OAUTH_TOKEN_VALIDATE_URI = "/oauth20/tokens/validate";

    public static final String AUTH_CODE_URI = "/oauth20/auth-codes";
    public static final String ACCESS_TOKEN_URI = "/oauth20/tokens";
    public static final String ACCESS_TOKEN_VALIDATE_URI = "/oauth20/tokens/validate";
    public static final String APPLICATION_URI = "/oauth20/applications";
    public static final String ACCESS_TOKEN_REVOKE_URI = "/oauth20/tokens/revoke";
    public static final String OAUTH_CLIENT_SCOPE_URI = "/oauth20/scopes";

    public static final String CLIENT_CREDENTIALS_PATTERN_STRING = "[a-f[0-9]]+";
    public static final Pattern APPLICATION_PATTERN = Pattern.compile("/oauth20/applications/(" + CLIENT_CREDENTIALS_PATTERN_STRING + ")$");
    public static final Pattern OAUTH_CLIENT_SCOPE_PATTERN = Pattern.compile("/oauth20/scopes/((\\p{Alnum}+-?_?)+$)");

    public AuthorizationServer auth = new AuthorizationServer();

    public static Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);
    public static Logger accessTokensLog = LoggerFactory.getLogger("accessTokens");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object e) {
        final Channel channel = ctx.channel();

        setConnectTimeout(channel);
        Object message = e;
        if (message instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) message;
            LifecycleEventHandlers.invokeRequestEventHandlers(req, null);
            String rawUri = req.getUri();
            String uriPath = null;
            try {
                uriPath = new URI(rawUri).getRawPath();
            } catch (URISyntaxException e1) {
                e1.printStackTrace();
            }
            HttpMethod method = req.getMethod();
            if (RELOAD_URI.equals(rawUri) && method.equals(HttpMethod.GET)) {
                reloadMappingConfig(channel);
                return;
            }

            if (MAPPINGS_URI.equals(rawUri) && method.equals(HttpMethod.GET)) {
                getLoadedMappings(channel);
                return;
            }

            if (GLOBAL_ERRORS_URI.equals(rawUri) && method.equals(HttpMethod.GET)) {
                getLoadedGlobalErrors(channel);
                return;
            }

            FullHttpResponse response = null;

            if (APPLICATION_URI.equals(uriPath) && method.equals(HttpMethod.POST)) {
                response = handleRegister(req);
            } else if (AUTH_CODE_URI.equals(uriPath) && method.equals(HttpMethod.GET)) {
                response = handleAuthorize(req);
            } else if (ACCESS_TOKEN_URI.equals(uriPath) && method.equals(HttpMethod.POST)) {
                response = handleToken(req);
            } else if (ACCESS_TOKEN_URI.equals(uriPath) && method.equals(HttpMethod.DELETE)) {
                response = handleUserTokenRevoke(req);
            } else if (ACCESS_TOKEN_VALIDATE_URI.equals(uriPath) && method.equals(HttpMethod.GET)) {
                response = handleTokenValidate(req);
            } else if (APPLICATION_URI.equals(uriPath) && method.equals(HttpMethod.GET)) {
                response = handleGetAllClientApplications(req);
            } else if (rawUri.startsWith(APPLICATION_URI) && method.equals(HttpMethod.GET)) {
                response = handleGetClientApplication(req);
            } else if (ACCESS_TOKEN_REVOKE_URI.equals(uriPath) && method.equals(HttpMethod.POST)) {
                response = handleTokenRevoke(req);
            } else if (OAUTH_CLIENT_SCOPE_URI.equals(uriPath) && method.equals(HttpMethod.GET)) {
                response = handleGetAllScopes(req);
            } else if (OAUTH_CLIENT_SCOPE_URI.equals(uriPath) && method.equals(HttpMethod.POST)) {
                response = handleRegisterScope(req);
            } else if (ACCESS_TOKEN_URI.equals(uriPath) && method.equals(HttpMethod.GET)) {
                response = handleGetAccessTokens(req);
            } else if (uriPath.startsWith(OAUTH_CLIENT_SCOPE_URI) && method.equals(HttpMethod.PUT)) {
                response = handleUpdateScope(req);
            } else if (uriPath.startsWith(OAUTH_CLIENT_SCOPE_URI) && method.equals(HttpMethod.GET)) {
                response = handleGetScope(req);
            } else if (uriPath.startsWith(APPLICATION_URI) && method.equals(HttpMethod.PUT)) {
                response = handleUpdateClientApplication(req);
            } else if (uriPath.startsWith(OAUTH_CLIENT_SCOPE_URI) && method.equals(HttpMethod.DELETE)) {
                response = handleDeleteScope(req);
            }

            if (response != null) {
                ChannelFuture future = channel.writeAndFlush(response);

                if (!HttpHeaders.isKeepAlive(req)) {
                    future.addListener(ChannelFutureListener.CLOSE);
                }
                return;
            }


            List<MappingConfig> configList = ConfigLoader.getConfig();
            MappingEndpoint mapping = null;
            MappingConfig config = null;
            for (MappingConfig mconfig : configList) {
                mapping = mconfig.getMappingEndpoint(rawUri, method.toString());
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

                    final FullHttpRequest request = req;
                    final MappingEndpoint endpoint = mapping;
                    final MappingConfig conf = config;

                    AccessToken validToken = auth.isValidToken(accessToken);

                    if (validToken == null || validToken != null && !validToken.isValid()) {
                        writeResponseToChannel(channel, request, HttpResponseFactory.createUnauthorizedResponse(INVALID_ACCESS_TOKEN));
                        return;
                    }
                    boolean scopeOk = AccessTokenValidator.validateTokenScope(validToken, endpoint.getScope());
                    if (!scopeOk) {
                        log.debug("access token scope not valid");
                        writeResponseToChannel(channel, request, HttpResponseFactory.createUnauthorizedResponse(INVALID_ACCESS_TOKEN_SCOPE));
                        return;
                    }

                    String userId = validToken.getUserId();
                    if ((MappingEndpoint.AUTH_TYPE_USER.equals(endpoint.getAuthType()) && (userId != null && userId.length() > 0)) ||
                            MappingEndpoint.AUTH_TYPE_CLIENT_APP.equals(endpoint.getAuthType())) {
                        try {
                            FullHttpRequest mappedReq = mapRequest(request, endpoint, conf, validToken);
                            if (mappedReq == null) {
                                throw new UpstreamException(HttpResponseFactory.createISEResponse());
                            }
                            MappingServer.client.send(mappedReq, endpoint.getBackendHost(), Integer.valueOf(endpoint.getBackendPort()), responseListener);
                        } catch (MappingException mappingException) {
                            log.error("cannot map request", mappingException);
                            LifecycleEventHandlers.invokeExceptionHandler(mappingException, request);

                            writeResponseToChannel(channel, request, HttpResponseFactory.createISEResponse());
                            return;
                        } catch (UpstreamException ue) {
                            writeResponseToChannel(channel, request, ue.getResponse());
                            return;
                        } catch (Exception ge) {  // Not nice but ensures we ALWAYS respond to the client
                            writeResponseToChannel(channel, request, HttpResponseFactory.createISEResponse());
                            return;
                        }
                    } else {
                        writeResponseToChannel(channel, request, HttpResponseFactory.createUnauthorizedResponse(INVALID_ACCESS_TOKEN_TYPE));
                        return;
                    }

                } else {
                    try {
                        BasicFilter filter = getMappingFilter(mapping, config, channel);
                        ResponseListener responseListener = createResponseListener(filter, config.getErrors(), channel, req);


                        FullHttpRequest mappedReq = mapRequest(req, mapping, config, null);
                        MappingServer.client.send(mappedReq, mapping.getBackendHost(), Integer.valueOf(mapping.getBackendPort()), responseListener);
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
                response = HttpResponseFactory.createNotFoundResponse();
                writeResponseToChannel(channel, req, response);
                return;
            }
        } else {
            log.debug("write response here from the BE");
        }
    }

    public FullHttpResponse handleGetClientApplication(HttpRequest req) {
        FullHttpResponse response = null;
        Matcher m = APPLICATION_PATTERN.matcher(req.getUri());
        if (m.find()) {
            String clientId = m.group(1);
            ApplicationInfo appInfo = auth.getApplicationInfo(clientId);
            if (appInfo != null) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String json = mapper.writeValueAsString(appInfo);
                    log.debug(json);
                    response = Response.createOkResponse(json);
                } catch (JsonGenerationException e) {
                    log.error("error get application info", e);
                    invokeExceptionHandler(e, req);
                } catch (JsonMappingException e) {
                    log.error("error get application info", e);
                    invokeExceptionHandler(e, req);
                } catch (IOException e) {
                    log.error("error get application info", e);
                    invokeExceptionHandler(e, req);
                }
            } else {
                response = Response.createResponse(HttpResponseStatus.NOT_FOUND, Response.CLIENT_APP_NOT_EXIST);
            }
        } else {
            response = Response.createNotFoundResponse();
        }
        return response;
    }

    public FullHttpResponse handleTokenValidate(HttpRequest req) {
        FullHttpResponse response = null;
        QueryStringDecoder dec = new QueryStringDecoder(req.getUri());
        Map<String, List<String>> params = dec.parameters();
        String tokenParam = QueryParameter.getFirstElement(params, QueryParameter.TOKEN);
        if (tokenParam == null || tokenParam.isEmpty()) {
            response = Response.createBadRequestResponse();
        } else {
            AccessToken token = auth.isValidToken(tokenParam);
            if (token != null) {
                Gson gson = new Gson();
                String json = gson.toJson(token);
                log.debug(json);
                response = Response.createOkResponse(json);
            } else {
                response = Response.createUnauthorizedResponse();
            }
        }
        return response;
    }

    public FullHttpResponse handleToken(FullHttpRequest request) {
        FullHttpResponse response = null;
        String contentType = request.headers().get(HttpHeaders.Names.CONTENT_TYPE);
        if (contentType != null && contentType.contains(HttpHeaders.Values.APPLICATION_X_WWW_FORM_URLENCODED)) {
            try {
                AccessToken accessToken = auth.issueAccessToken(request);
                if (accessToken != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    String jsonString = mapper.writeValueAsString(accessToken);
                    log.debug("access token:" + jsonString);
                    response = Response.createOkResponse(jsonString);
                    accessTokensLog.debug("token {}", jsonString);
                }
            } catch (OAuthException ex) {
                response = Response.createOAuthExceptionResponse(ex);
                invokeExceptionHandler(ex, request);
            } catch (JsonGenerationException e1) {
                log.error("error handle token", e1);
                invokeExceptionHandler(e1, request);
            } catch (JsonMappingException e1) {
                log.error("error handle token", e1);
                invokeExceptionHandler(e1, request);
            } catch (IOException e1) {
                log.error("error handle token", e1);
                invokeExceptionHandler(e1, request);
            }
            if (response == null) {
                response = Response.createBadRequestResponse(Response.CANNOT_ISSUE_TOKEN);
            }
        } else {
            response = Response.createResponse(HttpResponseStatus.BAD_REQUEST, Response.UNSUPPORTED_MEDIA_TYPE);
        }
        return response;
    }

    public void invokeRequestEventHandlers(FullHttpRequest request, FullHttpResponse response) {
        invokeHandlers(request, response, com.apifest.oauth20.LifecycleEventHandlers.getRequestEventHandlers());
    }

    public void invokeResponseEventHandlers(FullHttpRequest request, FullHttpResponse response) {
        invokeHandlers(request, response, com.apifest.oauth20.LifecycleEventHandlers.getResponseEventHandlers());
    }

    public void invokeExceptionHandler(Exception ex, HttpRequest request) {
        List<Class<ExceptionEventHandler>> handlers = com.apifest.oauth20.LifecycleEventHandlers.getExceptionHandlers();
        for (int i = 0; i < handlers.size(); i++) {
            try {
                ExceptionEventHandler handler = handlers.get(i).newInstance();
                handler.handleException(ex, request);
            } catch (InstantiationException e) {
                log.error("cannot instantiate exception handler", e);
                invokeExceptionHandler(e, request);
            } catch (IllegalAccessException e) {
                log.error("cannot invoke exception handler", e);
                invokeExceptionHandler(ex, request);
            }
        }
    }

    public void invokeHandlers(FullHttpRequest request, FullHttpResponse response, List<Class<LifecycleHandler>> handlers) {
        for (int i = 0; i < handlers.size(); i++) {
            try {
                LifecycleHandler handler = handlers.get(i).newInstance();
                handler.handle(request, response);
            } catch (InstantiationException e) {
                log.error("cannot instantiate handler", e);
                invokeExceptionHandler(e, request);
            } catch (IllegalAccessException e) {
                log.error("cannot invoke handler", e);
                invokeExceptionHandler(e, request);
            }
        }
    }

    public FullHttpResponse handleAuthorize(HttpRequest req) {
        FullHttpResponse response = null;
        try {
            String redirectURI = auth.issueAuthorizationCode(req);
            // TODO: validation http protocol?
            log.debug("redirectURI: {}", redirectURI);

            // return auth_code
            JsonObject obj = new JsonObject();
            obj.addProperty("redirect_uri", redirectURI);
            response = Response.createOkResponse(obj.toString());
            accessTokensLog.info("authCode {}", obj.toString());
        } catch (OAuthException ex) {
            response = Response.createOAuthExceptionResponse(ex);
            invokeExceptionHandler(ex, req);
        }
        return response;
    }

    public FullHttpResponse handleRegister(FullHttpRequest req) {
        FullHttpResponse response = null;
        try {
            ClientCredentials creds = auth.issueClientCredentials(req);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(creds);
            log.debug("credentials:" + jsonString);
            response = Response.createOkResponse(jsonString);
        } catch (OAuthException ex) {
            response = Response.createOAuthExceptionResponse(ex);
            invokeExceptionHandler(ex, req);
        } catch (JsonGenerationException e1) {
            log.error("error handle register", e1);
            invokeExceptionHandler(e1, req);
        } catch (JsonMappingException e1) {
            log.error("error handle register", e1);
            invokeExceptionHandler(e1, req);
        } catch (IOException e1) {
            log.error("error handle register", e1);
            invokeExceptionHandler(e1, req);
        }
        if (response == null) {
            response = Response.createBadRequestResponse(Response.CANNOT_REGISTER_APP);
        }
        return response;
    }

    public FullHttpResponse handleUserTokenRevoke(FullHttpRequest req) {
        boolean revoked = false;
        try {
            revoked = auth.revokeUserAccessTokens(req);
        } catch (OAuthException e) {
            log.error("cannot revoke token", e);
            invokeExceptionHandler(e, req);
            return Response.createOAuthExceptionResponse(e);
        }
        String json = "{\"revoked\":\"" + revoked + "\"}";
        FullHttpResponse response = Response.createOkResponse(json);
        return response;
    }

    public FullHttpResponse handleTokenRevoke(FullHttpRequest req) {
        boolean revoked = false;
        try {
            revoked = auth.revokeToken(req);
        } catch (OAuthException e) {
            log.error("cannot revoke token", e);
            invokeExceptionHandler(e, req);
            return Response.createOAuthExceptionResponse(e);
        }
        //String json = "{\"revoked\":\"" + revoked + "\"}";
        JsonObject json = new JsonObject();
        json.addProperty("revoked", revoked);
        FullHttpResponse response = Response.createOkResponse(json.toString());
        return response;
    }

    public FullHttpResponse handleRegisterScope(FullHttpRequest req) {
        ScopeService scopeService = getScopeService();
        FullHttpResponse response = null;
        try {
            String responseMsg = scopeService.registerScope(req);
            response = Response.createOkResponse(responseMsg);
        } catch (OAuthException e) {
            invokeExceptionHandler(e, req);
            response = Response.createResponse(e.getHttpStatus(), e.getMessage());
        }
        return response;
    }

    public FullHttpResponse handleUpdateScope(FullHttpRequest req) {
        FullHttpResponse response = null;
        Matcher m = OAUTH_CLIENT_SCOPE_PATTERN.matcher(req.getUri());
        if (m.find()) {
            String scopeName = m.group(1);
            ScopeService scopeService = getScopeService();
            try {
                String responseMsg = scopeService.updateScope(req, scopeName);
                response = Response.createOkResponse(responseMsg);
            } catch (OAuthException e) {
                invokeExceptionHandler(e, req);
                response = Response.createResponse(e.getHttpStatus(), e.getMessage());
            }
        } else {
            response = Response.createNotFoundResponse();
        }
        return response;
    }

    public FullHttpResponse handleGetAllScopes(HttpRequest req) {
        ScopeService scopeService = getScopeService();
        FullHttpResponse response = null;
        try {
            String jsonString = scopeService.getScopes(req);
            response = Response.createOkResponse(jsonString);
        } catch (OAuthException e) {
            invokeExceptionHandler(e, req);
            response = Response.createResponse(e.getHttpStatus(), e.getMessage());
        }
        return response;
    }

    public FullHttpResponse handleGetScope(HttpRequest req) {
        FullHttpResponse response = null;
        Matcher m = OAUTH_CLIENT_SCOPE_PATTERN.matcher(req.getUri());
        if (m.find()) {
            String scopeName = m.group(1);
            ScopeService scopeService = getScopeService();
            try {
                String responseMsg = scopeService.getScopeByName(scopeName);
                response = Response.createOkResponse(responseMsg);
            } catch (OAuthException e) {
                invokeExceptionHandler(e, req);
                response = Response.createResponse(e.getHttpStatus(), e.getMessage());
            }
        } else {
            response = Response.createNotFoundResponse();
        }
        return response;
    }

    public FullHttpResponse handleDeleteScope(HttpRequest req) {
        FullHttpResponse response = null;
        Matcher m = OAUTH_CLIENT_SCOPE_PATTERN.matcher(req.getUri());
        if (m.find()) {
            String scopeName = m.group(1);
            ScopeService scopeService = getScopeService();
            try {
                String responseMsg = scopeService.deleteScope(scopeName);
                response = Response.createOkResponse(responseMsg);
            } catch (OAuthException e) {
                invokeExceptionHandler(e, req);
                response = Response.createResponse(e.getHttpStatus(), e.getMessage());
            }
        } else {
            response = Response.createNotFoundResponse();
        }
        return response;
    }

    public ScopeService getScopeService() {
        return new ScopeService();
    }

    public FullHttpResponse handleUpdateClientApplication(FullHttpRequest req) {
        FullHttpResponse response = null;
        Matcher m = APPLICATION_PATTERN.matcher(req.uri());
        if (m.find()) {
            String clientId = m.group(1);
            try {
                if (auth.updateClientApp(req, clientId)) {
                    response = Response.createOkResponse(Response.CLIENT_APP_UPDATED);
                }
            } catch (OAuthException ex) {
                response = Response.createOAuthExceptionResponse(ex);
                invokeExceptionHandler(ex, req);
            }
        } else {
            response = Response.createNotFoundResponse();
        }
        return response;
    }

    public FullHttpResponse handleGetAllClientApplications(HttpRequest req) {
        List<ApplicationInfo> apps = filterClientApps(req, DBManagerFactory.getInstance().getAllApplications());
        ObjectMapper mapper = new ObjectMapper();
        FullHttpResponse response = null;
        try {
            String jsonString = mapper.writeValueAsString(apps);
            response = Response.createOkResponse(jsonString);
        } catch (JsonGenerationException e) {
            log.error("cannot list client applications", e);
            invokeExceptionHandler(e, req);
            response = Response.createResponse(HttpResponseStatus.BAD_REQUEST, Response.CANNOT_LIST_CLIENT_APPS);
        } catch (JsonMappingException e) {
            log.error("cannot list client applications", e);
            invokeExceptionHandler(e, req);
            response = Response.createResponse(HttpResponseStatus.BAD_REQUEST, Response.CANNOT_LIST_CLIENT_APPS);
        } catch (IOException e) {
            log.error("cannot list client applications", e);
            invokeExceptionHandler(e, req);
            response = Response.createResponse(HttpResponseStatus.BAD_REQUEST, Response.CANNOT_LIST_CLIENT_APPS);
        }

        return response;
    }

    public List<ApplicationInfo> filterClientApps(HttpRequest req, List<ApplicationInfo> apps) {
        List<ApplicationInfo> filteredApps = new ArrayList<ApplicationInfo>();
        QueryStringDecoder dec = new QueryStringDecoder(req.getUri());
        Map<String, List<String>> params = dec.parameters();
        if (params != null) {
            String status = QueryParameter.getFirstElement(params, "status");
            Integer statusInt = null;
            if (status != null && !status.isEmpty()) {
                try {
                    statusInt = Integer.valueOf(status);
                    for (ApplicationInfo app : apps) {
                        if (app.getStatus() == statusInt) {
                            filteredApps.add(app);
                        }
                    }
                } catch (NumberFormatException e) {
                    // status is invalid, ignore it
                    filteredApps = Collections.unmodifiableList(apps);
                }
            } else {
                filteredApps = Collections.unmodifiableList(apps);
            }
        }
        return filteredApps;
    }

    public FullHttpResponse handleGetAccessTokens(HttpRequest req) {
        FullHttpResponse response = null;
        QueryStringDecoder dec = new QueryStringDecoder(req.getUri());
        Map<String, List<String>> params = dec.parameters();
        String clientId = QueryParameter.getFirstElement(params, QueryParameter.CLIENT_ID);
        String userId = QueryParameter.getFirstElement(params, QueryParameter.USER_ID);
        if (clientId == null || clientId.isEmpty()) {
            response = Response.createBadRequestResponse(String.format(Response.MANDATORY_PARAM_MISSING, QueryParameter.CLIENT_ID));
        } else if (userId == null || userId.isEmpty()) {
            response = Response.createBadRequestResponse(String.format(Response.MANDATORY_PARAM_MISSING, QueryParameter.USER_ID));
        } else {
            // check that clientId exists, no matter whether it is active or not
            if (!auth.isExistingClient(clientId)) {
                response = Response.createBadRequestResponse(Response.INVALID_CLIENT_ID);
            } else {
                List<AccessToken> accessTokens = DBManagerFactory.getInstance().getAccessTokenByUserIdAndClientApp(userId, clientId);
                Gson gson = new Gson();
                String jsonString = gson.toJson(accessTokens);
                response = Response.createOkResponse(jsonString);
            }
        }
        return response;
    }

    public ResponseListener createResponseListener(BasicFilter filter, Map<String, String> errors, final Channel channel, final FullHttpRequest request) {
        return new ResponseListener(filter, errors) {
            @Override
            public void responseReceived(FullHttpMessage response, Channel clientChannel) {
                HttpMessage newResponse = response;
                if (response instanceof FullHttpResponse) {
                    if (getFilter() != null) {
                        newResponse = getFilter().execute((FullHttpResponse) response);
                    }
                }
                LifecycleEventHandlers.invokeResponseEventHandlers(request, (FullHttpResponse) newResponse);
                ChannelFuture future = channel.writeAndFlush(newResponse);
                clientChannel.attr(HttpResponseHandler.poolAttachmentKey).get().release(clientChannel);
                if (!HttpUtil.isKeepAlive(request)) {
                    future.addListener(ChannelFutureListener.CLOSE);
                }
            }
        };
    }

    public FullHttpRequest mapRequest(FullHttpRequest request, MappingEndpoint mapping, MappingConfig config, AccessToken validToken)
            throws MappingException, UpstreamException {
        BaseMapper mapper = new BaseMapper();
        request.headers().set(HttpHeaderNames.HOST, mapping.getBackendHost());
        FullHttpRequest req = mapper.map(request, mapping.getInternalEndpoint());
        if (mapping.getAction() != null) {
            BasicAction action = config.getAction(mapping.getAction());
            req = action.execute(req, validToken, mapping);
        }
        return req;
    }

    public BasicFilter getMappingFilter(MappingEndpoint mapping, MappingConfig config, final Channel channel) throws MappingException {
        BasicFilter filter = null;
        if (mapping.getFilter() != null) {
            filter = config.getFilter(mapping.getFilter());
        }
        return filter;
    }

    public void writeResponseToChannel(Channel channel, FullHttpRequest request, FullHttpResponse response) {
        LifecycleEventHandlers.invokeResponseEventHandlers(request, response);
        ChannelFuture future = channel.writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    public void setConnectTimeout(final Channel channel) {
        channel.config().setConnectTimeoutMillis(ServerConfig.getConnectTimeout());
        channel.config().setOption(ChannelOption.TCP_NODELAY.SO_LINGER, -1);
    }

    public void reloadMappingConfig(final Channel channel) {
        FullHttpResponse response = null;
        try {
            ConfigLoader.reloadConfigs();
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        } catch (MappingException e) {
            response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);
            ByteBuf content = Unpooled.copiedBuffer(e.getMessage().getBytes(CharsetUtil.UTF_8));
            response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
            response.replace(content);
        }
        ChannelFuture future = channel.writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    public HttpRequest createTokenValidateRequest(String accessToken) {
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

    public void getLoadedMappings(Channel channel) {
        Map<String, MappingConfig> mappings = ConfigLoader.getLoadedMappings();
        Gson gson = new Gson();
        String jsonObj = gson.toJson(mappings);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(jsonObj.getBytes(CharsetUtil.UTF_8)));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        ChannelFuture future = channel.writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    public void getLoadedGlobalErrors(Channel channel) {
        Map<Integer, String> mappings = ConfigLoader.getLoadedGlobalErrors();
        Gson gson = new Gson();
        String jsonObj = gson.toJson(mappings);
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(jsonObj.getBytes(CharsetUtil.UTF_8)));
        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        ChannelFuture future = channel.writeAndFlush(response);
        future.addListener(ChannelFutureListener.CLOSE);

    }
}

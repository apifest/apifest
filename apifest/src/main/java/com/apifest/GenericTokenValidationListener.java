package com.apifest;

import org.apache.http.client.protocol.RequestAuthCache;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.api.BasicAction;
import com.apifest.api.BasicFilter;
import com.apifest.api.MappingEndpoint;
import com.apifest.api.MappingException;
import com.apifest.api.UpstreamException;

/**
 *
 * @author Apostol Terziev
 *
 */
public class GenericTokenValidationListener implements TokenValidationListener {

    protected static Logger log = LoggerFactory.getLogger(GenericTokenValidationListener.class);
    private Channel channel;
    private HttpRequest request;
    private MappingEndpoint endpoint;
    private MappingConfig config;
    private MappingClient client = MappingClient.getClient();

    public GenericTokenValidationListener(Channel channel, HttpRequest request, MappingEndpoint endpoint, MappingConfig config) {
        this.channel = channel;
        this.request = request;
        this.endpoint = endpoint;
        this.config = config;

    }

    @Override
    public void responseReceived(HttpMessage response) {
        HttpMessage tokenResponse = response;
        if (response instanceof HttpResponse) {
            HttpResponse tokenValidationResponse = (HttpResponse) response;
            if (!HttpResponseStatus.OK.equals(tokenValidationResponse.getStatus())) {
                ChannelUtilities.writeResponseToChannel(channel, request, HttpResponseFactory.createUnauthorizedResponse(HttpRequestHandler.INVALID_ACCESS_TOKEN));
                return;
            }
            String tokenContent = tokenValidationResponse.getContent().toString(CharsetUtil.UTF_8);
            boolean scopeOk = AccessTokenValidator.validateTokenScope(tokenContent, endpoint.getScope());
            if (!scopeOk) {
                log.debug("access token scope not valid");
                ChannelUtilities.writeResponseToChannel(channel, request, HttpResponseFactory.createUnauthorizedResponse(HttpRequestHandler.INVALID_ACCESS_TOKEN_SCOPE));
                return;
            }

            String userId = BasicAction.getUserId(tokenValidationResponse);
            if ((MappingEndpoint.AUTH_TYPE_USER.equals(endpoint.getAuthType()) && (userId != null && userId.length() > 0)) ||
                    MappingEndpoint.AUTH_TYPE_CLIENT_APP.equals(endpoint.getAuthType())) {
                try {
                    HttpRequest mappedReq = ChannelUtilities.mapRequest(request, endpoint, config, tokenValidationResponse);
                    BasicFilter filter;
                    try {
                        filter = ChannelUtilities.getMappingFilter(endpoint, config, channel);
                    } catch (MappingException e2) {
                        log.error("cannot map request", e2);
                        LifecycleEventHandlers.invokeExceptionHandler(e2, request);
                        ChannelUtilities.writeResponseToChannel(channel, request, HttpResponseFactory.createISEResponse());
                        return;
                    }
                    ResponseListener responseListener = ChannelUtilities.createResponseListener(filter, config.getErrors(), channel, request);
                    channel.getPipeline().getContext("handler").setAttachment(responseListener);
                    client.send(mappedReq, endpoint.getBackendHost(), Integer.valueOf(endpoint.getBackendPort()), responseListener);
                } catch (MappingException e) {
                    log.error("cannot map request", e);
                    LifecycleEventHandlers.invokeExceptionHandler(e, request);

                    ChannelUtilities.writeResponseToChannel(channel, request, HttpResponseFactory.createISEResponse());
                    return;
                } catch (UpstreamException ue) {
                    ChannelUtilities.writeResponseToChannel(channel, request, ue.getResponse());
                    return;
                }
            } else {
                ChannelUtilities.writeResponseToChannel(channel, request, HttpResponseFactory.createUnauthorizedResponse(HttpRequestHandler.INVALID_ACCESS_TOKEN_TYPE));
                return;
            }
        } else {
            ChannelFuture future = channel.write(tokenResponse);
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

}

package com.apifest;

import java.util.Map;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

import com.apifest.api.BasicAction;
import com.apifest.api.BasicFilter;
import com.apifest.api.MappingEndpoint;
import com.apifest.api.MappingException;
import com.apifest.api.UpstreamException;

public class ChannelUtilities {

    protected static void writeResponseToChannel(Channel channel, HttpRequest request, HttpResponse response) {
        LifecycleEventHandlers.invokeResponseEventHandlers(request, response);
        ChannelFuture future = channel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    protected static void setConnectTimeout(final Channel channel) {
        channel.getConfig().setOption("soLinger", -1);
        channel.getConfig()
        .setConnectTimeoutMillis(ServerConfig.getConnectTimeout());
    }

    protected static ResponseListener createResponseListener(BasicFilter filter, Map<String, String> errors, final Channel channel, final HttpRequest request) {
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

    protected static BasicFilter getMappingFilter(MappingEndpoint mapping, MappingConfig config, final Channel channel) throws MappingException {
        BasicFilter filter = null;
        if (mapping.getFilter() != null) {
            filter = config.getFilter(mapping.getFilter());
        }
        return filter;
    }

    protected static HttpRequest mapRequest(HttpRequest request, MappingEndpoint mapping, MappingConfig config, HttpResponse tokenValidationResponse)
            throws MappingException, UpstreamException {
        BaseMapper mapper = new BaseMapper();
        request.headers().set(HttpHeaders.Names.HOST, mapping.getBackendHost());
        HttpRequest req = mapper.map(request, mapping.getInternalEndpoint());
        if (mapping.getAction() != null) {
            BasicAction action = config.getAction(mapping.getAction());
            req = action.execute(req, req.getUri(), tokenValidationResponse);
        }
        return req;
    }
}

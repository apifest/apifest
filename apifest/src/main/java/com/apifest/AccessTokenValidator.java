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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts access token and perform a call to OAuth20 server to validate it.
 *
 * @author Rossitsa Borissova
 */
public class AccessTokenValidator {

    private static final Pattern AUTH_BEARER_PATTERN = Pattern.compile("(Bearer )(\\w*)");

    protected static Logger log = LoggerFactory.getLogger(AccessTokenValidator.class);

    protected static final String ACCESS_TOKEN_REQUIRED = "{\"error\":\"access token required\"}";
    protected static final String INVALID_ACCESS_TOKEN_SCOPE = "{\"error\":\"scope not valid\"}";
    protected static final String INVALID_ACCESS_TOKEN = "{\"error\":\"access token not valid\"}";

    /**
     * Extracts an access token and calls OAuth20 server token validation.
     * @param req client http request
     * @return HttpResponse that contains information from OAuth20 server token validation. If no response returned,
     * constructs a response with HTTP Status 401/Unauthorized.
     */
    public HttpResponse checkAcessToken(HttpRequest req, String endpointScope) {
        List<String> authorizationHaders = req.getHeaders(HttpHeaders.Names.AUTHORIZATION);
        for(String header : authorizationHaders) {
            String accessToken = extractAccessToken(header);
            if(accessToken != null) {
                return validateAccessToken(accessToken, endpointScope);
            }
        }
        return HttpResponseFactory.createUnauthorizedResponse(ACCESS_TOKEN_REQUIRED);
    }


    protected HttpResponse validateAccessToken(String accessToken, String endpointScope) {
        String tokenValidateURL = ServerConfig.getTokenValidateEndpoint();
        String content = "";
        try {
            URIBuilder builder = new URIBuilder(tokenValidateURL);
            builder.setParameter("token", accessToken);
            HttpGet get = new HttpGet(builder.build());
            HttpClient httpClient = new DefaultHttpClient();
            org.apache.http.HttpResponse tokenResponse = httpClient.execute(get);
            content = getHttpResponseContent(tokenResponse);
            if(content != null) {
                boolean scopeOK = validateTokenScope(content, endpointScope);
                if(!scopeOK) {
                    return HttpResponseFactory.createUnauthorizedResponse(INVALID_ACCESS_TOKEN_SCOPE);
                }
            } else {
                return HttpResponseFactory.createUnauthorizedResponse(INVALID_ACCESS_TOKEN);
            }
        } catch (ClientProtocolException e) {
            log.error("cannot validate access token", e);
            return HttpResponseFactory.createISEResponse();
        } catch (IOException e) {
            log.error("cannot validate access token, IO problem", e);
            return HttpResponseFactory.createISEResponse();
        } catch (URISyntaxException e) {
            log.error("cannot validate access token, URI problem", e);
            return HttpResponseFactory.createISEResponse();
        }
        return HttpResponseFactory.createOKResponse(content);
    }

    protected boolean validateTokenScope(String tokenContent, String endpointScope) {
        String tokenScope = extractTokenScope(tokenContent);
        if(tokenScope == null) {
            return false;
        }
        // tokenScope should be always not null
        String [] allowedScopes = endpointScope.split(",");
        for(String scope : allowedScopes) {
            if(scope.equals(tokenScope)) {
                return true;
            }
        }
        return false;
    }

    protected String extractTokenScope(String tokenContent) {
        String scope = null;
        try {
            JSONObject object = new JSONObject(tokenContent);
            Object rs = object.get("scope");
            if(rs != null && !rs.toString().equals("null")) {
                scope = (String) rs;
            }
        } catch (JSONException e) {
            log.error("Cannot extract scope from content {}", tokenContent);
        }
        return scope;
    }

    protected String getHttpResponseContent(org.apache.http.HttpResponse httpResponse) {
        String content = null;
        if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            InputStream in;
            try {
                in = httpResponse.getEntity().getContent();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte [] b = new byte [1024];
                while((in.read(b) != -1)) {
                    out.write(b);
                }
                content = out.toString("UTF-8");
                log.info("response content: {}", content);
            } catch (IOException e) {
                log.error("cannot get http response content", e);
            }
        }
        return content;
    }

    protected String extractAccessToken(String header) {
        Matcher m = AUTH_BEARER_PATTERN.matcher(header);
        if(m.find()) {
            return m.group(2);
        }
        return null;
    }

}

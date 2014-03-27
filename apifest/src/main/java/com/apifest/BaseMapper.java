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

import java.util.List;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.codec.http.QueryStringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends the passed request to the backend as it is.
 *
 * @author Rossitsa Borissova
 */
public class BaseMapper {

    protected Logger log = LoggerFactory.getLogger(BaseMapper.class);

    public HttpRequest map(HttpRequest req, String internalURI) {
        // pass all query params and headers
        String newUri = constructNewUri(req.getUri(), internalURI);
        req.setUri(newUri);
        log.info("map the request to {}", newUri);
        return req;
    }

    protected String constructNewUri(String uri, String newUri) {
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        Map<String, List<String>> queryParams = decoder.getParameters();
        QueryStringEncoder encoder = new QueryStringEncoder(newUri);
        for(String key : queryParams.keySet()) {
            encoder.addParam(key, queryParams.get(key).get(0));
        }
        return encoder.toString();
    }
}

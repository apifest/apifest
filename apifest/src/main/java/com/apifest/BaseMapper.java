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

    protected static Logger log = LoggerFactory.getLogger(BaseMapper.class);

    public HttpRequest map(HttpRequest req, String internalURI) {
        // pass all query params and headers
        String newUri = constructNewUri(req.getUri(), internalURI);
        req.setUri(newUri);
        log.debug("map the request to {}", newUri);
        return req;
    }

    protected String constructNewUri(String uri, String newUri) {
        QueryStringDecoder decoder = new QueryStringDecoder(uri);
        QueryStringDecoder internalUrldecoder = new QueryStringDecoder(newUri);

        Map<String, List<String>> queryParams = decoder.getParameters();
        Map<String, List<String>> internalQueryParams = internalUrldecoder.getParameters();

        QueryStringEncoder encoder = new QueryStringEncoder(internalUrldecoder.getPath());
        for (String key : queryParams.keySet()) {
            for (String value : queryParams.get(key)) {
                encoder.addParam(key, value);
            }
        }

        for (String key : internalQueryParams.keySet()) {
            for (String value : internalQueryParams.get(key)) {
                encoder.addParam(key, value);
            }
        }
        return encoder.toString();
    }
}

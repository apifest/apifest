/*
 * Copyright 2014, ApiFest project
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

package com.apifest.api;

import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * Exception thrown when the upstream should be broken (no hit of the backend) and a response should be returned directly.
 *
 * @author Rossitsa Borissova
 */
public class UpstreamException  extends Exception {

    private static final long serialVersionUID = 1447022954843271887L;

    private HttpResponse response;

    public UpstreamException(HttpResponse response) {
        this.response = response;
    }

    /**
     * Returns the response that will be returned when this exception is thrown.
     *
     * @return {@link HttpResponse} custom response
     */
    public HttpResponse getResponse() {
        return response;
    }

}

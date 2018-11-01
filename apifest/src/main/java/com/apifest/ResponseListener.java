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

import com.apifest.api.BasicFilter;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpMessage;

import java.util.Map;

/**
 * Interface for listeners that handle the response received.
 *
 * @author Rossitsa Borissova
 */
public abstract class ResponseListener {

    private BasicFilter filter = null;
    private Map<String, String> errors = null;

    public ResponseListener(BasicFilter filter, Map<String, String> errors) {
        this.filter = filter;
        this.errors = errors;
    }

    /**
     * Handles the received response.
     *
     * @param response response received from the backend
     */
    abstract void responseReceived(FullHttpMessage response, Channel clientChannel);

    public BasicFilter getFilter() {
        return filter;
    }

    public String getErrorMessage(int statusCode) {
        return errors.get(String.valueOf(statusCode));
    }

}

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

import org.jboss.netty.handler.codec.http.HttpMessage;

import com.apifest.api.BasicFilter;

/**
 * Interface for listeners that handle the response received.
 *
 * @author Rossitsa Borissova
 */
public abstract class ResponseListener {

    private BasicFilter filter = null;

    public ResponseListener(BasicFilter filter) {
        this.filter = filter;
    }

    /**
     * Handles the received response.
     * @param response response received from the backend
     */
    abstract void responseReceived(HttpMessage response);

    public BasicFilter getFilter(){
        return filter;
    }
}

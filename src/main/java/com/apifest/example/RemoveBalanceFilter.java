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

package com.apifest.example;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.api.BasicFilter;


/**
 * Filter that removes balance field from response.
 *
 * @author Rossitsa Borissova
 */
public class RemoveBalanceFilter extends BasicFilter {

    public static Logger log = LoggerFactory.getLogger(RemoveBalanceFilter.class);

    @Override
    public HttpResponse execute(HttpResponse response) {
        JSONObject json;
        try {
            json = new JSONObject(response.getContent().toString(CharsetUtil.UTF_8));
            log.info("response body: " + json.toString());
            json.remove("balance");
            log.info("modified response body: " + json.toString());
            byte [] newContent = json.toString().getBytes(CharsetUtil.UTF_8);
            response.setContent(ChannelBuffers.copiedBuffer(newContent));
            HttpHeaders.setContentLength(response, newContent.length);
        } catch (JSONException e) {
            log.error("Cannot parse JSON", e);
        }
        return response;
    }

}

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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.spy;
import static org.testng.Assert.*;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.apifest.example.RemoveBalanceFilter;

/**
 * @author Rossitsa Borissova
 */
public class RemoveBalanceFilterTest {

    RemoveBalanceFilter filter;
    HttpResponse response;

    @BeforeMethod
    public void setup() {
        filter = spy(new RemoveBalanceFilter());
        response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().add(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        String content = "{\"customerId\":\"1223\",\"email\":\"rossi.test@apifest.com\",\"balance\":\"1234.34\"}";
        response.setContent(ChannelBuffers.copiedBuffer(content.getBytes()));
        response.headers().add(HttpHeaders.Names.CONTENT_LENGTH, content.getBytes().length);
    }


    @Test
    public void when_return_customer_remove_balance_field() throws Exception {
        // WHEN
        HttpResponse res = filter.execute(response);

        // THEN
        assertEquals(new String(res.getContent().array()), "{\"customerId\":\"1223\",\"email\":\"rossi.test@apifest.com\"}");
    }

}

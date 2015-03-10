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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import org.apache.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.testng.annotations.Test;

import com.apifest.api.BasicAction;
import com.apifest.api.MappingAction;
import com.hazelcast.core.IMap;

/**
 * @author Rossitsa Borissova
 */
public class HttpResponseFactoryTest {

    String customNotFoundMessage = "{\"error\":\"custom resource not found\"}";
    String customUnauthorizedResponse = "{\"error\":\"custom unauthorized response\"}";
    String customISEResponse = "{\"error\":\"custom ISE response\"}";

    @Test
    public void when_ise_response_set_header_and_body() throws Exception {
        // GIVEN
        mockNoGlobalErrors();

        // WHEN
        HttpResponse response = HttpResponseFactory.createISEResponse();

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
        assertEquals(response.headers().get(HttpHeaders.CONTENT_TYPE), HttpResponseFactory.APPLICATION_JSON);
        String error = new String(response.getContent().array());
        assertEquals(error, "");
    }

    @Test
    public void when_unauthorized_response_set_header_and_body() throws Exception {
        // GIVEN
        mockNoGlobalErrors();

        // WHEN
        String errorMsg = "no access token";
        HttpResponse response = HttpResponseFactory.createUnauthorizedResponse(errorMsg);

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.UNAUTHORIZED);
        assertEquals(response.headers().get(HttpHeaders.CONTENT_TYPE), HttpResponseFactory.APPLICATION_JSON);
        String error = new String(response.getContent().array());
        assertEquals(error, errorMsg);
    }

    @Test
    public void when_create_not_found_response_set_header_and_body() throws Exception {
        // GIVEN
        mockNoGlobalErrors();

        // WHEN
        HttpResponse response = HttpResponseFactory.createNotFoundResponse();

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.NOT_FOUND);
        assertEquals(response.headers().get(HttpHeaders.CONTENT_TYPE), HttpResponseFactory.APPLICATION_JSON);
        String error = new String(response.getContent().array());
        assertEquals(error, HttpResponseFactory.NOT_FOUND_CONTENT);
    }

    @Test
    public void when_OK_response_set_header_and_body() throws Exception {
        // GIVEN
        mockNoGlobalErrors();

        // WHEN
        String message = "OK message";
        HttpResponse response = HttpResponseFactory.createOKResponse(message);

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.OK);
        assertEquals(response.headers().get(HttpHeaders.CONTENT_TYPE), HttpResponseFactory.APPLICATION_JSON);
        String responseMsg = new String(response.getContent().array());
        assertEquals(responseMsg, message);
    }

    @Test
    public void when_ise_response_and_global_error_500_exists_use_global_error() throws Exception {
        // GIVEN
        mockGlobalErrors();

        // WHEN
        HttpResponse response = HttpResponseFactory.createISEResponse();

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
        assertEquals(response.headers().get(HttpHeaders.CONTENT_TYPE), HttpResponseFactory.APPLICATION_JSON);
        String error = new String(response.getContent().array());
        assertEquals(error, customISEResponse);
    }


    @Test
    public void when_create_unauthorized_response_and_global_error_401_exists_use_global_error() throws Exception {
        // GIVEN
        mockGlobalErrors();

        // WHEN
        String errorMsg = "no access token";
        HttpResponse response = HttpResponseFactory.createUnauthorizedResponse(errorMsg);

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.UNAUTHORIZED);
        assertEquals(response.headers().get(HttpHeaders.CONTENT_TYPE), HttpResponseFactory.APPLICATION_JSON);
        String error = new String(response.getContent().array());
        assertEquals(error, customUnauthorizedResponse);
    }

    @Test
    public void when_create_not_found_response_and_global_error_404_exists_use_global_error() throws Exception {
        // GIVEN
        mockGlobalErrors();

        // WHEN
        HttpResponse response = HttpResponseFactory.createNotFoundResponse();

        // THEN
        assertEquals(response.getStatus(), HttpResponseStatus.NOT_FOUND);
        assertEquals(response.headers().get(HttpHeaders.CONTENT_TYPE), HttpResponseFactory.APPLICATION_JSON);
        String error = new String(response.getContent().array());
        assertEquals(error, customNotFoundMessage );
    }

    @SuppressWarnings("unchecked")
    private void mockGlobalErrors() throws Exception {
        String hzPath = getClass().getClassLoader().getResource("test_mapping.xml").getPath();
        ServerConfig.mappingsPath = hzPath.replace("/test_mapping.xml", "");
        ServerConfig.globalErrors = getClass().getClassLoader().getResource("global-errors/test_global_errors.xml").getPath();
        ServerConfig.customJarPath = null;

        HazelcastConfigInstance.configInstance = mock(HazelcastConfigInstance.class);

        IMap<Integer, String> errorMap = mock(IMap.class);
        doReturn(errorMap).when(HazelcastConfigInstance.configInstance).getGlobalErrors();

        IMap<String, com.apifest.MappingConfig> mappingMap = mock(IMap.class);

        MappingConfig config = mock(MappingConfig.class);
        doReturn(mock(BasicAction.class)).when(config).getAction(any(MappingAction.class));
        mappingMap.put("v0.1", config);
        doReturn(mappingMap).when(HazelcastConfigInstance.configInstance).getMappingConfigs();

        ConfigLoader.loadGlobalErrorsConfig(false);
    }

    private void mockNoGlobalErrors() throws Exception {
        if (ConfigLoader.getLoadedGlobalErrors() != null) {
            ConfigLoader.getLoadedGlobalErrors().clear();
        }
    }
}

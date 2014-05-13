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

import static org.mockito.BDDMockito.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Rossitsa Borissova
 */
public class AccessTokenValidatorTest {

    AccessTokenValidator validator;

    @BeforeMethod
    public void setup() {
        validator = spy(new AccessTokenValidator());
        AccessTokenValidator.log = mock(Logger.class);
    }

    @Test
    public void when_header_extract_access_token() throws Exception {
        // GIVEN
        String header = "Bearer ab1f63c52d1bb435f1d9c97c805b6a04d62cbbad7f1876038cd9f5362c37ac5a1";

        // WHEN
        String accessToken = AccessTokenValidator.extractAccessToken(header);

        // THEN
        assertEquals(accessToken, "ab1f63c52d1bb435f1d9c97c805b6a04d62cbbad7f1876038cd9f5362c37ac5a1");
    }

    @Test
    public void when_endpoint_scope_same_as_token_scope_retunr_true() throws Exception {
        // GIVEN
        String tokenResponse = "{\"scope\":\"basic\",\"token_type\":\"Bearer\",\"userId\":\"12345\"}";

        // WHEN
        boolean result = AccessTokenValidator.validateTokenScope(tokenResponse, "basic");

        // THEN
        assertTrue(result);
    }

    @Test
    public void when_token_response_extract_token_scope() throws Exception {
        // GIVEN
        String tokenContent = "{\"tokenType\":\"599\",\"scope\":\"basic\"," + "\"accessToken\":\"da96c8141bcda91be65db4adbc8fafe77d116c88caacb8de404c0654c16c6620\","
                + "\"expiresIn\":\"Bearer\",\"userId\":null," + "\"refreshToken\":\"cb2e2e068447913d0c97f79f888f6e2882bfcb569325a9ad9e9b52937b06e547\"}";

        // WHEN
        String tokenScope = AccessTokenValidator.extractTokenScope(tokenContent);

        // THEN
        assertEquals(tokenScope, "basic");
    }

    @Test
    public void when_token_response_does_not_contain_scope_return_null_as_scope() throws Exception {
        // GIVEN
        String tokenContent = "{\"tokenType\":\"599\",\"scope\":" + null + "," + "\"accessToken\":\"da96c8141bcda91be65db4adbc8fafe77d116c88caacb8de404c0654c16c6620\","
                + "\"expiresIn\":\"Bearer\",\"userId\":null," + "\"refreshToken\":\"cb2e2e068447913d0c97f79f888f6e2882bfcb569325a9ad9e9b52937b06e547\"}";

        // WHEN
        String tokenScope = AccessTokenValidator.extractTokenScope(tokenContent);

        // THEN
        assertNull(tokenScope);
    }

    @Test
    public void when_token_response_contain_null_scope_return_null_as_scope() throws Exception {
        // GIVEN
        String tokenContent = "{\"tokenType\":\"599\"," + "\"accessToken\":\"da96c8141bcda91be65db4adbc8fafe77d116c88caacb8de404c0654c16c6620\"," + "\"expiresIn\":\"Bearer\",\"userId\":null,"
                + "\"refreshToken\":\"cb2e2e068447913d0c97f79f888f6e2882bfcb569325a9ad9e9b52937b06e547\"}";

        // WHEN
        String tokenScope = AccessTokenValidator.extractTokenScope(tokenContent);

        // THEN
        assertNull(tokenScope);
    }

    @Test
    public void when_endpoint_scope_contain_token_scope_return_true() throws Exception {
        // GIVEN
        String endpointScope = "extended basic";
        String tokenContent = "{\"tokenType\":\"599\",\"scope\":\"basic\"," + "\"accessToken\":\"da96c8141bcda91be65db4adbc8fafe77d116c88caacb8de404c0654c16c6620\","
                + "\"expiresIn\":\"Bearer\",\"userId\":null," + "\"refreshToken\":\"cb2e2e068447913d0c97f79f888f6e2882bfcb569325a9ad9e9b52937b06e547\"}";

        // WHEN
        boolean result = AccessTokenValidator.validateTokenScope(tokenContent, endpointScope);

        // THEN
        assertTrue(result);
    }

    @Test
    public void when_endpoint_scope_not_contain_token_scope_return_false() throws Exception {
        // GIVEN
        String endpointScope = "extended friends";
        String tokenContent = "{\"tokenType\":\"599\",\"scope\":\"basic\"," + "\"accessToken\":\"da96c8141bcda91be65db4adbc8fafe77d116c88caacb8de404c0654c16c6620\","
                + "\"expiresIn\":\"Bearer\",\"userId\":null," + "\"refreshToken\":\"cb2e2e068447913d0c97f79f888f6e2882bfcb569325a9ad9e9b52937b06e547\"}";

        // WHEN
        boolean result = AccessTokenValidator.validateTokenScope(tokenContent, endpointScope);

        // THEN
        assertFalse(result);
    }

    @Test
    public void when_endpoint_scopes_contain_one_of_token_scopes_return_true() throws Exception {
        // GIVEN
        String endpointScope = "extended friends";
        String tokenContent = "{\"tokenType\":\"599\",\"scope\":\"basic extended\"," + "\"accessToken\":\"da96c8141bcda91be65db4adbc8fafe77d116c88caacb8de404c0654c16c6620\","
                + "\"expiresIn\":\"Bearer\",\"userId\":null," + "\"refreshToken\":\"cb2e2e068447913d0c97f79f888f6e2882bfcb569325a9ad9e9b52937b06e547\"}";

        // WHEN
        boolean result = AccessTokenValidator.validateTokenScope(tokenContent, endpointScope);

        // THEN
        assertTrue(result);
    }

    @Test
    public void when_endpoint_scopes_contain_none_of_token_scopes_return_false() throws Exception {
        // GIVEN
        String endpointScope = "extended friends";
        String tokenContent = "{\"tokenType\":\"599\",\"scope\":\"basic other\"," + "\"accessToken\":\"da96c8141bcda91be65db4adbc8fafe77d116c88caacb8de404c0654c16c6620\","
                + "\"expiresIn\":\"Bearer\",\"userId\":null," + "\"refreshToken\":\"cb2e2e068447913d0c97f79f888f6e2882bfcb569325a9ad9e9b52937b06e547\"}";

        // WHEN
        boolean result = AccessTokenValidator.validateTokenScope(tokenContent, endpointScope);

        // THEN
        assertFalse(result);
    }

    @Test
    public void when_token_scope_is_null_return_false() throws Exception {
        // GIVEN
        String endpointScope = "extended,other";
        String tokenContent = "{\"tokenType\":\"599\"," + "\"accessToken\":\"da96c8141bcda91be65db4adbc8fafe77d116c88caacb8de404c0654c16c6620\"," + "\"expiresIn\":\"Bearer\",\"userId\":null,"
                + "\"refreshToken\":\"cb2e2e068447913d0c97f79f888f6e2882bfcb569325a9ad9e9b52937b06e547\"}";

        // WHEN
        boolean result = AccessTokenValidator.validateTokenScope(tokenContent, endpointScope);

        // THEN
        assertFalse(result);
    }
}

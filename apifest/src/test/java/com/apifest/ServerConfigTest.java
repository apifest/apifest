/*
 * Copyright 2015, ApiFest project
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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * @author rossitsaborissova
 *
 */
public class ServerConfigTest {

    @BeforeTest
    public void setup() {
        ServerConfig.log = mock(Logger.class);
    }

    @Test
    public void when_property_apifest_host_is_empty_use_default_host() throws Exception {
        // GIVEN
        String input = "apifest.host=";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertEquals(ServerConfig.host, ServerConfig.DEFAULT_APIFEST_HOST);
    }

    @Test
    public void when_property_apifest_host_is_null_use_default_host() throws Exception {
        // GIVEN
        String input = "";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertEquals(ServerConfig.host, ServerConfig.DEFAULT_APIFEST_HOST);
    }

    @Test
    public void when_property_apifest_host_is_not_empty_set_the_value_as_host() throws Exception {
        // GIVEN
        String input = "apifest.host=192.168.1.1";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertEquals(ServerConfig.host, "192.168.1.1");
    }

    @Test
    public void when_property_apifest_port_is_empty_use_default_port() throws Exception {
        // GIVEN
        String input = "apifest.port=";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertTrue(ServerConfig.port == ServerConfig.DEFAULT_APIFEST_PORT);
    }

    @Test
    public void when_property_apifest_port_is_null_use_default_port() throws Exception {
        // GIVEN
        String input = "";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertTrue(ServerConfig.port == ServerConfig.DEFAULT_APIFEST_PORT);
    }

    @Test
    public void when_property_apifest_port_is_not_empty_set_the_value_as_port() throws Exception {
        // GIVEN
        String input = "apifest.port=5000";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertTrue(ServerConfig.port == 5000);
    }

    @Test
    public void set_mappings_path_to_property_apifest_mappings() throws Exception {
        // GIVEN
        String input = "apifest.mappings=/home/apifest/mappings";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertEquals(ServerConfig.mappingsPath, "/home/apifest/mappings");
    }

    @Test
    public void set_global_errors_to_property_apifest_global_errors() throws Exception {
        // GIVEN
        String input = "apifest.global-errors=/home/apifest/configs/global-errors.xml";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertEquals(ServerConfig.globalErrors, "/home/apifest/configs/global-errors.xml");
    }

    @Test
    public void when_property_connect_timeout_is_empty_use_default_connect_timeout() throws Exception {
        // GIVEN
        String input = "connect.timeout=";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertTrue(ServerConfig.connectTimeout == ServerConfig.DEFAULT_CONNECT_TIMEOUT);
    }

    @Test
    public void when_property_connect_timeout_is_null_use_default_connect_timeout() throws Exception {
        // GIVEN
        String input = "";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertTrue(ServerConfig.connectTimeout == ServerConfig.DEFAULT_CONNECT_TIMEOUT);
    }

    @Test
    public void when_property_connect_timeout_is_not_empty_use_the_value_as_connect_timeout() throws Exception {
        // GIVEN
        String input = "connect.timeout=5";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertTrue(ServerConfig.connectTimeout == 5);
    }

    @Test
    public void set_custom_jar_path_to_property_custom_jar() throws Exception {
        // GIVEN
        String input = "custom.jar=/home/apifest/configs/custom.jar";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertEquals(ServerConfig.customJarPath, "/home/apifest/configs/custom.jar");
    }

    @Test
    public void when_property_apifest_nodes_is_empty_use_default_host() throws Exception {
        // GIVEN
        String input = "apifest.nodes=";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertEquals(ServerConfig.apifestNodes, ServerConfig.DEFAULT_APIFEST_HOST);
    }

    @Test
    public void when_property_apifest_nodes_is_null_use_default_host() throws Exception {
        // GIVEN
        String input = "";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertEquals(ServerConfig.apifestNodes, ServerConfig.DEFAULT_APIFEST_HOST);
    }

    @Test
    public void when_property_apifest_nodes_is_not_empty_set_the_value_as_apifest_nodes() throws Exception {
        // GIVEN
        String input = "apifest.nodes=192.168.1.1,192.168.1.2";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertEquals(ServerConfig.apifestNodes, "192.168.1.1,192.168.1.2");
    }

    @Test
    public void when_property_hazelcast_pass_is_null_set_hazelcast_default_password() throws Exception {
        // GIVEN
        String input = "";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertEquals(ServerConfig.hazelcastPassword, ServerConfig.DEFAULT_HAZELCAST_PASS);
    }

    @Test
    public void when_property_hazelcast_pass_is_not_null_set_the_value_as_hazelcast_password() throws Exception {
        // GIVEN
        String input = "hazelcast.password=mypass";
        InputStream in = new ByteArrayInputStream(input.getBytes(Charset.forName("UTF-8")));

        // WHEN
        ServerConfig.loadProperties(in);

        // THEN
        assertEquals(ServerConfig.hazelcastPassword, "mypass");
    }

    @Test
    public void when_set_default_configs_use_default_values() throws Exception {
        // WHEN
        ServerConfig.setDefaultConfigs();

        // THEN
        assertEquals(ServerConfig.host, ServerConfig.DEFAULT_APIFEST_HOST);
        assertEquals(ServerConfig.port, ServerConfig.DEFAULT_APIFEST_PORT);
        assertEquals(ServerConfig.connectTimeout, ServerConfig.DEFAULT_CONNECT_TIMEOUT);
        assertEquals(ServerConfig.apifestNodes, ServerConfig.DEFAULT_APIFEST_HOST);
        assertEquals(ServerConfig.hazelcastPassword, ServerConfig.DEFAULT_HAZELCAST_PASS);
    }

}

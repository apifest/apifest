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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialize all server configs.
 *
 * @author Rossitsa Borissova
 */
public final class ServerConfig {

    protected static Logger log = LoggerFactory.getLogger(ServerConfig.class);

    protected static String host;
    protected static Integer port;
    protected static String mappingsPath;
    protected static String tokenValidateEndpoint;
    protected static Integer connectTimeout;
    protected static String customJarPath;

    private ServerConfig() {
    }

    public static void readProperties () throws IOException {
        String propertiesFilePath = System.getProperty("properties.file");
        InputStream in;
        if(propertiesFilePath == null) {
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream("apifest.properties");
            if(in != null) {
                loadProperties(in);
            } else {
                throw new IOException();
            }
        } else {
            File file = new File(propertiesFilePath);
            in = new FileInputStream(file);
            loadProperties(in);
        }
    }

    protected static void loadProperties(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);
        host = props.getProperty("apifest.host", "localhost");
        port = Integer.valueOf(props.getProperty("apifest.port", "8080"));
        mappingsPath = props.getProperty("apifest.mappings");

        tokenValidateEndpoint = props.getProperty("token.validate.endpoint");
        connectTimeout = Integer.valueOf(props.getProperty("connect.timeout", "10"));
        customJarPath = props.getProperty("custom.jar");
    }

    public static String getHost() {
        return host;
    }

    public static Integer getPort() {
        return port;
    }

    public static String getMappingsPath() {
        return mappingsPath;
    }

    public static String getTokenValidateEndpoint() {
        return tokenValidateEndpoint;
    }

    public static Integer getConnectTimeout() {
        return connectTimeout;
    }

    public static String getCustomJarPath() {
        return customJarPath;
    }
}

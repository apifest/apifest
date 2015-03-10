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

    protected static final Integer DEFAULT_CONNECT_TIMEOUT = 10;
    protected static final Integer DEFAULT_APIFEST_PORT = 8080;
    protected static final String DEFAULT_APIFEST_HOST = "localhost";
    protected static final String DEFAULT_HAZELCAST_PASS = "dev-pass";

    protected static Logger log = LoggerFactory.getLogger(ServerConfig.class);

    protected static String host;
    protected static Integer port;
    protected static String mappingsPath;
    protected static String tokenValidateHost;
    protected static Integer tokenValidatePort;
    protected static Integer connectTimeout;
    protected static String customJarPath;
    protected static String apifestNodes;
    protected static String hazelcastPassword;
    protected static String globalErrors;

    private ServerConfig() {
    }

    public static void readProperties() throws IOException {
        String propertiesFilePath = System.getProperty("properties.file");
        InputStream in = null;
        try {
            if (propertiesFilePath == null) {
                in = Thread.currentThread().getContextClassLoader().getResourceAsStream("apifest.properties");
                if (in != null) {
                    loadProperties(in);
                } else {
                    log.warn("No properties file setup, default configs will be used");
                    setDefaultConfigs();
                }
            } else {
                File file = new File(propertiesFilePath);
                in = new FileInputStream(file);
                loadProperties(in);
            }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("cannot close input stream", e);
                }
            }
        }
    }

    protected static void setDefaultConfigs() {
        host = DEFAULT_APIFEST_HOST;
        port = DEFAULT_APIFEST_PORT;
        connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        apifestNodes = DEFAULT_APIFEST_HOST;
        hazelcastPassword = DEFAULT_HAZELCAST_PASS;
    }

    protected static void loadProperties(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);

        host = props.getProperty("apifest.host");
        if (host == null || host.isEmpty()) {
            host = DEFAULT_APIFEST_HOST;
        }

        String portString = props.getProperty("apifest.port");
        if (portString == null || portString.isEmpty()) {
            port = DEFAULT_APIFEST_PORT;
        } else {
            port = Integer.valueOf(portString);
        }

        mappingsPath = props.getProperty("apifest.mappings");
        if (mappingsPath == null || mappingsPath.isEmpty()) {
            log.warn("apifest.mappings property is not defined in properties file");
        }

        globalErrors = props.getProperty("apifest.global-errors");

        tokenValidateHost = props.getProperty("token.validate.host");
        if (tokenValidateHost == null || tokenValidateHost.isEmpty()) {
            log.warn("token.validate.host property is not defined in properties file");
        }

        String tokenValidatePortString = props.getProperty("token.validate.port");
        if (tokenValidatePortString == null || tokenValidatePortString.isEmpty()) {
            log.warn("token.validate.port property is not defined in properties file");
        } else {
            tokenValidatePort = Integer.valueOf(props.getProperty("token.validate.port"));
        }

        String connectTimeoutString = props.getProperty("connect.timeout");
        if (connectTimeoutString == null || connectTimeoutString.isEmpty()) {
            connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        } else {
            connectTimeout = Integer.valueOf(connectTimeoutString);
        }

        customJarPath = props.getProperty("custom.jar");

        apifestNodes = props.getProperty("apifest.nodes");
        if (apifestNodes == null || apifestNodes.isEmpty()) {
            apifestNodes = DEFAULT_APIFEST_HOST;
        }

        // dev-pass is the default password used in Hazelcast
        hazelcastPassword = props.getProperty("hazelcast.password", "dev-pass");
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

    public static String getTokenValidateHost() {
        return tokenValidateHost;
    }

    public static Integer getTokenValidatePort() {
        return tokenValidatePort;
    }

    public static Integer getConnectTimeout() {
        return connectTimeout;
    }

    public static String getCustomJarPath() {
        return customJarPath;
    }

    public static String getApifestNodes() {
        return apifestNodes;
    }

    public static String getHazelcastPassword() {
        return hazelcastPassword;
    }

    public static String getGlobalErrorsFile() {
        return globalErrors;
    }

}

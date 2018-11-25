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

import com.apifest.oauth20.ResourceBundleImpl;
import com.apifest.oauth20.persistence.DBManagerFactory;
import com.apifest.api.ICustomGrantTypeHandler;
import com.apifest.api.IUserAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

/**
 * Initialize all server configs.
 *
 * @author Rossitsa Borissova
 */
public final class ServerConfig {

    public static final Integer DEFAULT_CONNECT_TIMEOUT = 10;
    public static final Integer DEFAULT_APIFEST_PORT = 8181;
    public static final String DEFAULT_APIFEST_HOST = "localhost";
    public static final String DEFAULT_HAZELCAST_PASS = "dev-pass";
    // expires_in in sec for grant type password
    public static final int DEFAULT_PASSWOD_EXPIRES_IN = 900;

    // expires_in in sec for grant type client_credentials
    public static final int DEFAULT_CC_EXPIRES_IN = 1800;

    public static Logger log = LoggerFactory.getLogger(ServerConfig.class);

    public static String host;
    public static Integer port;
    public static String mappingsPath;
    public static Integer connectTimeout;
    public static String customJarPath;
    public static String apifestNodes;
    public static String hazelcastPassword;
    public static String globalErrors;


    //=================

    private static String customJar;
    private static String userAuthClass;
    private static Class<IUserAuthentication> userAuthenticationClass;
    private static String customGrantType;
    private static String customGrantTypeClass;
    private static Class<ICustomGrantTypeHandler> customGrantTypeHandler;
    private static String dbURI;
    private static String database;
    private static String redisSentinels;
    private static String redisMaster;
    private static URLClassLoader jarClassLoader;
    private static String hazelcastClusterPassword;

    private static String cassandraContactPoints;
    public static Integer rateLimitResetTimeinSec = 60;

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

    public static void setDefaultConfigs() {
        host = DEFAULT_APIFEST_HOST;
        port = DEFAULT_APIFEST_PORT;
        connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        apifestNodes = DEFAULT_APIFEST_HOST;
        hazelcastPassword = DEFAULT_HAZELCAST_PASS;
    }

    public static void loadProperties(InputStream in) throws IOException {
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

        customJar = props.getProperty("custom.classes.jar");
        userAuthClass = props.getProperty("user.authenticate.class");
        customGrantType = props.getProperty("custom.grant_type");
        customGrantTypeClass = props.getProperty("custom.grant_type.class");
        database = props.getProperty("oauth20.database", DBManagerFactory.DEFAULT_DB);
        redisSentinels = props.getProperty("redis.sentinels");
        redisMaster = props.getProperty("redis.master");
        dbURI = props.getProperty("db_uri");

        // dev-pass is the default password used in Hazelcast
        hazelcastPassword = props.getProperty("hazelcast.password", "dev-pass");
        hazelcastClusterPassword = props.getProperty("hazelcast.password", "dev-pass");

        cassandraContactPoints = props.getProperty("cassandra.contanctPoints");

        String rateLimitResetTimeString = props.getProperty("rateLimit.reset.time_in_seconds");
        if (rateLimitResetTimeString == null || rateLimitResetTimeString.isEmpty()) {
            log.warn("rateLimit.reset.time_in_seconds property is not defined in properties file");
        } else {
            rateLimitResetTimeinSec = Integer.valueOf(rateLimitResetTimeString);
        }
    }

    @SuppressWarnings("unchecked")
    public static Class<IUserAuthentication> loadCustomUserAuthentication(String className)
            throws ClassNotFoundException {
        Class<IUserAuthentication> result = null;
        try {
            URLClassLoader classLoader = getJarClassLoader();
            if (classLoader != null) {
                Class<?> clazz = classLoader.loadClass(className);
                if (IUserAuthentication.class.isAssignableFrom(clazz)) {
                    result = (Class<IUserAuthentication>) clazz;
                } else {
                    log.error(
                            "user.authentication.class {} does not implement IUserAuthentication interface, default authentication will be used",
                            clazz);
                }
            } else {
                log.error("cannot load custom jar, default authentication will be used");
            }
        } catch (MalformedURLException e) {
            log.error("cannot load custom jar, default authentication will be used");
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static Class<ICustomGrantTypeHandler> loadCustomGrantTypeClass(String className)
            throws ClassNotFoundException {
        Class<ICustomGrantTypeHandler> result = null;
        try {
            URLClassLoader classLoader = getJarClassLoader();
            if (classLoader != null) {
                Class<?> clazz = classLoader.loadClass(className);
                if (ICustomGrantTypeHandler.class.isAssignableFrom(clazz)) {
                    result = (Class<ICustomGrantTypeHandler>) clazz;
                } else {
                    log.error("custom.grant_type.class {} does not implement ICustomGrantTypeHandler interface", clazz);
                }
            } else {
                log.error("cannot load custom jar");
            }
        } catch (MalformedURLException e) {
            log.error("cannot load custom jar");
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
        }
        return result;
    }

    private static URLClassLoader getJarClassLoader() throws MalformedURLException {
        if (jarClassLoader == null) {
            if (customJar != null) {
                File file = new File(customJar);
                if (file.exists()) {
                    URL jarfile = file.toURI().toURL();
                    jarClassLoader = URLClassLoader.newInstance(new URL[] { jarfile },
                            ServerConfig.class.getClassLoader());
                } else {
                    throw new IllegalArgumentException(
                            "check property custom.classes.jar, jar does not exist, default authentication will be used");
                }
            }
        }
        return jarClassLoader;
    }

    public static void loadCustomProperties() {
        Properties properties = new Properties();
        InputStream in = null;
        File file = new File(customJar + ".properties");
        if (file.exists()) {
            try {
                in = new FileInputStream(file);
                properties.load(in);
            } catch (FileNotFoundException e) {
                log.info("Cannot find custom properties file");
            } catch (IOException e) {
                log.error("Error loading custom properties file");
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error("Error closing input stream", e);
                    }
                }
            }
        }
        new ResourceBundleImpl(properties).install();
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


    public static String getDbURI() {
        return dbURI;
    }

    public static String getDatabase() {
        return database;
    }

    public static String getRedisSentinels() {
        return redisSentinels;
    }

    public static String getRedisMaster() {
        return redisMaster;
    }

    public static Class<IUserAuthentication> getUserAuthenticationClass() {
        return userAuthenticationClass;
    }

    public static String getCustomGrantType() {
        return customGrantType;
    }

    public static Class<ICustomGrantTypeHandler> getCustomGrantTypeHandler() {
        return customGrantTypeHandler;
    }

    public static String getHazelcastClusterPassword() {
        return hazelcastClusterPassword;
    }

    public static String getCassandraContactPoints() {
        return cassandraContactPoints;
    }

    public static Integer getRateLimitResetTimeinSec() {
        return rateLimitResetTimeinSec;
    }

}

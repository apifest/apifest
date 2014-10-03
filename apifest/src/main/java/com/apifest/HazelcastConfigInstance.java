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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.config.ExecutorConfig;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.InterfacesConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapConfig.EvictionPolicy;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.MaxSizeConfig.MaxSizePolicy;
import com.hazelcast.config.MulticastConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * Responsible for creating Hazelcast node in JVM. Reads the configuration for Hazelcast maps used to store mapping
 * configuration.
 *
 * @author Rossitsa Borissova
 */
public class HazelcastConfigInstance {

    private static Logger log = LoggerFactory.getLogger(HazelcastConfigInstance.class);

    private HazelcastInstance hzInstance;
    protected static HazelcastConfigInstance configInstance; // NOSONAR, used to mock in unit tests

    private static final String HZ_MAP_NAME = "mappings";
    private static final int MAX_POOL_SIZE = 64;

    private HazelcastConfigInstance() {
    }

    private void load() {
        Config cfg = createConfiguration();
        hzInstance = Hazelcast.newHazelcastInstance(cfg);
        log.debug("Hazelcast instance created");
        ConfigChangeListener listener = new ConfigChangeListener();
        IMap<String, MappingConfig> map = hzInstance.getMap(HZ_MAP_NAME);
        map.addEntryListener(listener, true);
    }

    public static HazelcastConfigInstance instance() {
        if (configInstance == null) {
            configInstance = new HazelcastConfigInstance();
            configInstance.load();
        }
        return configInstance;
    }

    public IMap<String, com.apifest.MappingConfig> getMappingConfigs() {
        return hzInstance.getMap(HZ_MAP_NAME);
    }

    protected Config createConfiguration() {
        Config config = new Config();
        Map<String, MapConfig> mapCfg = createMapConfigs();
        config.setMapConfigs(mapCfg);

        NetworkConfig networkCfg = createNetworkConfigs();
        config.setNetworkConfig(networkCfg);

        ExecutorConfig executorConfig = new ExecutorConfig();
        executorConfig.setPoolSize(MAX_POOL_SIZE);
        executorConfig.setStatisticsEnabled(false);
        config.addExecutorConfig(executorConfig);

        return config;
    }

    private NetworkConfig createNetworkConfigs() {
        NetworkConfig networkConfig = new NetworkConfig();
        InterfacesConfig interfaceConfig = new InterfacesConfig();
        // add current host
        try {
            interfaceConfig.addInterface(InetAddress.getByName(ServerConfig.getHost()).getHostAddress());
        } catch (UnknownHostException e) {
            log.error("cannot create Hazelcast network config", e);
        }
        interfaceConfig.setEnabled(true);

        networkConfig.setInterfaces(interfaceConfig);
        JoinConfig joinConfig = new JoinConfig();
        TcpIpConfig tcpIps = new TcpIpConfig();

        // read members from properties file
        List<String> ips = createNodesList();
        if (ips != null) {
            tcpIps.setMembers(ips);
            joinConfig.setTcpIpConfig(tcpIps);
        }
        tcpIps.setEnabled(true);

        MulticastConfig multicastConfig = new MulticastConfig();
        multicastConfig.setEnabled(false);
        joinConfig.setMulticastConfig(multicastConfig);
        networkConfig.setJoin(joinConfig);

        return networkConfig;
    }

    private List<String> createNodesList() {
        List<String> nodes = null;
        String list = ServerConfig.getApifestNodes();
        if (list != null && list.length() > 0) {
            String [] n = list.split(",");
            nodes = Arrays.asList(n);
        }
        return nodes;
    }

    private Map<String, MapConfig> createMapConfigs() {
        MapConfig mapConfig = new MapConfig(HZ_MAP_NAME);
        mapConfig.setInMemoryFormat(InMemoryFormat.BINARY);
        // REVISIT: backupCount = nodes/2 + 1
        mapConfig.setBackupCount(1);
        mapConfig.setAsyncBackupCount(0);
        mapConfig.setTimeToLiveSeconds(0);
        mapConfig.setMaxIdleSeconds(0);
        mapConfig.setEvictionPolicy(EvictionPolicy.NONE);
        mapConfig.setMaxSizeConfig(new MaxSizeConfig(0, MaxSizePolicy.PER_NODE));
        mapConfig.setEvictionPercentage(0);
        mapConfig.setMergePolicy("com.hazelcast.map.merge.PutIfAbsentMapMergePolicy");
        Map<String, MapConfig> configs = new HashMap<String, MapConfig>();
        configs.put(mapConfig.getName(), mapConfig);
        return configs;
    }
}

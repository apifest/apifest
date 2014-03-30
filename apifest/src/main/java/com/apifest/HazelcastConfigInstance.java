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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * Responsible for creating Hazelcast node in JVM.
 * Reads the configuration for Hazelcast maps used to store mapping configuration.
 *
 * @author Rossitsa Borissova
 */
public class HazelcastConfigInstance {

    private static Logger log = LoggerFactory.getLogger(HazelcastConfigInstance.class);

    private HazelcastInstance hzInstance;
    protected static HazelcastConfigInstance configInstance; // NOSONAR, used to mock in unit tests

    private HazelcastConfigInstance() {
    }

    private void load() {
        String hazelcastConfig = System.getProperty("hazelcast.config.file");
        InputStream xml;
        try {
            xml = new FileInputStream(hazelcastConfig);
            XmlConfigBuilder cfgBuilder = new XmlConfigBuilder(xml);
            Config cfg = cfgBuilder.build();
            log.debug("Hazelcast instance created");
            hzInstance = Hazelcast.newHazelcastInstance(cfg);
            ConfigChangeListener listener = new ConfigChangeListener();
            IMap<String, MappingConfig> map= hzInstance.getMap("mappings");
            map.addEntryListener(listener, true);
        } catch (FileNotFoundException e) {
            log.error("hazelcast.config.file {} not found", hazelcastConfig);
        }
    }

    public static HazelcastConfigInstance instance() {
        if(configInstance == null) {
            configInstance = new HazelcastConfigInstance();
            configInstance.load();
        }
        return configInstance;
    }

    public IMap<String, com.apifest.MappingConfig> getMappingConfigs() {
      return hzInstance.getMap("mappings");
    }

}

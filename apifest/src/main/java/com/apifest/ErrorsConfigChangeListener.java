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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;

/**
 * @author Rossitsa Borissova
 *
 */
public class ErrorsConfigChangeListener implements EntryListener<Integer, String> {

    private static Logger log = LoggerFactory.getLogger(MappingConfigChangeListener.class);

    /*
     * @see com.hazelcast.core.EntryListener#entryAdded(com.hazelcast.core.EntryEvent)
     */
    @Override
    public void entryAdded(EntryEvent<Integer, String> event) {
        log.debug("errors entry added, key {}, value {}", event.getKey(), event.getValue());
        ConfigLoader.updateError(event.getKey(), event.getValue());
    }

    /*
     * @see com.hazelcast.core.EntryListener#entryRemoved(com.hazelcast.core.EntryEvent)
     */
    @Override
    public void entryRemoved(EntryEvent<Integer, String> event) {
        log.debug("errors entry removed, key {}, value {}", event.getKey(), event.getValue());
        ConfigLoader.removeError(event.getKey());
    }

    /*
     * @see com.hazelcast.core.EntryListener#entryUpdated(com.hazelcast.core.EntryEvent)
     */
    @Override
    public void entryUpdated(EntryEvent<Integer, String> event) {
        log.debug("errors entry updated, key {}, value {}", event.getKey(), event.getValue());
        ConfigLoader.updateError(event.getKey(), event.getValue());
    }

    /*
     * @see com.hazelcast.core.EntryListener#entryEvicted(com.hazelcast.core.EntryEvent)
     */
    @Override
    public void entryEvicted(EntryEvent<Integer, String> event) {
        log.debug("errors entry evicted, key {}, value {}", event.getKey(), event.getValue());
        ConfigLoader.removeError(event.getKey());
    }

}

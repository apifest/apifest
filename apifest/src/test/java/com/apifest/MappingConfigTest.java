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

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.apifest.api.MappingEndpoint;
import com.apifest.api.MappingException;

/**
 * @author Rossitsa Borissova
 */
public class MappingConfigTest {

    @Test(expectedExceptions = MappingException.class)
    public void when_merge_and_there_is_conflict_throw_exception() throws Exception {
        // GIVEN
        MappingConfig config = new MappingConfig();
        Map<MappingPattern, MappingEndpoint> mappings = new HashMap<MappingPattern, MappingEndpoint>();
        MappingPattern pattern = new MappingPattern(Pattern.compile("/v0.1/me"), "GET");
        MappingEndpoint endpoint = mock(MappingEndpoint.class);
        mappings.put(pattern, endpoint);
        config.setMappings(mappings);

        MappingConfig newConfig = new MappingConfig();
        Map<MappingPattern, MappingEndpoint> newMappings = new HashMap<MappingPattern, MappingEndpoint>();
        newMappings.put(pattern, endpoint);
        newConfig.setMappings(newMappings);

        // WHEN
        config.mergeConfig(newConfig);
    }

    @Test
    public void when_merge_with_no_conflict_add_new_mappings() throws Exception {
        // GIVEN
        MappingConfig config = new MappingConfig();
        Map<MappingPattern, MappingEndpoint> mappings = new HashMap<MappingPattern, MappingEndpoint>();
        MappingPattern pattern = new MappingPattern(Pattern.compile("/v0.1/me"), "GET");
        MappingEndpoint endpoint = mock(MappingEndpoint.class);
        mappings.put(pattern, endpoint);
        config.setMappings(mappings);

        MappingConfig newConfig = new MappingConfig();
        Map<MappingPattern, MappingEndpoint> newMappings = new HashMap<MappingPattern, MappingEndpoint>();
        MappingPattern newPattern = new MappingPattern(Pattern.compile("/v0.1/me/emails"), "GET");
        newMappings.put(newPattern, endpoint);
        newConfig.setMappings(newMappings);

        // WHEN
        config.mergeConfig(newConfig);

        // THEN
        assertTrue(config.getMappings().size() == 2);
    }

}

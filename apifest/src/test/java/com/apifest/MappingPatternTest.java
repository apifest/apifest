/*
 * Copyright 2014, ApiFest project
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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

/**
 * @author Rossitsa Borissova
 */
public class MappingPatternTest {


    @Test
    public void when_path_and_method_equal_then_MappingPatterns_are_equal() throws Exception {
        // GIVEN
        String path = "/v0.1/me";
        Pattern p1 = Pattern.compile(path);
        MappingPattern pattern1 = new MappingPattern(p1, "GET");
        Pattern p2 = Pattern.compile(path);
        MappingPattern pattern2 = new MappingPattern(p2, "GET");

        // WHEN
        boolean equals = pattern1.equals(pattern2);

        // THEN
        assertTrue(equals);
    }

    @Test
    public void when_path_equal_but_method_different_then_MappingPatterns_are_NOT_equal() throws Exception {
        // GIVEN
        String path = "/v0.1/me";
        Pattern p1 = Pattern.compile(path);
        MappingPattern pattern1 = new MappingPattern(p1, "GET");
        Pattern p2 = Pattern.compile(path);
        MappingPattern pattern2 = new MappingPattern(p2, "POST");

        // WHEN
        boolean equals = pattern1.equals(pattern2);

        // THEN
        assertFalse(equals);
    }

    @Test
    public void when_path_different_and_method_equal_then_MappingPatterns_are_NOT_equal() throws Exception {
        // GIVEN
        String path = "/v0.1/me";
        Pattern p1 = Pattern.compile(path);
        MappingPattern pattern1 = new MappingPattern(p1, "GET");
        Pattern p2 = Pattern.compile(path + "/emails");
        MappingPattern pattern2 = new MappingPattern(p2, "GET");

        // WHEN
        boolean equals = pattern1.equals(pattern2);

        // THEN
        assertFalse(equals);
    }

    @Test
    public void when_map_already_contains_pattern_and_method_then_return_true() throws Exception {
        // GIVEN
        String path = "/v0.1/me";
        Pattern p1 = Pattern.compile(path);
        MappingPattern pattern1 = new MappingPattern(p1, "GET");
        Pattern p2 = Pattern.compile(path);
        MappingPattern pattern2 = new MappingPattern(p2, "GET");

        // WHEN
        Map<MappingPattern, String> map = new HashMap<MappingPattern, String>();
        map.put(pattern1, "someString");

        // THEN
        boolean contained = false;
        if (map.containsKey(pattern2)) {
            contained = true;
        }
        assertTrue(contained);
    }
}

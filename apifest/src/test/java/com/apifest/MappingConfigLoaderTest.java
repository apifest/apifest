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

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

import org.testng.annotations.Test;

import com.apifest.api.MappingException;
import com.apifest.example.AddSenderIdInBodyAction;

/**
 * @author Rossitsa Borissova
 */
public class MappingConfigLoaderTest {

    @Test
    public void when_no_custom_jar_do_not_load_custom_class_and_throw_exception() throws Exception {
        // GIVEN

        // WHEN
        String errorMsg = null;
        try {
            MappingConfigLoader.loadCustomClass(AddSenderIdInBodyAction.class.getCanonicalName());
        } catch (MappingException e) {
            errorMsg = e.getMessage();
        }

        // THEN
        assertEquals(errorMsg, "cannot load custom jar");
    }
}

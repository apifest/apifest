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

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Represents a mapping by a pattern and a HTTP method.
 * Regular expressions are described as Java Regular expressions.
 *
 * @author Rossitsa Borissova
 */
public class MappingPattern implements Serializable {

    private static final long serialVersionUID = -6618067253697563104L;

    private Pattern pattern;
    private String method;

    public MappingPattern() {
    }

    public MappingPattern(Pattern pattern, String method) {
        this.pattern = pattern;
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public String getMethod() {
        return method;
    }
}

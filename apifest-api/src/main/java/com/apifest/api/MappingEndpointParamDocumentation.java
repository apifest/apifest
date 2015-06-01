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

package com.apifest.api;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "endpoint_param_documentation")
public class MappingEndpointParamDocumentation implements Serializable {

    private static final long serialVersionUID = 2055836426063609094L;

    @XmlAttribute(name = "type", required = true)
    private String type;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "required", required = true)
    private boolean required;

    public MappingEndpointParamDocumentation() {
    }

    public MappingEndpointParamDocumentation(String name, String type, boolean required) {
        this.setName(name);
        this.setType(type);
        this.setRequired(required);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
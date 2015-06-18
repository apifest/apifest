/*
 * Copyright 2013-2015, ApiFest project
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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A wrapper type that holds all the documentation for the mappings.
 * @author Ivan Georgiev
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mappings_docs")
public class MappingDocumentation implements Serializable {

    private static final long serialVersionUID = 8027603159873624488L;

    @XmlAttribute(name = "version", required = true)
    private String version;

    @XmlElement(name = "endpoints", type = MappingEndpointDocumentation.class, required = true)
    private List<MappingEndpointDocumentation> mappingEndpointDocumentation;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public MappingDocumentation() {
        this.setMappingEndpointDocumentation(new ArrayList<MappingEndpointDocumentation>());
    }

    public List<MappingEndpointDocumentation> getMappingEndpiontDocumentation() {
        return mappingEndpointDocumentation;
    }

    public void setMappingEndpointDocumentation(List<MappingEndpointDocumentation> mappingEndpointDocumentation) {
        this.mappingEndpointDocumentation = mappingEndpointDocumentation;
    }
}

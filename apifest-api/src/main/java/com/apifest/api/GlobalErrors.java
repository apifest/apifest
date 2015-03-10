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

package com.apifest.api;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents custom errors that are not related to a mapping configuration.
 * For example, when a request is not mapped in mapping xml files then error response with content {"error":"Not found"} and HTTP 404 status will be returned.
 * The response error message could be customized in the following way in global error XML file:
 * <error status="404" message='{"error":"resource not found"}'/>
 *
 * @author Rossitsa Borissova
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="global-errors")
public class GlobalErrors implements Serializable {

    private static final long serialVersionUID = 1429025245719263465L;

    @XmlElement(name = "error", type = MappingError.class, required = true)
    private List<MappingError> errors;

    public void setErrors(List<MappingError> errors) {
        this.errors = errors;
    }

    public List<MappingError> getErrors() {
        return errors;
    }

}

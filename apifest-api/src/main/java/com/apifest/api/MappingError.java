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

/**
 * Represents a customized error response.
 * The error mapping is defined by status (HTTP statuse code) and message - customized response.
 * Note, that error mappings work only for HTTP status equals or above HTTP status 300.
 * @author Rossitsa Borissova
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "error")
public class MappingError implements Serializable {

    private static final long serialVersionUID = 3349722297997373619L;

    @XmlAttribute(name = "status", required = true)
    private String status;

    @XmlAttribute(name = "message", required = true)
    private String message;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

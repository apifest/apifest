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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Represents a mapping with its external and internal endpoints, HTTP method, actions, filters
 * and regular expressions used.
 * @author Rossitsa Borissova
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "endpoint")
public class MappingEndpoint implements  Serializable {

    private static final long serialVersionUID = -1013670420032412311L;

    // TODO: make it Int
    @XmlAttribute(name = "backendPort", required = false)
    private Integer backendPort;

    @XmlAttribute(name = "backendHost", required = false)
    private String backendHost;

    @XmlAttribute(name = "authRequired", required = false)
    private String authRequired;

    @XmlAttribute(name = "scope", required = false)
    private String scope;

    @XmlAttribute(name = "method", required = true)
    private String method;

    @XmlAttribute(name = "internal", required = true)
    private String internalEndpoint;

    @XmlAttribute(name = "external", required = true)
    private String externalEndpoint;

    @XmlElement(name = "action", type = MappingAction.class)
    private List<MappingAction> actions;

    @XmlElement(name = "filter", type = ResponseFilter.class)
    private List<ResponseFilter> filters;

    // Do we need a support for multiple vars?
    @XmlAttribute(name = "varExpression")
    private String varExpression;

    @XmlAttribute(name = "varName")
    private String varName;

    public MappingEndpoint() {
    }

    public MappingEndpoint(String external, String internal, String method, String authRequired, String scope,
            List<MappingAction> actions, List<ResponseFilter> filters, String varExpr, String varName,
            String backendHost, Integer backendPort) {
        this.externalEndpoint = external;
        this.internalEndpoint = internal;
        this.method = method;
        this.authRequired = authRequired;
        this.scope = scope;
        this.actions = actions;
        this.filters = filters;
        this.varExpression = varExpr;
        this.varName = varName;
        this.backendHost = backendHost;
        this.backendPort = backendPort;
    }

    public String getExternalEndpoint() {
        return externalEndpoint;
    }

    public void setExternalEndpoint(String externalEndpoint) {
        this.externalEndpoint = externalEndpoint;
    }

    public String getInternalEndpoint() {
        return internalEndpoint;
    }

    public void setInternalEndpoint(String internalEndpoint) {
        this.internalEndpoint = internalEndpoint;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getAuthRequired() {
        return authRequired;
    }

    public void setAuthRequired(String authRequired) {
        this.authRequired = authRequired;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<MappingAction> getActions() {
        return actions;
    }

    public void setActions(List<MappingAction>actions) {
        this.actions = actions;
    }

    public List<ResponseFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<ResponseFilter> filters) {
        this.filters = filters;
    }

    public String getVarExpression() {
        return varExpression;
    }

    public void setVarExpression(String varExpression) {
        this.varExpression = varExpression;
    }

    public String getVarName() {
        return varName;
    }

    public void setVarName(String varName) {
        this.varName = varName;
    }

    public Integer getBackendPort() {
        return backendPort;
    }

    public void setBackendPort(Integer backendPort) {
        this.backendPort = backendPort;
    }

    public String getBackendHost() {
        return backendHost;
    }

    public void setBackendHost(String backendHost) {
        this.backendHost = backendHost;
    }

    /**
     * Unique mapping endpoint key is constructed from mapping endpoint method and mapping endpoint external endpoint.
     * @return mapping endpoint unique key
     */
    public String getUniqueKey() {
        return getMethod() + getExternalEndpoint();
    }
}

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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Represents an endpoint mapping - with its endpoint mapping, actions, filters and errors.
 * @author Rossitsa Borissova
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mappings")
public class Mapping implements  Serializable {

    private static final long serialVersionUID = 6087428073230165216L;

    @XmlAttribute(name = "version")
    private String version;

    @XmlElement(name = "actions", type = ActionsWrapper.class)
    private ActionsWrapper actionsWrapper;

    @XmlElement(name = "filters", type = FiltersWrapper.class)
    private FiltersWrapper filtersWrapper;

    @XmlElement(name = "backend", type = Backend.class)
    private Backend backend;

    @XmlElement(name = "endpoints", type = EndpointsWrapper.class, required = true)
    private EndpointsWrapper endpointsWrapper;

    @XmlElement(name = "errors", type = ErrorsWrapper.class)
    private ErrorsWrapper errorsWrapper;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ActionsWrapper getActionsWrapper() {
        return actionsWrapper;
    }

    public void setActionsWrapper(ActionsWrapper actionsWrapper) {
        this.actionsWrapper = actionsWrapper;
    }

    public FiltersWrapper getFiltersWrapper() {
        return filtersWrapper;
    }

    public void setFiltersWrapper(FiltersWrapper filtersWrapper) {
        this.filtersWrapper = filtersWrapper;
    }

    public Backend getBackend() {
        return backend;
    }

    public void setBackend(Backend backend) {
        this.backend = backend;
    }

    public EndpointsWrapper getEndpointsWrapper() {
        return endpointsWrapper;
    }

    public void setEndpointsWrapper(EndpointsWrapper endpointsWrapper) {
        this.endpointsWrapper = endpointsWrapper;
    }

    public void setErrorsWrapper(ErrorsWrapper errorsWrapper) {
        this.errorsWrapper = errorsWrapper;
    }

    public ErrorsWrapper getErrorsWrapper() {
        return errorsWrapper;
    }


    @XmlType
    public static class EndpointsWrapper implements Serializable {

        private static final long serialVersionUID = -3862159327816900857L;

        private List<MappingEndpoint> endpoints;

        @XmlElement(name = "endpoint", type = MappingEndpoint.class)
        public List<MappingEndpoint> getEndpoints() {
            return endpoints;
        }

        public void setEndpoints(List<MappingEndpoint> endpoints) {
            this.endpoints = endpoints;
        }
    }

    @XmlType
    public static class ActionsWrapper implements Serializable {

        private static final long serialVersionUID = 5664375571203867423L;

        private List<MappingAction> actions;

        public void setActions(List<MappingAction> actions) {
            this.actions = actions;
        }

        @XmlElement(name = "action", type = MappingAction.class)
        public List<MappingAction> getActions() {
            return actions;
        }
    }

    @XmlType
    public static class FiltersWrapper implements Serializable {

        private static final long serialVersionUID = 4138231375881528582L;

        private List<ResponseFilter> filters;

        public void setFilters(List<ResponseFilter> filters) {
            this.filters = filters;
        }

        @XmlElement(name = "filter", type = ResponseFilter.class)
        public List<ResponseFilter> getFilters() {
            return filters;
        }
    }

    @XmlType
    public static class Backend implements Serializable {

        private static final long serialVersionUID = -629932484949029609L;

        private String backendHost;
        private Integer backendPort;

        public Backend() {
        }

        public Backend(String backendHost, Integer backendPort) {
            this.backendHost = backendHost;
            this.backendPort = backendPort;
        }

        @XmlAttribute(name = "host")
        public String getBackendHost() {
            return backendHost;
        }

        public void setBackendHost(String backendHost) {
            this.backendHost = backendHost;
        }

        @XmlAttribute(name = "port")
        public Integer getBackendPort() {
            return backendPort;
        }

        public void setBackendPort(Integer backendPort) {
            this.backendPort = backendPort;
        }
    }

    @XmlType
    public static class ErrorsWrapper implements Serializable {

        private static final long serialVersionUID = -316718601740882695L;

        private List<MappingError> errors;

        public void setErrors(List<MappingError> errors) {
            this.errors = errors;
        }

        @XmlElement(name = "error", type = MappingError.class)
        public List<MappingError> getErrors() {
            return errors;
        }
    }
}

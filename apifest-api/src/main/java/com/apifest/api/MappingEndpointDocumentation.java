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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.apifest.api.params.ExceptionDocumentation;
import com.apifest.api.params.RequestParamDocumentation;
import com.apifest.api.params.ResultParamDocumentation;

/**
 * A wrapper type that holds all the documentation an endpoint.
 * @author Ivan Georgiev
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "endpoint_documentation")
public class MappingEndpointDocumentation implements Serializable {

    private static final long serialVersionUID = 4130570229846556297L;

    @XmlAttribute(name = "scope", required = false)
    private String scope;

    @XmlAttribute(name = "method", required = true)
    private String method;

    @XmlAttribute(name = "endpoint", required = true)
    private String endpoint;

    @XmlAttribute(name = "description", required = true)
    private String description;

    @XmlAttribute(name = "paramsDescription", required = true)
    private String paramsDescription;

    @XmlAttribute(name = "resultsDescription", required = true)
    private String resultsDescription;

    @XmlAttribute(name = "summary", required = true)
    private String summary;

    @XmlAttribute(name = "group", required = true)
    private String group;

    @XmlTransient
    private int order;

    @XmlElement(name = "requestParams", type = RequestParamDocumentation.class)
    private List<RequestParamDocumentation> requestParamsDocumentation;

    @XmlElement(name = "resultParams", type = RequestParamDocumentation.class)
    private List<ResultParamDocumentation> resultParamsDocumentation;

    @XmlElement(name = "exceptions", type = ExceptionDocumentation.class)
    private List<ExceptionDocumentation> exceptionsDocumentation;

    @XmlElement(name="exampleRequest")
    private String exampleRequest;

    @XmlElement(name="exampleResult")
    private String exampleResult;

    @XmlTransient
    private boolean hidden;

    public MappingEndpointDocumentation() {
        this.requestParamsDocumentation = new ArrayList<RequestParamDocumentation>();
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public List<RequestParamDocumentation> getRequestParamsDocumentation()
    {
        return requestParamsDocumentation;
    }

    public void setRequestParamsDocumentation(List<RequestParamDocumentation> requestParamsDocumentation)
    {
        this.requestParamsDocumentation = requestParamsDocumentation;
    }

    public List<ResultParamDocumentation> getResultParamsDocumentation()
    {
        return resultParamsDocumentation;
    }

    public void setResultParamsDocumentation(List<ResultParamDocumentation> resultParamsDocumentation)
    {
        this.resultParamsDocumentation = resultParamsDocumentation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParamsDescription()
    {
        return paramsDescription;
    }

    public void setParamsDescription(String paramsDescription)
    {
        this.paramsDescription = paramsDescription;
    }

    public String getResultsDescription()
    {
        return resultsDescription;
    }

    public void setResultsDescription(String resultsDescription)
    {
        this.resultsDescription = resultsDescription;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getOrder()
    {
        return order;
    }

    public void setOrder(int order)
    {
        this.order = order;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public List<ExceptionDocumentation> getExceptionsDocumentation()
    {
        return exceptionsDocumentation;
    }

    public void setExceptionsDocumentation(List<ExceptionDocumentation> exceptionsDocumentation)
    {
        this.exceptionsDocumentation = exceptionsDocumentation;
    }

    public String getExampleRequest()
    {
        return exampleRequest;
    }

    public void setExampleRequest(String exampleRequest)
    {
        this.exampleRequest = exampleRequest;
    }

    public String getExampleResult()
    {
        return exampleResult;
    }

    public void setExampleResult(String exampleResult)
    {
        this.exampleResult = exampleResult;
    }
}

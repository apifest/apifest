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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "endpoint_documentation")
public class MappingEndpointDocumentation implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "scope", required = false)
    private String scope;

    @XmlAttribute(name = "method", required = true)
    private String method;

    @XmlAttribute(name = "endpoint", required = true)
    private String endpoint;

    @XmlAttribute(name = "description", required = true)
    private String description;

    @XmlAttribute(name = "summary", required = true)
    private String summary;

    @XmlAttribute(name = "group", required = true)
    private String group;

    @XmlElement(name = "params", type = MappingEndpointParamDocumentation.class)
    private List<MappingEndpointParamDocumentation> mappingEndpontParamsDocumentation;

    @XmlTransient
    private boolean isHidden;

    public MappingEndpointDocumentation() {
        this.mappingEndpontParamsDocumentation = new ArrayList<MappingEndpointParamDocumentation>();
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

    public List<MappingEndpointParamDocumentation> getMappingEndpontParamsDocumentation() {
        return mappingEndpontParamsDocumentation;
    }

    public void setMappingEndpontParamsDocumentation(List<MappingEndpointParamDocumentation> mappingEndpontParamsDocumentation) {
        this.mappingEndpontParamsDocumentation = mappingEndpontParamsDocumentation;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public boolean isHidden()
    {
        return isHidden;
    }

    public void setHidden(boolean isHidden)
    {
        this.isHidden = isHidden;
    }
}

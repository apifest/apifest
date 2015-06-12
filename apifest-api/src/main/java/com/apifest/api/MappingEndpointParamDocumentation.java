package com.apifest.api;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "endpoint_param_documentation")
public class MappingEndpointParamDocumentation {
    private static final long serialVersionUID = 1L;

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

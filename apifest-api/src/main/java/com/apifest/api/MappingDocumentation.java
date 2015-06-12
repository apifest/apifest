package com.apifest.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mappings_docs")
public class MappingDocumentation implements Serializable {

    private static final long serialVersionUID = 1L;

    @XmlAttribute(name = "version", required = true)
    private String version;

    @XmlElement(name = "endpoints", type = MappingEndpointDocumentation.class, required = true)
    private List<MappingEndpointDocumentation> mappingEndpontDocumentation;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public MappingDocumentation() {
        this.setMappingEndpontDocumentation(new ArrayList<MappingEndpointDocumentation>());
    }

    public List<MappingEndpointDocumentation> getMappingEndpontDocumentation() {
        return mappingEndpontDocumentation;
    }

    public void setMappingEndpontDocumentation(List<MappingEndpointDocumentation> mappingEndpontDocumentation) {
        this.mappingEndpontDocumentation = mappingEndpontDocumentation;
    }
}

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

package com.apifest.api.params;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;


/**
 * A POJO which holds the information for expected errors or exceptions when invoking the API.
 * @author Martin Boyanov
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ExceptionDocumentation implements Serializable
{

    private static final long serialVersionUID = 3886830353468804316L;

    @XmlAttribute(name = "name", required = true)
    private String name;

    @XmlAttribute(name = "condition", required = true)
    private String condition;

    @XmlAttribute(name = "description", required = true)
    private String description;

    @XmlAttribute(name = "code", required = true)
    private Integer code;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Integer getCode()
    {
        return code;
    }

    public void setCode(Integer code)
    {
        this.code = code;
    }
}

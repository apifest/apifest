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

package com.apifest;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.api.BasicAction;
import com.apifest.api.BasicFilter;
import com.apifest.api.MappingAction;
import com.apifest.api.MappingEndpoint;
import com.apifest.api.ResponseFilter;

/**
 * Holds the configuration for a mapping version.
 *
 * @author Rossitsa Borissova
 */
public class MappingConfig implements Serializable {

    private static final long serialVersionUID = 5553036783446791217L;

    private static Logger log = LoggerFactory.getLogger(MappingConfig.class);

    private Map<MappingPattern, MappingEndpoint> mappings =  new HashMap<MappingPattern, MappingEndpoint>();

    private Map<String, String> actions = new HashMap<String, String>();

    private Map<String, String> filters = new HashMap<String, String>();

    private Map<String, String> errors = new HashMap<String, String>();

    public void setMappings(Map<MappingPattern, MappingEndpoint> mappings) {
        this.mappings = mappings;
    }

    public void setActions(Map<String, String> actions) {
        this.actions = actions;
    }

    public void setFilters(Map<String, String> filters) {
        this.filters = filters;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    protected Map<MappingPattern, MappingEndpoint> getMappings() {
        return mappings;
    }

    protected Map<String, String> getActions() {
        return actions;
    }

    protected Map<String, String> getFilters() {
        return filters;
    }

    protected Map<String, String> getErrors() {
        return errors;
    }

    public String getErrorMessage(int code) {
        if (getErrors().get(String.valueOf(code)) != null) {
            return getErrors().get(String.valueOf(code));
        }
        return null;
    }

    /**
     * Returns MappingEndpoint corresponding to an URI.
     * @param uri external uri
     * @return <code>MappingEndpoint</code> that corresponds to the passed URI
     */
    public MappingEndpoint getMappingEndpoint(String uri, String method) {
        String rawUri = getUriWithoutParams(uri);

        Set<MappingPattern> patterns = getMappings().keySet();
        for (MappingPattern p : patterns) {
            Matcher m = p.getPattern().matcher(rawUri.trim());
            if (m.find() && p.getMethod().equals(method)) {
                MappingEndpoint cur = getMappings().get(p);
                String newUri = cur.getInternalEndpoint();
                // when RE used
                if(newUri.contains("{") && m.groupCount() >= 1) {
                    String varValue = m.group(1);
                    newUri = cur.getInternalEndpoint().replace("{" + cur.getVarName() + "}", varValue);
                }
                MappingEndpoint result = new MappingEndpoint(cur.getExternalEndpoint(), newUri, cur.getMethod(), cur.getAuthRequired(),
                        cur.getScope(), cur.getActions(), cur.getFilters(), cur.getVarExpression(), cur.getVarName(),
                        cur.getBackendHost(), cur.getBackendPort());
                return result;
            }
        }
        return null;
    }

    private String getUriWithoutParams(String uri) {
        String rawUri = uri;
        try {
            URI u = new URI(uri);
            rawUri = u.getRawPath();
        } catch (URISyntaxException e) {
            log.error("URI syntax is not valid, {}", uri);
        }
        return rawUri;
    }

    /**
     * Returns instance of BasicAction that corresponds to the passed mappingAction.
     * @param mappingAction <code>MappingAction</code>
     * @return instance of <code>BasicAction</code>
     */
    public BasicAction getAction(MappingAction mappingAction) throws MappingException {
        BasicAction action = null;
        String actionClass;
        if (mappingAction.getActionClassName() != null) {
            actionClass = mappingAction.getActionClassName();
        } else {
            actionClass = getActions().get(mappingAction.getName());
        }
        if(actionClass == null) {
            throw new MappingException("action " + mappingAction.getName() + " not mapped to class");
        }
        Class<?> clazz;
        try {
            // load class from custom jar
            clazz = MappingConfigLoader.loadCustomClass(actionClass);
        } catch (ClassNotFoundException e) {
            try {
                clazz = Class.forName(actionClass);
            } catch (ClassNotFoundException e1) {
                throw new MappingException("cannot instantiate action class " + actionClass, e1);
            }
        }
        try {
            action = (BasicAction) clazz.newInstance();
        } catch (InstantiationException e) {
            throw new MappingException("cannot instantiate action class " + actionClass, e);
        } catch (IllegalAccessException e) {
            throw new MappingException("cannot instantiate action class " + actionClass, e);
        }
        return action;
    }

    /**
     * Returns instance of BasicFilter that corresponds to the response filter.
     * @param responseFilter response filter
     * @return BasicFilter instance
     */
    public BasicFilter getFilter(ResponseFilter responseFilter) throws MappingException {
        BasicFilter filter = null;
        String filterClass;
        if (responseFilter.getFilterClassName() != null) {
            filterClass = responseFilter.getFilterClassName();
        } else {
            filterClass = getFilters().get(responseFilter.getName());
        }
        if(filterClass == null) {
            throw new MappingException("filter " + responseFilter.getName() + " not mapped to class");
        }
        Class<?> clazz;
        try {
            // load class from custom jar
            clazz = MappingConfigLoader.loadCustomClass(filterClass);
        } catch (ClassNotFoundException e) {
            try {
                clazz = Class.forName(filterClass);
            } catch (ClassNotFoundException e1) {
                throw new MappingException("cannot instantiate filter class " + filterClass, e1);
            }
        }

        try{
            filter = (BasicFilter) clazz.newInstance();
        } catch (InstantiationException e) {
            throw new MappingException("cannot instantiate filter class " + filterClass, e);
        } catch (IllegalAccessException e) {
            throw new MappingException("cannot instantiate filter class " + filterClass, e);
        }
        return filter;
    }

}

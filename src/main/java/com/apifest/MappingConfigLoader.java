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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads/reloads all mapping configurations.
 *
 * @author Rossitsa Borissova
 */
public final class MappingConfigLoader {

    private static Logger log = LoggerFactory.getLogger(MappingConfigLoader.class);

    private MappingConfigLoader() {
    }

    protected static void load() {
        String mappingFileDir = ServerConfig.getMappingsPath();
        MappingConfig config = new MappingConfig();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Mapping.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            File configFile = new File(mappingFileDir);
            Mapping configs = (Mapping) unmarshaller.unmarshal(configFile);

            config.setActions(getActionsMap(configs));

            if (configs.getFiltersWrapper() != null) {
                config.setFilters(getFiltersMap(configs));
            }

            if (configs.getErrorsWrapper() != null) {
                config.setErrors(getErrorsMap(configs));
            }

            List<MappingEndpoint> mappingEndpoints = configs.getEndpointsWrapper().getEndpoints();
            config.setMappings(getMappingsMap(mappingEndpoints));

            // TODO: Create different Config objects for each supported version
            Map<String, MappingConfig> map = getHazelcastConfig();
            map.put("v0.1", config);
        } catch (JAXBException e) {
            log.error("Cannot load mapping configuration, file {}", mappingFileDir);
            throw new IllegalArgumentException(e);
        }
    }


    public static MappingConfig getConfig() {
        Map<String, MappingConfig> map = HazelcastConfigInstance.instance().getMappingConfigs();
        return map.get("v0.1");
    }

    private static Map<String, String> getActionsMap(Mapping configs) {
        List<MappingAction> mappingActions = configs.getActionsWrapper().getActions();
        Map<String, String> actions = new HashMap<String, String>();
        for (MappingAction action : mappingActions) {
            actions.put(action.getName(), action.getActionClassName());
        }
        return actions;
    }

    private static Map<String, String> getFiltersMap(Mapping configs) {
        List<ResponseFilter> responseFilters = configs.getFiltersWrapper().getFilters();
        Map<String, String> filters = new HashMap<String, String>();
        for (ResponseFilter filter : responseFilters) {
            filters.put(filter.getName(), filter.getFilterClassName());
        }
        return filters;
    }

    private static Map<String, String> getErrorsMap(Mapping configs) {
        List<MappingError> mappingErrors = configs.getErrorsWrapper().getErrors();
        Map<String, String> errors = new HashMap<String, String>();
        for (MappingError error : mappingErrors) {
            errors.put(error.getStatus(), error.getMessage());
        }
        return errors;
    }

    protected static Map<MappingPattern, MappingEndpoint> getMappingsMap(List<MappingEndpoint> mappingEndpoints) {
        Map<MappingPattern, MappingEndpoint> mappings = new HashMap<MappingPattern, MappingEndpoint>();
        for (MappingEndpoint endpoint : mappingEndpoints) {
            // construct regular expression
            Pattern p = constructPattern(endpoint);
            MappingPattern pattern = new MappingPattern(p, endpoint.getMethod());
            mappings.put(pattern, endpoint);
        }
        return mappings;
    }

    protected static Pattern constructPattern(MappingEndpoint endpoint) {
        String path = endpoint.getExternalEndpoint();
        if (endpoint.getExternalEndpoint().contains("{")){
            String varExpr = "(" + endpoint.getVarExpression() + ")";
            String varName = "{" + endpoint.getVarName() + "}";
            path = path.replace(varName, varExpr);
            return Pattern.compile(path);
        } else {
            // add $ for regular expressions - for no RE path
            return Pattern.compile(path + "$");
        }
    }

    protected static Map<String, MappingConfig> getHazelcastConfig() {
        return HazelcastConfigInstance.instance().getMappingConfigs();
    }

    /**
     * Reloads all mapping configs.
     */
    public static void reloadConfigs() {
        // TODO: Think about how to switch between old and new version of the mapping configs (use version?)
        load();
    }
}

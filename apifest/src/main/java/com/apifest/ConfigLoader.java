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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.apifest.api.GlobalErrors;
import com.apifest.api.Mapping;
import com.apifest.api.Mapping.Backend;
import com.apifest.api.MappingAction;
import com.apifest.api.MappingEndpoint;
import com.apifest.api.MappingError;
import com.apifest.api.MappingException;
import com.apifest.api.ResponseFilter;
import com.hazelcast.core.IMap;

/**
 * Loads/reloads all mapping and global errors configurations.
 *
 * @author Rossitsa Borissova
 */
public final class ConfigLoader {

    private static Logger log = LoggerFactory.getLogger(ConfigLoader.class);
    private static final String END = "$";

    private static final String VAR_NAME_FORMAT = "{%s}";
    private static final String VAR_EXPRESSION_FORMAT = "(%s)%s";

    protected static URLClassLoader jarClassLoader;

    private static Map<String, MappingConfig> localMappingConfigMap = new HashMap<String, MappingConfig>();
    private static Map<Integer, String> localGlobalErrorsMap = new HashMap<Integer, String>();

    private ConfigLoader() {
    }

    protected static void loadMappingsConfig(boolean reload) throws MappingException {
        String mappingFileDir = ServerConfig.getMappingsPath();
        if (ServerConfig.getMappingsPath() != null && !ServerConfig.getMappingsPath().isEmpty()) {
            Map<String, MappingConfig> local = new HashMap<String, MappingConfig>();
            try {
                JAXBContext jaxbContext = JAXBContext.newInstance(Mapping.class);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                File configPath = new File(mappingFileDir);
                if (configPath.isDirectory()) {
                    File[] files = configPath.listFiles();
                    // load all config files
                    for (File mappingFile : files) {
                        if (!mappingFile.isFile() || !mappingFile.getName().endsWith(".xml")) {
                            continue;
                        }
                        // whether the mappings configuration file is valid against the schema
                        if (!MappingConfigValidator.validate(mappingFile)) {
                            throw new MappingException("{\"error\":\"mappings configuration file " + mappingFile.getName() + " is not valid against schema\"}");
                        }
                        MappingConfig config = new MappingConfig();
                        Mapping mappings = (Mapping) unmarshaller.unmarshal(mappingFile);

                        List<MappingEndpoint> mappingEndpoints = mappings.getEndpointsWrapper().getEndpoints();
                        config.setMappings(getMappingsMap(mappingEndpoints, mappings.getBackend()));

                        setConfigActions(config, mappings);

                        setConfigFilters(config, mappings);

                        if (mappings.getErrorsWrapper() != null) {
                            config.setErrors(getErrorsMap(mappings));
                        }
                        // load all actions and filters
                        if (ServerConfig.getCustomJarPath() != null && ServerConfig.getCustomJarPath().length() > 0) {
                            try {
                                loadCustomClasses(config.getActions().values());
                            } catch (MalformedURLException e) {
                                log.error("Cannot load custom jar file", e);
                                throw new IllegalArgumentException(e);
                            }
                        }
                        MappingConfig currentConfig = local.get(mappings.getVersion());
                        if (currentConfig != null) {
                            MappingConfig mergedConfig = currentConfig.mergeConfig(config);
                            local.put(mappings.getVersion(), mergedConfig);
                        } else {
                            local.put(mappings.getVersion(), config);
                        }
                    }
                    IMap<String, MappingConfig> map = getHazelcastConfig();
                    if (reload) {
                        //clear all keys one by one in order to fire events in Hazelcast
                        for (String key : map.keySet()) {
                            map.remove(key);
                        }
                    }
                    if (local.size() > 0) {
                        map.putAll(local);
                        local.putAll(map);
                    } else {
                        local.putAll(map);
                    }
                    localMappingConfigMap = local;
                } else {
                    throw new MappingException("Cannot load mapping configuration from directory " + mappingFileDir);
                }
            } catch (JAXBException e) {
                String errorMessage = e.getMessage();
                if (errorMessage == null && e.getLinkedException() != null) {
                    errorMessage = e.getLinkedException().getMessage();
                }
                throw new MappingException(errorMessage, e);
            }
        }
    }

    protected static void loadGlobalErrorsConfig(boolean reload) throws MappingException {
        String globalErrorsFile = ServerConfig.getGlobalErrorsFile();
        if (globalErrorsFile != null && !globalErrorsFile.isEmpty()) {
            File errorsFile = new File(globalErrorsFile);
            Map<Integer, String> errors = new HashMap<Integer, String>();
            if (errorsFile.isFile() && errorsFile.getName().endsWith(".xml")) {
                try {
                    // whether the global errors configuration file is valid against the schema
                    if (!GlobalErrorsConfigValidator.validate(errorsFile)) {
                        throw new MappingException("{\"error\":\"global errors configuration file " + errorsFile.getName() + " is not valid against schema\"}");
                    }
                    JAXBContext jaxbContext = JAXBContext.newInstance(GlobalErrors.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    GlobalErrors globalErrors = (GlobalErrors) unmarshaller.unmarshal(errorsFile);

                    for (MappingError error : globalErrors.getErrors()) {
                        errors.put(Integer.valueOf(error.getStatus()), error.getMessage());
                    }

                    IMap<Integer, String> errorsMap = getHazelcastErrorsConfig();
                    if (reload) {
                        //clear all keys one by one in order to fire events in Hazelcast
                        for (Integer key : errorsMap.keySet()) {
                            errorsMap.remove(key);
                        }
                    }
                    if (errors.size() > 0) {
                        errorsMap.putAll(errors);
                    } else {
                        errors.putAll(errorsMap);
                    }
                    localGlobalErrorsMap = errors;
                } catch (JAXBException e) {
                    String errorMessage = e.getMessage();
                    if (errorMessage == null && e.getLinkedException() != null) {
                        errorMessage = e.getLinkedException().getMessage();
                    }
                    throw new MappingException(errorMessage, e);
                }
            } else {
                throw new MappingException("Cannot load global errors configuration from directory " + globalErrorsFile);
            }
        }
    }

    protected static void setConfigFilters(MappingConfig config, Mapping mappings) {
        if (mappings.getFiltersWrapper() != null) {
            config.setFilters(getFiltersMap(mappings));
        } else {
            // search for filters per endpoint
            Map<String, String> filters = new HashMap<String, String>();
            Map<MappingPattern, MappingEndpoint> maps = config.getMappings();
            for(MappingEndpoint endpoint : maps.values()) {
                if (endpoint.getFilter() != null) {
                    ResponseFilter filter = endpoint.getFilter();
                    String filterName = filter.getName();
                    if (filterName == null) {
                        filterName = filter.getFilterClassName();
                    }
                    filters.put(filterName, filter.getFilterClassName());
                }
            }
            if (filters.size() > 0) {
                config.setFilters(filters);
            }
        }
    }

    protected static void setConfigActions(MappingConfig config, Mapping mappings) {
        if (mappings.getActionsWrapper() != null) {
            config.setActions(getActionsMap(mappings));
        } else {
            // search for actions per endpoint
            Map<String, String> actions = new HashMap<String, String>();
            Map<MappingPattern, MappingEndpoint> maps = config.getMappings();
            for(MappingEndpoint endpoint : maps.values()) {
                if (endpoint.getAction() != null) {
                    MappingAction action = endpoint.getAction();
                    String actionName = action.getName();
                    if (actionName == null) {
                        actionName = action.getActionClassName();
                    }
                    actions.put(actionName, action.getActionClassName());
                }
            }
            if (actions.size() > 0) {
                config.setActions(actions);
            }
        }
    }

    public static void loadCustomHandlers() throws MappingException {
        try {
            if (jarClassLoader != null || createJarClassLoader()) {
                LifecycleEventHandlers.loadLifecycleHandlers(jarClassLoader, ServerConfig.getCustomJarPath());
            } else {
                throw new MappingException("cannot load custom jar");
            }
        } catch (MalformedURLException e) {
            throw new MappingException("cannot load custom jar");
        }
    }

    public static Class<?> loadCustomClass(String className) throws MappingException, ClassNotFoundException {
        Class<?> clazz = null;
        try {
            if (jarClassLoader != null || createJarClassLoader()) {
                clazz = jarClassLoader.loadClass(className);
            } else {
                throw new MappingException("cannot load custom jar");
            }
        } catch (MalformedURLException e) {
            throw new MappingException("cannot load custom class", e);
        }
        return clazz;
    }

    private static boolean createJarClassLoader() throws MalformedURLException {
        boolean created = false;
        if (ServerConfig.getCustomJarPath() != null) {
            File file = new File(ServerConfig.getCustomJarPath());
            URL jarfile = file.toURI().toURL();
            jarClassLoader = URLClassLoader.newInstance(new URL[] { jarfile }, ConfigLoader.class.getClassLoader());
            created = true;
        }
        return created;
    }

    private static void loadCustomClasses(Collection<String> actionClasses) throws MalformedURLException, MappingException {
        if (jarClassLoader != null || createJarClassLoader()) {
            for (String className : actionClasses) {
                try {
                    jarClassLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new MappingException("cannot load custom class " + className, e);
                }
            }
        }
    }

    public static List<MappingConfig> getConfig() {
        return new ArrayList<MappingConfig>(localMappingConfigMap.values());
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

    protected static Map<MappingPattern, MappingEndpoint> getMappingsMap(List<MappingEndpoint> mappingEndpoints, Backend backend) throws MappingException {
        Map<MappingPattern, MappingEndpoint> mappings = new HashMap<MappingPattern, MappingEndpoint>();
        for (MappingEndpoint endpoint : mappingEndpoints) {
            // construct regular expression
            Pattern p = constructPattern(endpoint);
            MappingPattern pattern = new MappingPattern(p, endpoint.getMethod());
            if (endpoint.getBackendHost() == null || endpoint.getBackendPort() == null) {
                endpoint.setBackendHost(backend.getBackendHost());
                endpoint.setBackendPort(backend.getBackendPort());
            }
            // if the map already contains that pattern, throws exception
            if (mappings.containsKey(pattern)) {
                throw new MappingException("external path " + pattern.getMethod() + " " + pattern.getPattern().toString() + " is duplicated in mappings");
            } else {
                mappings.put(pattern, endpoint);
            }
        }
        return mappings;
    }

    protected static Pattern constructPattern(MappingEndpoint endpoint) {
        String path = endpoint.getExternalEndpoint();
        if (path.contains("{") && endpoint.getVarName() != null && endpoint.getVarExpression() != null) {
            String [] varNames = endpoint.getVarName().split(" ");
            String [] varExpressions = endpoint.getVarExpression().split(" ");
            String end = "";
            for (int i = 0; i < varNames.length; i++) {
                if ((i == varNames.length - 1) && endpoint.getExternalEndpoint().endsWith("}")) {
                    end = (varExpressions[i].endsWith(END)) ? "": END;
                }
                String varExpr = String.format(VAR_EXPRESSION_FORMAT, varExpressions[i], end);
                String varName = String.format(VAR_NAME_FORMAT, varNames[i]);
                path = path.replace(varName, varExpr);
            };
            return Pattern.compile(path);
        } else {
            // add $ for regular expressions - for no RE path
            return Pattern.compile(path + END);
        }
    }

    protected static IMap<String, MappingConfig> getHazelcastConfig() {
        return HazelcastConfigInstance.instance().getMappingConfigs();
    }

    /**
     * Reads mappings config from Hazelcast Distributed Map.
     */
    public static void updateMapping(String name, MappingConfig value) {
        try {
            reloadCustomClasses(value);
        } catch (MappingException e) {
            log.error("check custom.jar is the consistent on each running instance", e);
        }
        localMappingConfigMap.put(name, value);
    }

    /**
     * Removes mapping config.
     */
    public static void removeMapping(String name) {
        localMappingConfigMap.remove(name);
    }

    private static void reloadCustomClasses(MappingConfig config) throws MappingException {
        jarClassLoader = null;
        if (ServerConfig.getCustomJarPath() != null) {
            try {
                loadCustomClasses(config.getActions().values());
            } catch (MalformedURLException e) {
                log.error("cannot load custom jar ", e);
            }
        }
    }

    /**
     * Reloads all mapping and errors configs.
     * @throws MappingException
     */
    public static void reloadConfigs() throws MappingException {
        jarClassLoader = null;
        loadMappingsConfig(true);
        loadGlobalErrorsConfig(true);
    }

    /**
     * Returns all currently loaded mappings.
     * @return {@link Map} of all current mappings
     */
    public static Map<String, MappingConfig> getLoadedMappings() {
        return localMappingConfigMap;
    }

    public static Map<Integer, String> getLoadedGlobalErrors() {
        return localGlobalErrorsMap;
    }

    public static void updateError(Integer status, String message) {
        localGlobalErrorsMap.put(status, message);
    }

    public static void removeError(Integer status) {
        localGlobalErrorsMap.remove(status);
    }

    protected static IMap<Integer, String> getHazelcastErrorsConfig() {
        return HazelcastConfigInstance.instance().getGlobalErrors();
    }

}

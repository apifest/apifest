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

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Parses mapping configuration.
 *
 * @author Rossitsa Borissova
 */
public final class MappingConfigValidator {

    private static Logger log = LoggerFactory.getLogger(MappingConfigValidator.class);

    private MappingConfigValidator() {
    }

    public static void main(String [] args) {
        try {
            String mappingFile = null;
            if (args.length > 0) {
                mappingFile = args[0];
            } else {
                mappingFile = System.getProperty(ServerConfig.getMappingsPath());
            }
            if(mappingFile == null) {
                log.error("mapping file not set by argument in pom.xml or as system property apifest.mappings");
                return;
            }
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream xsdFile = ClassLoader.getSystemClassLoader().getResourceAsStream("schema.xsd");
            Schema schema = factory.newSchema(new StreamSource(xsdFile));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(mappingFile));
            log.info("mapping file validated");
        } catch (SAXException ex) {
            log.error("mapping file NOT valid: {}", ex.getMessage());
        } catch (IOException e) {
            log.error("mapping file not loaded: {}", e.getMessage());
        }
    }
}

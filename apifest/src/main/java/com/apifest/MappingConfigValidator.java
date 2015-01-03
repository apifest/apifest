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
import java.io.FileInputStream;
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
 * Validates a mapping file against schema.
 *
 * @author Rossitsa Borissova
 */
public final class MappingConfigValidator {

    private static Logger log = LoggerFactory.getLogger(MappingConfigValidator.class);
    private static volatile Schema schema;

    private MappingConfigValidator() {
    }

    public static void main(String[] args) {
        InputStream xsdFile = null;
        try {
            if (args.length == 1) {
                xsdFile = new FileInputStream(new File(args[0]));
            } else {
                throw new IllegalArgumentException("ERROR: schema file is not passed as arguments");
            }
            String mappingFile = System.getProperty("mapping.file");
            if (mappingFile == null) {
                throw new IllegalArgumentException("ERROR: mapping.file property not set");
            } else {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = factory.newSchema(new StreamSource(xsdFile));
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(mappingFile));
            }
        } catch (SAXException e) {
            throw new RuntimeException("ERROR: mapping file NOT valid: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("ERROR: mapping or schema file not loaded: " + e.getMessage());
        } finally {
            if (xsdFile != null) {
                try {
                    xsdFile.close();
                } catch (IOException e) {
                    System.err.println("cannot close input stream" + e);
                }
            }
        }
    }

    public static boolean validate(File mappingFile) {
        boolean ok = false;
        try {
            if (schema == null) {
                loadSchema();
            }
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(mappingFile));
            ok = true;
        } catch (SAXException ex) {
            log.error("mapping file NOT valid: {}", ex.getMessage());
        } catch (IOException e) {
            log.error("schema file not loaded: {}", e.getMessage());
        }
        return ok;
    }

    private static void loadSchema() {
        InputStream xsdFile = null;
        try {
            if (schema == null) {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                xsdFile = ClassLoader.getSystemResourceAsStream("schema.xsd");
                schema = factory.newSchema(new StreamSource(xsdFile));
            }
        }catch (SAXException e) {
            log.error("schema file not valid: {}", e.getMessage());
        } finally {
            if (xsdFile != null) {
                try {
                    xsdFile.close();
                } catch (IOException e) {
                    log.error("cannot close input stream", e);
                }
            }
        }
    }
}

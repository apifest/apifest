/*
 * Copyright 2015, ApiFest project
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

/**
 * Validates a file against the corresponding schema.
 *
 * @author rossitsaborissova
 *
 */
public final class ConfigValidator {


    private ConfigValidator() {
    }

    public static void validate(String schemaFile, String file) {
        InputStream xsdFile = null;
        try {
            if (schemaFile != null) {
                xsdFile = new FileInputStream(schemaFile);
            } else {
                throw new IllegalArgumentException("ERROR: schema file is not passed as an argument");
            }
            if (file == null) {
                throw new IllegalArgumentException("ERROR: file to be validated is not passed as an argument");
            } else {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = factory.newSchema(new StreamSource(xsdFile));
                Validator validator = schema.newValidator();
                validator.validate(new StreamSource(file));
            }
        } catch (SAXException e) {
            throw new RuntimeException("ERROR: file NOT valid: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("ERROR: file to be validated or schema file not loaded: " + e.getMessage());
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
}

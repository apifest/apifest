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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.testng.*;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.mockito.*;

import com.apifest.Mapping.ActionsWrapper;
import com.apifest.Mapping.EndpointsWrapper;
import com.apifest.example.AddSenderIdInBodyAction;
import com.apifest.example.RemoveBalanceFilter;
import com.apifest.example.ReplaceCustomerIdAction;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * @author Rossitsa Borissova
 */
public class MappingConfigTest {

    @BeforeTest
    public void setup() {
        String hzPath = getClass().getClassLoader().getResource("test_mapping.xml").getPath();
        ServerConfig.mappingsPath = hzPath;

        HazelcastConfigInstance.configInstance = mock(HazelcastConfigInstance.class);
        Map<String, com.apifest.MappingConfig> map = new HashMap<String, MappingConfig>();
        doReturn(map).when(HazelcastConfigInstance.configInstance).getMappingConfigs();

        // mock loggers
        AddSenderIdInBodyAction.log = mock(Logger.class);
        RemoveBalanceFilter.log = mock(Logger.class);

        ServerConfig.getMappingsPath();
    }

    @Test
    public void when_load_read_mapping() {

        // WHEN
        MappingConfigLoader.load();

        // THEN
        MappingConfig config = MappingConfigLoader.getConfig();
        Map<String, String> actions = config.getActions();
        assertEquals(actions.get("ReplaceCustomerId"), "com.apifest.example.ReplaceCustomerIdAction");
        assertEquals(actions.get("AddSenderIdInBody"), "com.apifest.example.AddSenderIdInBody");

        MappingEndpoint endpoint = config.getMappingEndpoint("/me", "GET");
        assertEquals(endpoint.getInternalEndpoint(), "/customer/{customerId}");
        assertEquals(endpoint.getActions().get(0).getName(), "ReplaceCustomerId");
    }

    @Test
    public void test_marshal() {
        // WHEN
        String marshalled = marshal();

        // THEN
        assertTrue(marshalled.startsWith("<mappings><actions><action"));
        assertTrue(marshalled.endsWith("</endpoint></endpoints></mappings>"));
        assertTrue(marshalled.contains("class=\"com.apifest.example.ReplaceCustomerIdAction\""));
        assertTrue(marshalled.contains("class=\"com.apifest.example.AddSenderIdInBody\""));
        assertTrue(marshalled.contains("name=\"AddSenderIdInBody\""));
        assertTrue(marshalled.contains("name=\"ReplaceCustomerId\""));
    }

    @Test
    public void when_actionClassname_is_null_get_className_from_actions_config() {
        // GIVEN
        MappingEndpoint endpoint = new MappingEndpoint();
        List<MappingAction> actions = new ArrayList<MappingAction>();
        MappingAction mappingAction = new MappingAction();
        mappingAction.setName("ReplaceCustomerId");
        actions.add(mappingAction);
        endpoint.setActions(actions);

        MappingConfigLoader.load();

        // WHEN
        BasicAction action = MappingConfigLoader.getConfig().getAction(mappingAction);

        // THEN
        assertTrue(action instanceof ReplaceCustomerIdAction);
    }


    @Test
    public void when_mapping_with_RE_construct_Pattern() throws Exception {
        // GIVEN
        MappingEndpoint endpoint = new MappingEndpoint();
        endpoint.setExternalEndpoint("/payments/{paymentId}");
        endpoint.setInternalEndpoint("/payments/{paymentId}");
        endpoint.setVarExpression("\\d*");
        endpoint.setVarName("paymentId");

        // WHEN
        Pattern p = MappingConfigLoader.constructPattern(endpoint);

        // THEN
        assertEquals(p.toString(), "/payments/(\\d*)");
    }

    @Test
    public void when_endpoint_contains_RE_return_it_from_RE_mappings() throws Exception {
        // GIVEN
        MappingConfigLoader.load();

        // WHEN
        MappingEndpoint endpoint = MappingConfigLoader.getConfig().getMappingEndpoint("/payments/12345", "GET");

        // THEN
        assertEquals(endpoint.getInternalEndpoint(), "/payments/12345");
    }


    @Test
    public void when_mapping_list_contains_method_and_uri_return_that_mapping_endpoint() throws Exception {
        // GIVEN
        MappingConfigLoader.load();

        // WHEN
        MappingEndpoint meEndpoint = MappingConfigLoader.getConfig().getMappingEndpoint("/me", "GET");

        // THEN
        assertEquals(meEndpoint.getExternalEndpoint(), "/me");
        assertEquals(meEndpoint.getMethod(), "GET");
    }


    @Test
    public void when_action_class_is_not_null_do_not_invoke_getAction_from_actions_map() throws Exception {
        // GIVEN
        MappingAction action = new MappingAction();
        action.setName("/testAction");
        action.setActionClassName("com.apifest.example.AddSenderIdInBodyAction");

        // WHEN
        BasicAction actionClass = MappingConfigLoader.getConfig().getAction(action);

        // THEN
        assertTrue(actionClass instanceof AddSenderIdInBodyAction);
    }

    private String marshal() {
        String result = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Mapping.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Mapping mapping = new Mapping();

            MappingAction action = new MappingAction();
            action.setName("ReplaceCustomerId");
            action.setActionClassName("com.apifest.example.ReplaceCustomerIdAction");

            MappingAction action2 = new MappingAction();
            action2.setName("AddSenderIdInBody");
            action2.setActionClassName("com.apifest.example.AddSenderIdInBody");

            List<MappingAction> actions = new ArrayList<MappingAction>();
            actions.add(action);
            actions.add(action2);

            ActionsWrapper allActions = new ActionsWrapper();
            allActions.setActions(actions);

            mapping.setActionsWrapper(allActions);

            MappingEndpoint endpoint = new MappingEndpoint();
            List<MappingAction> acts = new ArrayList<MappingAction>();
            MappingAction addAction = new MappingAction();
            addAction.setName("ReplaceCustomerId");
            acts.add(addAction);
            endpoint.setActions(acts);
            endpoint.setInternalEndpoint("/customer/{customerId}");
            endpoint.setExternalEndpoint("/me");

            EndpointsWrapper endpointWrapper = new EndpointsWrapper();
            List<MappingEndpoint> endpoints = new ArrayList<MappingEndpoint>();
            endpoints.add(endpoint);

            endpointWrapper.setEndpoints(endpoints);
            mapping.setEndpointsWrapper(endpointWrapper);

            marshaller.marshal(mapping, out);
            result = out.toString("UTF-8");
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

}

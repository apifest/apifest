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

package com.apifest.example;

/**
 * @author Rossitsa Borissova
 */
public class AddSenderIdInBodyActionTest {

  /*  AddSenderIdInBodyAction action;
    HttpRequest req;

    @BeforeMethod
    public void setup() {
        action = spy(new AddSenderIdInBodyAction());
        req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/payments");
        req.headers().add(HttpHeaders.Names.CONTENT_TYPE, "application/json");
        String content = "{}";
        req.setContent(ChannelBuffers.copiedBuffer(content.getBytes(CharsetUtil.UTF_8)));
        req.headers().add(HttpHeaders.Names.CONTENT_LENGTH, content.getBytes(CharsetUtil.UTF_8).length);
    }*/

    /*@Test
    public void when_execute_add_senderId_in_request_body() throws Exception {
        // GIVEN
        HttpResponse validationResponse = mock(HttpResponse.class);
        ChannelBuffer content = ChannelBuffers.copiedBuffer(("{\"valid\":true,\"codeId\":\"\",\"scope\":\"basic scope2\"," +
                "\"details\":null,\"token\":\"f60971beb3881ea0bd5675a5baf2a47e95332277\",\"created\":1409043454427,\"expiresIn\":\"300\"," +
                "\"userId\":\"12345\",\"refreshToken\":\"ae79692416349da03fcdff1beca6e131ab3e5de1\",\"type\":\"Bearer\",\"clientId\":\"196657904238186\"}").getBytes(CharsetUtil.UTF_8));
        willReturn(content).given(validationResponse).getContent();

        // WHEN
        HttpRequest request = action.execute(req, "/payments", validationResponse);

        // THEN
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(new String(request.getContent().toString(CharsetUtil.UTF_8))).getAsJsonObject();
        assertEquals(json.get("senderId").getAsString(), "1232");
    }*/

}

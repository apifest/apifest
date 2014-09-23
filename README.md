#ApiFest OAuth 2.0 Server and Mapping
ApiFest consists of two main parts - the ApiFest OAuth 2.0 an OAuth 2.0 server and the ApiFest Mapping Server.

##ApiFest Mapping Server
The ApiFest Mapping Server is for people who have APIs and want to expose them to the world in a safe and convenient way.
The ApiFest Mapping Server is used to translate between the outside world and your internal systems. It helps you keep a consistent API facade.

###Features
- mappings are described in xml;
- can validate and authorize requests using the ApiFest OAuth20 Server;
- out-of-the-box flexible mapping options - several versions support, different hosts to which API requests could be directed to;
- easy to extend and customize;
- customizable error messages and responses;
- "online" change of all configurations;
- unlimited horizontal scalability;


##ApiFest OAuth 2.0 Server
The ApiFest OAuth 2.0 Server implements OAuth 2.0 server side as per http://tools.ietf.org/html/rfc6749.
It enables the usage of access tokens in ApiFest Mapping Server.

###Features
- register new client app;
- generate access token using auth code;
- generate access token using username and password - grant_type=password;
- generate access token using client credentials - grant_type=client_credentials;
- generate access token using refresh token - grant_type=refresh_token;
- revoke access token;
- validate access token;
- pluggable storage (currently supports MongoDB and Redis);
- unlimited horizontal scalability;


##ApiFest Mapping Server Quick start:
**1. apifest.properties file**

Here is a template of the apifest.properties file:
```
apifest.host=
apifest.port=
apifest.mappings=
token.validate.host=
token.validate.port=
connect.timeout=
custom.jar=
apifest.nodes=
```

The path to the apifest.properties file should be set as a system variable:

***-Dproperties.file***

* **Setup the ApiFest Mapping Server host and port**

The ApiFest Mapping Server can run on different hosts and ports.
You can define ApiFest Mapping Server host and port in the apifest.properties file -
***apifest.host*** and ***apifest.port***

By default, ApiFest Mapping Server will start on localhost:8080.

* **Setup mappings**

The ApiFest Mapping Server needs information how to translate requests between the outside world and your internal system.
That should be done in a mapping configuration file. 
The mapping configuration file is XML with schema accessible in the project under resources folder - *schema.xsd*.

Here is an example mapping file:
```
<mappings version="v0.1">
    <actions>
        <action name="ReplaceCustomerId" class="com.apifest.example.ReplaceCustomerIdAction"/>
        <action name="AddSenderIdInBody" class="com.apifest.example.AddSenderIdInBodyAction"/>
    </actions>
    <filters>
        <filter name="RemoveBalance" class="com.apifest.example.RemoveBalanceFilter"/>
    </filters>
    <backend host="127.0.0.1" port="8080"/>
    <endpoints>
        <endpoint external="/v0.1/me" internal="/customers/{customerId}" method="GET" authType="user" scope="basic">
            <action name="ReplaceCustomerId" />
            <filter name="RemoveBalance" />
        </endpoint>
        <endpoint external="/v0.1/me/friends" internal="/customers/{customerId}/friends" method="GET" authType="user" scope="basic">
            <action name="ReplaceCustomerId" />
        </endpoint>
        <endpoint external="/v0.1/countries/{countryId}" internal="/countries/{countryId}" method="GET" authType="client-app" varExpression="\w{3}$" varName="countryId"/>
        <endpoint external="/v0.1/mobile-auth/{mobileId}" internal="/mobile-auth/{mobileId}" method="GET" authType="user" varExpression="\d{6,15}$" varName="mobileId"/>
        <endpoint external="/v0.1/mobile-auth/{mobileId}" internal="/mobile-auth/{mobileId}" method="POST" authType="user" varExpression="\d{6,15}$" varName="mobileId"/>
    </endpoints>
    <errors>
        <error status="404" message='{"error":"resource not found"}' />
        <error status="405" message='{"error":"method is not allowed on that resource"}' />
        <error status="500" message='{"error":"ops...something wrong"}' />
    </errors>
</mappings>
```

XML specific tags explained:

- version - is the version of your API this mapping file describes
- actions - defines actions with name and class
- filters - defines filters with name and class
- backend - defines where your API is running, requests should be translated to that backend 
- endpoint - is a mapping between outer ednpoint and your API endpoint;
- external - the endpoint visible to the world;
- internal - your backend endpoint;
- method - HTTP method;
- scope - scope(s) of the endpoint;
- authType - *user* for tokens obtained with user credentials, *client-app* for tokens obtained for client application;
- action - defines action that will be executed before requests hit your API;
- filter - defines filter that will be executed before responses from API are returned back;
- varName - the name of the variable/s used in internal/external path (space delimited);
- varExpression - regular expression (Java format) for varName (space delimited);
- error - customize error responses - *status* attribute value defines the HTTP status for which *message* attribute value will be returned;

You can define as many mapping configuration files as many versions your API supports.
The ApiFest Mapping Server will get all mappings files from the directory defined in the apifest.properties as 

***apifest.mappings***

* **Setup token validation host and port**

If access token is required for an endpoint, first it should be validated. The host and port where the ApiFest OAuth 2.0 Server runs are set by the following properties in the apifest.properties file -

***token.validate.host*** and ***token.validate.port*** 

* **Setup connection timeout**

In order to setup connection timeout(in ms) to the backend, use the following property in the apifest.properties file -

***connect.timeout***

If you have custom request/response transformations, then you can set the path to your jar with transformations by the 
following property:
   
***custom.jar***

As ApiFest configurations are stored in distributed cache, you need to setup all other nodes (as comma-separated list of 
IPs) on which the ApiFest Mapping Server is running. To do that use the following property in the apifest.properties file -

***apifest.nodes***
 

**3. Start the ApiFest Mapping Server**

You can start the ApiFest Mapping Server with the following command:

```java -Dproperties.file=[apifest_properties_file_path] -Dlog4j.configuration=file:///[log4j_xml_file_path] -jar apifest-0.1.0-jar-with-dependencies.jar```

When the server starts, you will see:
```ApiFest Mapping Server started at [host]:[port]```
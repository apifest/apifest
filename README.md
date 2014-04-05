#ApiFest OAuth 2.0 Server and Mapping
ApiFest consists of two main parts - ApiFest OAuth 2.0 an OAuth 2.0 server and the ApiFest Mapping Server.

##ApiFest Mapping Server
ApiFest Mapping Server (AMS) is for people who have APIs and want to expose them to the world in a safe and convenient way.
AMS is used to translate between the outside world and your internal systems. It helps you keep a consistent API facade.

###Features
- mappings are described in xml;
- can validate and authorize requests using the ApiFest OAuth20 Server;
- out-of-the-box flexible mapping options - several versions support, different hosts to which API requests could be directed to;
- easy to extend and customize;
- customizable error messages and responses;
- "online" change of all configurations;
- unlimited horizontal scalability;


##ApiFest OAuth 2.0 Server
ApiFest OAuth 2.0 Server implements OAuth 2.0 server side as per http://tools.ietf.org/html/rfc6749.
It enables the usage of access tokens in ApiFest Mapping Server.

###Features
- register new client app;
- generate access token using auth code;
- generate access token using username and password - grant_type=password;
- generate access token using client credentials - grant_type=client_credentials;
- generate access token using refresh token - grant_type=refresh_token;
- revoke access token;
- validate access token;
- pluggable storage (currently supports MongoDB);
- unlimited horizontal scalability;


##ApiFest Mapping Server Quick start:
**1. apifest.properties file**

Here is a template of apifest.properties file:
```
apifest.host=
apifest.port=
apifest.mappings=
token.validate.endpoint=
connect.timeout=
```

The path to the apifest.properties file should be passed as a system variable:

***-Dproperties.file***

* **Setup ApiFest Mapping Server host and port**

ApiFest Mapping Server can run on different hosts and ports.
You can define AMS host and port in apifest.properties file -
***apifest.host*** and ***apifest.port***

By default, AMS will start on localhost:8080.

* **Setup mappings**

ApiFest Mapping Server needs information how to translate requests between the outside world and your internal system.
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
        <endpoint external="/v0.1/me" internal="/customers/{customerId}" method="GET" authRequired="true" scope="basic">
            <action name="ReplaceCustomerId" />
            <filter name="RemoveBalance" />
        </endpoint>
        <endpoint external="/v0.1/me/friends" internal="/customers/{customerId}/friends" method="GET" authRequired="true" scope="basic">
            <action name="ReplaceCustomerId" />
        </endpoint>
        <endpoint external="/v0.1/countries/{countryId}" internal="/countries/{countryId}" method="GET" varExpression="\w{3}$" varName="countryId"/>
        <endpoint external="/v0.1/mobile-auth/{mobileId}" internal="/mobile-auth/{mobileId}" method="GET" varExpression="\d{6,15}$" varName="mobileId"/>
        <endpoint external="/v0.1/mobile-auth/{mobileId}" internal="/mobile-auth/{mobileId}" method="POST" varExpression="\d{6,15}$" varName="mobileId"/>
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
- internal - your API endpoint;
- method - HTTP method;
- scope - scope(s) of the endpoint;
- authRequired - whether user authorization is required;
- action - defines action(s) that will be executed before requests hit your API;
- filter - defines filter(s) that will be executed before responses from API are returned back;
- varName - the name of a variable used in internal/external path;
- varExpression - varName regular expression;
- error - customize error responses - *status* attribute value defines the HTTP status for which *message* attribute value will be returned;

You can define as many mapping configuration files as many versions your API supports.
ApiFest Mapping Server will get all mappings files from the directory defined in apifest.properties as 

***apifest.mappings***

* **Setup token validation URL**

If access token is required for an endpoint, first it should be validated. The endpoint(URL) that validates access tokens is setup by the following property in apifest.properties file -

***token.validate.endpoint***

* **Setup connection timeout**

In order to setup connection timeout(in ms) to the backend, use the following property in apifest.properties file -

***connect.timeout***


**2. Hazelcast config**

As ApiFest Mapping Server uses Hazelcast for its mapping configurations, you should create hazelcast configuration 
file with Map named "mappings".
A template with hazelcast configuration file could be found in resources folder in the project.
The path to the hazelcast configuration file should be passed as system variable named hazelcast.config.file:

***-Dhazelcast.config.file***

**3. Start ApiFest Mapping Server**

You can start AMS with the following command:

```java -Dproperties.file=[apifest_properties_file_path] -Dhazelcast.config.file=[hazelcast_config_file_path] -jar apifest-0.1.0-jar-with-dependencies.jar```

When the server starts, you will see:
```ApiFest Mapping Server started at [host]:[port]```
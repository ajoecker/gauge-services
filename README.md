# Gauge Service Test Project  
## Idea  
To have a simple and easy-usable project for testing services such as graphql or REST.  

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ajoecker/gauge-services/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ajoecker/gauge-services)
[![Build Status](https://travis-ci.org/ajoecker/gauge-services.svg?branch=master)](https://travis-ci.org/ajoecker/gauge-services)
[![Sonarcloud Status](https://sonarcloud.io/api/project_badges/measure?project=com.github.ajoecker:gauge-services&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.github.ajoecker:gauge-services)

## Implementation  
The implementation uses [http://gauge.org](http://gauge.org) as describer and runner of the automated tests and [http://rest-assured.io/](http://rest-assured.io/) as helper library.  
  
## Execution  
### Run all test cases  
To execute all specs call `mvn clean install`. This will build the project and run all test cases  
  
### Run only a specific test case  
To execute a specific test case you can  
* call `mvn clean install -DspecsDir="specs/<spec_to_execute>` to run a complete spec with all test cases  
* call `mvn clean install -DspecsDir="specs/<spec_to_execute> -Dscenario=<name_of_scenario>` to run a single scenario of a spec  
  
## Usage 
The library can be used for two different api testings

### Graphql 
#### Maven
To use the library in a project simply put the following in the `pom.xml`  
```  
<dependency>  
 <groupId>com.github.ajoecker</groupId>
 <artifactId>gauge-graphql</artifactId>
 <version>0.3.3</version>
 <scope>test</scope>
</dependency>  
```
#### Gradle
```
testCompile 'com.github.ajoecker:gauge-graphql:0.3.3'
```

### REST 
#### Maven
To use the library in a project simply put the following in the `pom.xml`  
```  
<dependency>  
 <groupId>com.github.ajoecker</groupId>
 <artifactId>gauge-rest</artifactId>
 <version>0.3.3</version>
 <scope>test</scope>
</dependency>  
```
#### Gradle

#### Gradle
```
testCompile 'com.github.ajoecker:gauge-rest:0.3.3'
```
## Examples
The project [gauge-service-examples](https://github.com/ajoecker/gauge-services-examples) shows some examples of the usage.

## New Testcases  
To add new test cases one can either create a new spec file with the scenario(s) or add the new scenario to an   
existing spec.  
  
### Building blocks  
To add a new test case (scenario) one can re-use existing building blocks for the sake of simplicity. 

#### General
##### Endpoint
The library allows to ways for defining the endpoint to test.

- In the environment of Gauge with the key `gauge.service.endpoint`
- In a spec file as a common step `* Given the endpoint "http://the-endpoint`

The first one can be used to define a common endpoint for all specs and can be varied by using multiple gauge environments.

The second can be used to define an endpoint on a spec/scenario/step based level and allows more flexibility if needed.

##### Request time
To verify that a request took maximal of time, one can use one of the following
```
* Then the request finished in less than <timeout> ms
* And the request finished in less than <timeout> ms
* Then the request finished in less than <timeout> s
* And the request finished in less than <timeout> s
```
for example
```
* Then the request finished in less than "2" s
```
#### Status code
To verify a status code
```
* Then status code is <code>
* And status code is <code>
```
for example
```
* And status code is "200"
```
#### Authentication
The library supports Basic Authentication via user / password combination, via base64 encoded token or also via a query
to receive a token for succeeding queries. 

Simple Basic Authentication with a user and password
```
* When <user> logs in with password <password>
* And <user> logs in with password <password>
```
In case the authentication information is stored in the [Configuration](#Configuration)
```
* When user logs in
* And user logs in
```
For an existing token
```
* When user logs in with <token>
* And user logs in with <token>
```
#### GET
To send a GET request the following can be used
```
* When getting <resource>
* And getting <resource>
```
For sending parameters with the query
```
* When getting <query> with <parameters>
* And getting <query> with <parameters>
```
whereas the parameters can either be a string or a gauge table

for example
```
* When getting "comments" with 

   |name  |value|
   |------|-----|
   |postId|1    |
* When getting "comments" with "postId=1"
```
For multiple parameters one can either use multiple rows in the table or a `,` separated string.

#### POST
To send a POST request, the following can be used:

* To simple post a query to the current endpoint
```
* When posting <query>
* And posting <query>
```
whereas the `<query>` is a file containing the actual query.

for example
```
* Given the endpoint "https://api.predic8.de:443/shop/products/"
* When posting <file:src/test/resources/wildberries.json>
* Then "name" is "Wildberries Wild"
```
the `file` parameter gives the full path to the query file relative to the project (or with `/` as an absolute path to the file).

In the example the `wildberries.json` looks like this
```
{
  "name": "Wildberries Wild",
  "price": 10.99,
  "category_url": "/shop/categories/Fruits",
  "vendor_url": "/shop/vendors/672"
}
```

* To post a query to a certain path
```
* When posting <query> to <path>
* And posting <query> to <path>
```
This allows to define a common endpoint and post to different paths.

with the example above, one could rewrite it to 
```
* Given the endpoint "https://api.predic8.de:443"
* When posting <file:src/test/resources/wildberries.json> to "shop/products"
* Then "name" is "Wildberries Wild"
```

* To post a query with parameters
```
* When posting <query> with <parameters> 
* And posting <query> with <parameters>
```
for example
```
* When posting <file:src/test/resources/popular_artists_variable.graphql> with 

   |name|value|
   |----|-----|
   |size|4    |
* When posting <file:src/test/resources/popular_artists_variable.graphql> with "size=4"
```
This can also be combined then with the posting a query to a certain path.

#### PUT
PUT follows the same as described in [POST](#POST), with using `putting` instead of `posting`.

#### Extracting results
Any result can be extracted into a variable by defining the parent element and a matcher to find the variable
```
* Then extracting <variable> from <parent> where <attribute>
* And extracting <variable> from <parent> where <attribute>
```
In case no parent element is required
```
* Then extracting <variable> where <attribute>"
* And extracting <variable> where <attribute>
```

this can be used to 

* verify the value of the variable
```
* Use "https://jsonplaceholder.typicode.com/"
* When getting "comments" with "postId=1"
* And extracting "id" where "email=Nikita@garfield.biz"
* Then "id" is "3"
```
* use it for a succeeding call
```
* And extracting "id" from "cases" where "last_name=Vetinari,first_name=Havelock"
* When getting "cases/%id%"
``` 

#### Verification
To verify a response

##### contains
```
Then <path> contains <value>", "And <path> contains <value>
```
asserts that the given path contains the given value, for example
```
## popular artists
* When posting <file:src/test/resources/popular_artists.graphql>
* Then "popular_artists.artists.name" contains "Pablo Picasso, Banksy"
```
similar to examples above the expected values can be given as string or gauge table
##### is
```
Then <path> is <value>", "And <path> is <value>
```
asserts that the given path contains the given value, for example
```
## popular artists is matching with table
* When posting <file:src/test/resources/popular_artists.graphql>
* Then "popular_artists.artists" is 

   |name         |nationality|
   |-------------|-----------|
   |Pablo Picasso|Spanish    |
   |Banksy       |British    |
```
##### is empty
```
* When getting "zipcode/any_invalid_zipcode"
* Then "city.name" is empty
```
asserts that the given path as no value.

#### Chaining 
As all steps define their common BDD term `Given`, `When`, `Then`, as also can start with `And` it is easy to chain
multiple calls as also multiple verifications.  
 
##### Example  
`* Then "breakfast.time" is "8am"`  
`* And "breakfast.brand.name" is "Nutella"`    
`* And "breakfast.calories" is "Oh Hell NOOOO"`  

#### Dynamic queries
It is possible to use dynamic queries, when using variables in the query file.
##### Example
```
popular_artists(size: %size%) {
    artists {
        name
        nationality
    }
}
``` 
When using variables, the `When` step in the spec file must replace this variable to get a valid query.

Like
```
* When posting <file:queries/popular_artists_variable.graphql> with "size=4"`
```

It is also possible to facilitate gauge table for dynamic replacement
```
* When posting <file:queries/popular_artists_variable.graphql> with 

   |name|value|
   |----|-----|
   |size|4    |
```
whereas the column headers must be named `name` and `value`.

It is also possible to use the result of a previous request as substitute for a variable
```
## stations around Frankfurt with table
* When posting <file:queries/dbahn_frankfurt.graphql>
* And posting <file:queries/dbahn_frankfurt_nearby.graphql> with 

   |name     |value                                 |
   |---------|--------------------------------------|
   |latitude |%stationWithEvaId.location.latitude%  | 
   |longitude|%stationWithEvaId.location.longitude% |
   |radius   |2000                                  |
```
the first two values are masked to identify them as variables and contain the full path to a single value (list values are currently not supported).

The values are used in the second request to replace any variables in the query named `latitude` and `longitude`

Furthermore one can use variable files for graphql queries, when e.g. complex parameters are required.

E.g. given the following graphql query:
```
mutation newUser($user: NewUserInputData!) {
    newUser(user: $user) {
        id
        success
    }
}
```
one can give the fitting parameters either as json string in the step or as external file:
```
* When posting <file:query/createUser.graphql> with "{ 'customer': { 'email': 'some-customer-email@email.com', 'password' : 'RCBb8kjzzX^S' } }"
* When posting <file:query/createUser.graphql> with <file:query/createUserInput.json>" 
```
for the latter the input would look like this:
```
{
  "customer": {
    "email": "some-customer-email3333@email.com",
    "password" : "RCBb8kjzzX^S"
  }
}
```
## Referencing Variables
Variables can be defined via the steps provided from [https://github.com/ajoecker/gauge-random-data](https://github.com/ajoecker/gauge-random-data).

A defined variable can be referenced inside a scenario with `%`

for example:
```
## Comments with id and table
* Given the endpoint "https://jsonplaceholder.typicode.com/"
* Set "email" to "Nikita@garfield.biz"
* When getting "comments" with "postId=1"
* And extracting "id" where "email=%email%"
* Then "id" is "3"
```
In this case, the variable `%email%` will be replaced with the defined value `Nikita@garfield.biz`.

**Attention**

Values are first replaced from a defined variable in the scenario and then from a previous response.
This means if a variable is defined in the scenario and from a previous responds, the first is taken.
    
## Configuration
In the Gauge environment the following keys are recognized
 
### gauge.service.endpoint
*Optional*

The endpoint of the api. Only mandatory if the endpoint is not given in the spec file directly.
 
### gauge.service.debug
*Optional*

Will add request and response debug information on the console.

Possible values are `all` to log all information for request and response or `failure` to log only failed requests/responses 
 
### gauge.service.token
*Optional*

In case there is a common token for login instead of a dynamic one (see `gauge.service.token.query`)
 
### gauge.service.token.query
*Optional*

Path to the file, that contains the query for the login. Username/Email and password must be masked.
#### Example
```
mutation {  
    login(email: "%user%", password: "%password%") {  
        token  
    }  
}
```
### graph.token.path
*Optional*

When a *gauge.service.token.query* is given, this becomes *mandatory* as it gives the jsonpath from which the token can be extracted from the response of the login query. 

E.g. in the above query example, the `gauge.service.token.path` could be `data.login.token`
 
#### Example
`* Then "popular_artists.artists.name" must contain "Pablo Picasso, Banksy"`

### gauge.service.loginhandler
*Optional*

Defines the type of login that is used, if required. Values can be `token` (login via a token) or `basic`
(login via basic authentication)

## Note  
Gauge does not support currently multi-line parameters, which means a query cannot be part of the step, but must  be referenced by an external file. Watch https://github.com/getgauge/gauge/issues/175 for this.

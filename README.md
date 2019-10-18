# Gauge Graphql Test Project  
## Idea  
To have a simple and easy-usable project for testing services such as graphql or REST.  

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ajoecker/gauge-services/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.ajoecker/gauge-services) [![Build Status](https://travis-ci.org/ajoecker/gauge-services.svg?branch=master)](https://travis-ci.org/ajoecker/gauge-services)

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
To use the library in a project simply put the following in the `pom.xml`  
```  
<dependency>  
 <groupId>com.github.ajoecker</groupId>
 <artifactId>gauge-graphql</artifactId>
 <version>0.3.1-SNAPSHOT</version>
 <scope>test</scope>
</dependency>  
```

### REST 
To use the library in a project simply put the following in the `pom.xml`  
```  
<dependency>  
 <groupId>com.github.ajoecker</groupId>
 <artifactId>gauge-rest</artifactId>
 <version>0.3.1-SNAPSHOT</version>
 <scope>test</scope>
</dependency>  
```

## Examples
The project [gauge-service-examples](https://github.com/ajoecker/gauge-services-examples) shows some examples of the usage.

## New Testcases  
To add new test cases one can either create a new spec file with the scenario(s) or add the new scenario to an   
existing spec.  
  
### Building blocks  
To add a new test case (scenario) one can re-use existing building blocks for the sake of simplicity. 

#### Define endpoint
The library allows to ways for defining the endpoint to test.

- In the environment of Gauge with the key `gauge.service.endpoint`
- In a spec file as a common step `* Use "http://the-endpoint`

The first one can be used to define a common endpoint for all specs and can be varied by using multiple gauge environments.

The second can be used to define an endpoint on a spec based level and allows more flexibility if needed.
  
#### Login required  
If a login is required to execute subsequent queries, the first step of a scenario must be
  
`Given "<email>" logs in with password "<password>"`  
  
whereas `<email>` must be an existing customer email and `<password>` must be the matching password of the customer.

If the login is working on a common token and not a dynamic created one, the first step can also be

`Given user logs in`

Also the library suppors simple basic authentication.

See the [Configuration](#Configuration) section for details and how to set up authentication.
  
#### POST
To send a query/mutation one must create a file inside the gauge project and use this file in the sending step  
  
`When posting <file:the_file_to_send_with_full_path>`  
  
whereas `the_file_to_send_with_full_path` is the full path of the query file.  

#### GET
To get a resource one can simply query  
  
`When getting "1234"`   
  
#### Verifying the result  
To verify a response multiple building blocks exist. All of them can either start with `Then` or when chaining multiple verifications with `And`.  
  
Also all verification steps have the json path of the attribute to verify as first parameter (see examples below)  
  
##### Is  
Verifies that the returned value is a certain value, whereas value can either be a single value or a table.  
  
###### Examples  
`* Then "vehicle.price" is "720"` 

`* Then "cities.name" is "New York, London"`  
```  
* Then "brands" are   
 |id |name            | 
 |---|----------------|
 |10 |OREO            | 
 |73 |NUTELLA         |
```  
If the given response path returns a list with multiple attributes, one can also state a map like pattern

`* Then "popular_artists.artists" are "{name: Pablo Picasso, nationality: Spanish}, {name: Banksy, nationality: British}"`

##### Contains
Verifies that the returned value contains a certain value.

This follows the same blocks as above.

##### Is Empty  
Verifies that the returned value is empty  

###### Examples  
`* Then "cities.city" is empty`  
  
#### Chaining result verifications  
When multiple values shall be verified, each verification is one step in the scenario. The second and later  
verification can start with `And` instead of `Then` for better reading purpose. 
 
##### Example  
`* Then "vehicle.price" is "720"`  
`* And "breakfast.brand.name" is "Nutella"`    
`* And "breakfast.brand.calories" is "Oh Hell NOOOO"`  

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
 
 `* When posting <file:queries/popular_artists_variable.graphql> with "size:4"`
 
It is possible to configure the string that masks the variable in the query file (default: `%`), via the configuration
`gauge.service.variable.mask`.

It is also possible to configure the separator that divides the variable name with the variable value in the step (default `:`), 
via the configuration `gauge.service.variable.separator`.

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

Furthermore one can use variable files for graphql queries, when e.g. complex variables are required.

E.g. given the following graphql query:
```
mutation createUser($user: CreateUserInput!) {
    createUser(user: $user) {
        id
        success
    }
}
```
one can give the fitting variables either as json string in the step or as external file:
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
## Configuration
In the Gauge environment the following keys are recognized
 
### gauge.service.endpoint
*Mandatory*

The endpoint of the api. Only mandatory if the endpoint is not given in the spec file directly.
 
### gauge.service.debug
*Optional*

Will add request and response debug information on the console.
 
### gauge.service.token
*Optional*

In case there is a common token for login instead of a dynamic one (see `gauge.service.token.query`)
 
### gauge.service.token.query
*Optional*

Path to the file, that contains the query for the login. Username/Email and password must be masked with configuration: `gauge.service.variable.mask`.
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
 
### gauge.service.separator
*Optional*

Defines the separator in the verifying step to define multiple elements that needs to be verified. Default is `,`
#### Example
`* Then "popular_artists.artists.name" must contain "Pablo Picasso, Banksy"`

### gauge.service.variable.mask
*Optional*

Defines the string that masks a variable in the graphql file
#### Example
```
popular_artists(size: %size%) {
    artists {
        name
        nationality
    }
}
``` 
### gauge.service.variable.separator
*Optional*

Defines the separator of variable name and variable value in the step. Default is `:`
#### Example
 `* When sending <file:queries/popular_artists_variable.graphql> with "size:4"`
 
### gauge.service.loginhandler
*Optional*

Defines the type of login that is used, if required. Values can be `token` (login via a token) or `basic`
(login via basic authentication)

## Note  
Gauge does not support currently multi-line parameters, which means a query cannot be part of the step, but must  be referenced by an external file. Watch https://github.com/getgauge/gauge/issues/175 for this.

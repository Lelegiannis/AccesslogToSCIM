# What is Accesslog To SCIM
Accesslog To SCIM is a standalone java application designed to keep one or more Identity Servers up to date with the changes happening to a specified LDAP directory.
This is achieved by converting the LDAP accesslog to a series of SCIM requests and sending them to each identity server.
The receiving servers are specified upon execution and through property files.
The application keeps track of what was sent and if it reached the target server through a database audit.
Works for both SCIM 1.1 and SCIM 2.

# How to use
Compile using `mvn install` to produce the executable jar file.

Before executing the jar file, make sure that all the mandatory configuration files described in the [Configuration section](#configuration) are present in the same directory as the jar file.

To execute the jar file use `java -jar accesslogToSCIM.jar --identity-servers <acronymOfServer>`
Example: `java -jar accesslogToSCIM.jar --identity-servers wso2-test`

# Configuration
### General configuration
The `accesslog.properties` file contains the connection details to the LDAP's accesslog. This configuration file is mandatory for the application's execution. Refer to `conf-examples/accesslog.properties.example`

The `db.properties` file contains the connection details to the audit database of the application. This configuration file is mandatory for the application's execution. Refer to `conf-examples/db.properties.example`
### Identity Server Definition
Identity servers are defined through configuration files. To define a new identity server, you must create the files `<server_name>.mapping` and `<server_name>.properties`.
At least one identity server must be defined and specified on execution for the application to work properly.
You can find an example of configuration files defining a SCIM1.1 Identity Server on `conf-examples/test-wso2-scim1.mapping.example` and `conf-examples/test-wso2-scim1.properties.example`.
You can also find an example of configuration files defining a SCIM2 Identity Server on `conf-examples/test-wso2-scim2.mapping.example` and `conf-examples/test-wso2-scim2.properties.example`

# Common Issues
In case of certificate trust problems, you can specify a new truststore using the `-Djavax.net.ssl.trustStore=<path_to_truststore>` option.
Example: `java -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts -jar accesslogToSCIM.jar --identity-servers wso2-test`

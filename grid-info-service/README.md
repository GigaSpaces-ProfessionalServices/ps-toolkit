##DataGrid Info Tool

***Purpose:*** This tool connects to a running DataGrid and reports out a variety of data about the grid and its configuration.

***Usage:*** Building the project will create an executable jar file with a manifest file that specifies the main-method-containing class. 
To run it you would invoke the following command from the build directory:

```bash
java [SYSTEM PROPERTIES] -jar target\grid-info-service\grid-info-service.jar 

System properties:
-Dgsa.count           A count of running GS Agents to be discovered. Default value is 1.
-Dlookup.locators     Lookup locators.
-Dlookup.groups       Lookup groups.
-Dxap.user.name       A username in case of space security is enabled. (It will prompt for user password)
-Dwait.timeout        A wait timeout to lookup XAP components. Default value is 10 seconds.
```
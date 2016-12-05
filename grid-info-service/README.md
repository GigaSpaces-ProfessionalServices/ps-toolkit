##DataGrid Info Tool

***Purpose***

Report DataGrid runtime properties.

***Usage*** 

1. configure (optional: if you want email support)

edit [mail.properties](./src/main/resources/mail.properties)

2. build
 
```mvn clean package```

3. run

```bash
java [options] -jar target\grid-info-service\grid-info-service.jar
```

Data is output to file ```grid-info.pdf```. If valid mail credentials are provided, then the file is relayed to an email address.

***Options (none required)***

Properties|purpose|default
---|---|---
gsa.count|A count of running GS Agents to be discovered|1
lookup.locators|Lookup locators|[GigaSpaces' system default]
lookup.groups|Lookup groups|[GigaSpaces' system default]
xap.user.name|username, when Space security is enabled. (Will prompt for password.)|none
wait.timeout|A wait timeout to lookup XAP components. Default value is 10 seconds.|10
```
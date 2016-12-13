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

Data is output to ```grid-info.pdf```. With valid credentials, file is emailed.

***Options***

Properties|purpose|default|required
---|---|---|---
gsa.count|Number of running agents to wait for during discovery|1|no
lookup.locators|Lookup locators|none|yes
lookup.groups|Lookup groups|system default|no
xap.user.name|username, when Space security is enabled. (Will prompt for password.)|none|no
wait.timeout|A wait timeout to lookup XAP components.|10|no

{{% note "Important Note"%}}
The patterns and source code available in this repository are provided as is. While most of them are used in real life use cases and productions environments, and GigaSpaces is making its best effort to keep them up to date, they should not be considered as fully productized artifacts, and you should test them in your own environment before using them.
{{% /note %}}

The Apache v2 license applies. Here's [the fine print](../license.txt).
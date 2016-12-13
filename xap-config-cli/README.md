# DataGrid client config

{{% note "Important Note"%}}
The patterns and source code available in this repository are provided as is. While most of them are used in real life use cases and productions environments, and GigaSpaces is making its best effort to keep them up to date, they should not be considered as fully productized artifacts, and you should test them in your own environment before using them.
{{% /note %}}

The Apache v2 license applies. Here's [the fine print](../license.txt).

#####commons-cli extension library for DataGrids

This library provides a conventional CLI API for JVM applications that connect to DataGrids. CLI style is POSIX compliant.

######To implement

1. build

```bash
mvn clean package
```

(Jars are not hosted anywhere.)


2. depend upon produced jar

######API usage

1. Obtain an XAPOptions instance

```
XAPConfigCLI xapConfigCLI = new XAPConfigCLI();
XAPOptions xapOptions = xapConfigCLI.parseArgs(args);
```

2. Use CommandLine object

```
Boolean multicastEnabled = (Boolean) xapOptions.getCommandLine().getParsedOptionValue(XAPOptions.MULTICAST_ENABLED);
```

OR

```
Optional<Boolean> multicastEnabled = xapOptions.getMulticastEnabled();
```

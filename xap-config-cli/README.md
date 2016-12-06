# DataGrid client config

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

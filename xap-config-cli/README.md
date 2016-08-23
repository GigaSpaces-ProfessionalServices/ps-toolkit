# DataGrid Connection Config CLI

The purpose of this module is to provide a conventional CLI API for JVM applications that connect to GigaSpaces Data Grids. The style is POSIX compliant.

To use the API in your application just add the following to your maven dependency list, then pass the args parameter from your main() method to XAPConfigCLI.parseArgs(args) method:

```
XAPConfigCLI xapConfigCLI = new XAPConfigCLI();
XAPOptions xapOptions = xapConfigCLI.parseArgs(args);
```

To obtain parameters later on in your application's execution, you can:

1) Use CommandLine object

```
Boolean multicastEnabled = (Boolean) xapOptions.getCommandLine().getParsedOptionValue(XAPOptions.MULTICAST_ENABLED);
```

2) Use getters of XAPOptions

```
Optional<Boolean> multicastEnabled = xapOptions.getMulticastEnabled();
```

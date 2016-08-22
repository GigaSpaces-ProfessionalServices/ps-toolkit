# XAP Config CLI

To use XAP Config CLI in your app just add it to dependency list and pass args parameter from your main() method to XAPConfigCLI.parseArgs(args) method.

XAPConfigCLI xapConfigCLI = new XAPConfigCLI();
XAPOptions xapOptions = xapConfigCLI.parseArgs(args);

There're 2 ways to obtain parameters:
1) Use CommandLine object

Boolean multicastEnabled = (Boolean) xapOptions.getCommandLine().getParsedOptionValue(XAPOptions.MULTICAST_ENABLED);

2) Use getters of XAPOptions

Optional<Boolean> multicastEnabled = xapOptions.getMulticastEnabled();
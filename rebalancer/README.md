# Rebalancing Tool
Rebalancing tool is stateless Processing Unit that tries to keep other PUs evenly distributed across the grid.
Tool maintains PU zones configuration, GSA zones configuration to find overloaded/underloaded GSAs and relocates PU instances to make GSAs equally loaded. Both statefull and stateless processing units can be balanced with a help of Rebalancing tool. I case of statefull PU tool takes care of high availability and doesn't put primary and backup to the same node.

### Config
Rebalancing tool doesn't require any additional configuration. It just has to be deployed to the grid and then tool finds locators/groups from system properties or grid configuration.

### Rebalancing tool runs:
* when Rebalancing Tool is deployed;
* when GSA is added to the grid;
* when GSA is removed from the grid.

{{% note "Important Note"%}}
The patterns and source code available in this repository are provided as is. While most of them are used in real life use cases and productions environments, and GigaSpaces is making its best effort to keep them up to date, they should not be considered as fully productized artifacts, and you should test them in your own environment before using them.
{{% /note %}}

The Apache v2 license applies. Here's [the fine print](../license.txt).
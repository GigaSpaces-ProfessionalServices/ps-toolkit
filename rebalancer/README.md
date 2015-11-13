# Rebalancing Tool
Rebalancing tool is stateless Processing Unit that tries to keep other PUs evenly distributed across the grid.
Tool maintains PU zones configuration, GSA zones configuration to find overloaded/underloaded GSAs and relocates PU instances to make GSAs equally loaded. Both statefull and stateless processing units can be balanced with a help of Rebalancing tool. I case of statefull PU tool takes care of high availability and doesn't put primary and backup to the same node.

### Config
Rebalancing tool doesn't require any additional configuration. It just has to be deployed to the grid and then tool finds locators/groups from system properties or grid configuration.

### Rebalancing tool runs:
* when Rebalancing Tool is deployed;
* when GSA is added to the grid;
* when GSA is removed from the grid.

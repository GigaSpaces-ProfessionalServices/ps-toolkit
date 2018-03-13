# Rebalancing Tool
Rebalancing tool is stateful Processing Unit that tries to keep other PUs evenly distributed across the grid.
Tool maintains PU zones configuration, GSA zones configuration to find overloaded/underloaded GSAs and relocates 
PU instances to make GSAs equally loaded. Both stateful and stateless processing units can be balanced with a help of 
Rebalancing tool. In case of stateful PU tool takes care of high availability and doesn't put primary and backup to the same node.
Tool rebalances first primaries by locating empty and low loaded containers, and then, according to the primaries hosts, rebalances backups.

### Config
Rebalancing tool doesn't require any additional configuration. It just has to be deployed to the grid and then tool finds locators/groups 
from system properties or grid configuration.

### Rebalancing tool runs:
* when Rebalancing Tool is deployed;
* when GSA is added to the grid;
* when GSA is removed from the grid.
* when tool switched on (only for XAP Manager)

### Plugin
If XAP manager is used, with REST API and ZooKeeper, the plugin submodule can be built and put to specific directory to add 
ability enable/disable rebalancing tool without redeploy. Plugin also persists tool state (ON/OFF) into ZooKeeper, so if 
the tool will be restarted, it's state saved.
Used endpoints:

<i>http://<xap_host>:8090/v1/controller/start?appName=grid-rebalancing<br>
http://<xap_host>:8090/v1/controller/stop?appName=grid-rebalancing</i>

where <b>appName</b> parameter is the actual name of tool (if tool was deployed with -override-name option and has different name).
By hitting <i>start</i> endpoint, tool will be triggered to rebalance instances.


> The patterns and source code available in this repository are provided as is. While most of them are used in real life use cases and productions environments, and GigaSpaces is making its best effort to keep them up to date, they should not be considered as fully productized artifacts, and you should test them in your own environment before using them.

The Apache v2 license applies. Here's [the fine print](../license.txt).
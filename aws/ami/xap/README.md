# XAP Grid Scripts

* configure.sh - sets required environment variables to start the grid such as: JAVA_HOME, NIC_ADDR, LOOKUPGROUPS, LOOKUPLOCATORS, etc.
* configure-compute-nodes.sh - invokes configure.sh on remote hosts
* start-mgt.sh - starts 1 LUS, 1 GSA and 1 GSM
* start-gscs.sh - starts requested number of GSCs
* start-remote-gscs.sh - starts requested number of GSCs remotely
* start-webui.sh - starts web management console
* stop-mgt.sh - stops GSA, that has been started by 'start-mgt.sh' script
* stop-gscs.sh - stops GSA, that manages GSCs that have been started by 'start-gscs.sh' script
* stop-webui.sh - stops web management console

If you need more details regarding the scripts, navigate to [PST-17](http://10.8.1.184:8080/issue/PST-17)
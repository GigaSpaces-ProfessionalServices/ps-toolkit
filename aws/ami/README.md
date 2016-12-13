{{% note "Important Note"%}}
The patterns and source code available in this repository are provided as is. While most of them are used in real life use cases and productions environments, and GigaSpaces is making its best effort to keep them up to date, they should not be considered as fully productized artifacts, and you should test them in your own environment before using them.
{{% /note %}}

The Apache v2 license applies. Here's [the fine print](../license.txt).

 Each sub-directory contains scripts to build and configure three types of AMI:
  
  1. Driver AMI: An AMI for administering XAP grids on EC2. It is installed with ~/scripts that create, update, and manage XAP grids. (Most of the action is here.)
  1. XAP/MGT AMI (xap directory): An AMI that can be used to XAP services across multiple XAP builds. Contains local configuration and start/stop scripts for the XAP installations installed on the AMI.
  1. Grafana/Influx: An AMI that can be used to boot Grafana, Influx, and Grafana+Influx VMs. 
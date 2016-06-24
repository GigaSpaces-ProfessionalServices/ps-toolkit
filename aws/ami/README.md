 Each sub-directory contains scripts to build and configure three types of AMI:
  
  1. Driver AMI: An AMI for administering XAP grids on EC2. It is installed with ~/scripts that create, update, and manage XAP grids. (Most of the action is here.)
  1. XAP/MGT AMI (xap directory): An AMI that can be used to XAP services across multiple XAP builds. Contains local configuration and start/stop scripts for the XAP versions supported by the AMI itself.
  1. Grafana/Influx: An AMI that can be used to boot Grafana, Influx, and Grafana+Influx VMs. 
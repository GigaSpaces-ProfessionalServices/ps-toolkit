> The patterns and source code available in this repository are provided as is. While most of them are used in real life use cases and productions environments, and GigaSpaces is making its best effort to keep them up to date, they should not be considered as fully productized artifacts, and you should test them in your own environment before using them.

The Apache v2 license applies. Here's [the fine print](../license.txt).

### Driver scripts

This directory contains scripts that configure existing XAP grids by generating configuration files that are external to Processing Units and pushing them out to XAP/MGT VMs.

Each script should also provide a description of its behavior in its help output: `./script.sh --help`

#### InfluxDB / Grafana installation 

##### Considerations before you start

To start with, you can run the InfluxDB and Grafana processes from the same VM. Or you can start two copies of the InfluxDB/Grafana AMI and split responsibilities. This might be advisable for large grids where the amount of data being sent to InfluxDB would require a large disk partition to accommodate it. We are not likely to encounter this any time soon, but it is possible for Performance Testing activities that it might come up in the future. Also: Be aware that Grafana itself can be backed up to PgSQL, H2, or MySQL databases. At present it is using the default (H2). 

It is not necessarily clear whether one should perform the recipe for installation before or after starting an XAP grid. This is because the configurations have a circular dependency in them when the VM creation lifecycle is defined as **start VM, run XAP or metrics components.** This is because IPs and hostnames cannot usually be known ahead of VM startup in cloud environments.
 
Therefore, if you were to start the XAP node(s) first, you would not know the hostnames of the Grafana and InfluxDBs to point them at in `metrics.xml` (they haven't been created yet).
  
On the other hand, if you were to create the InfluxDB+Grafana nodes first, you would have to update the metrics.xml files on all XAP/MGT nodes **before XAP startup** but **after VM creation**.

There are nuanced (i.e. complicated) ways of working around this, but it's easy enough to just restart the XAP grid after pushing the metrics.xml files. 

##### Software install recipe

1. `rm /tmp/grafana.ini /tmp/metrics.xml`
<br/>
<br/>This is a safety measure for preventing unexpected - but not necessarily buggy - results in config.
<br/>
<br/>
1. `./create-metrics-xml.sh --help`
<br/>
<br/>Follow instructions.
<br/>
<br/>
1. `./create-grafana-ini.sh --help`
<br/>
<br/>Follow instructions.
<br/>
<br/>
1. (optional): check the contents of `/tmp/grafana.ini` and `/tmp/metrics.xml` to make sure they match your expectations.
<br/>
<br/>
<br/>
1. `./push-metrics-xml.sh --help`
<br/>
<br/>Follow instructions.
<br/>
<br/>
1. `./push-grafana-ini.sh --help`
<br/>
<br/>Follow instructions.
<br/>
<br/>
1. But we're not done!

##### More configuration (manual) recipe

1. Navigate to http://\<grafana-host\>:3000/ then click on Datasources -> Add Datasource
<br/>
<br/>
<br/>
![DATASRC](./resources/docs/datasource.png)
<br/>
<br/>
<br/>
1. Start XAP
1. Start WEB UI
<br/>
<br/>
Browse to http://\<mgt-ui-host\>:8099/ and login (with empty username and empty password, unless you have added them to InfluxDB).
Check the logs on \<mgt-ui-host\> (usually, they're at /opt/gigaspaces/current/logs/start-webui.log). 
There **should be** a mention that a Grafana datasource is already defined.
<br/>
<br/>
1. Deploy any XAP application<br/>
<br/>
+> (optional - build your own app from maven): `some-directory% mvn os:create -Dtemplate=basic`<br/>
+> (optional - deploy the maven app to XAP): WEB-UI -> Processing Units -> Deploy Button -> file chooser to `some-directory/target/my-app-processor.jar`<br/>
<br/>
1. Use dialogs -> ... -> Deploy<br/>
1. Click on Monitoring TAB<br/>
<tr/>
**Congrats!** It took only required that much work to get monitoring working in WEB UI.
<br/>
<br/>
<br/>
![WEB-UI monitoring tab](./resources/docs/monitoring.png)
<br/>
<br/>
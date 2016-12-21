> The patterns and source code available in this repository are provided as is. While most of them are used in real life use cases and productions environments, and GigaSpaces is making its best effort to keep them up to date, they should not be considered as fully productized artifacts, and you should test them in your own environment before using them.

The Apache v2 license applies. Here's [the fine print](../license.txt).

The Grafana/Influx AMI requires no special scripts to administer. (It does require some manual configuration using those tools' Admin UIs).

The `build_grafana_influx.sh` script can be used to install InfluxDB 0.13 and/or Grafana on a newly provisioned Ubuntu 14.04 VM.
# Network outage error monitor script

This script searches for log files (base folder must be specified in `BASE_LOG_DIR` variable; logs filename regexes must be specified in `LOG_FILE_REGEXES` variable) and detects exceptions related to replication channel or LUS lookup disconnections.
Detected Exceptions are printed to stdout and stderr.
Also it detects system recovery messages and prints them to stdout\stderr as well.



If you need more details regarding the script, navigate to [BW-1](http://10.8.1.184:8080/issue/BW-1)
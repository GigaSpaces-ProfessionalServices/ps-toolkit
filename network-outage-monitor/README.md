# DataGrid log scanner

> The patterns and source code available in this repository are provided as is. While most of them are used in real life use cases and productions environments, and GigaSpaces is making its best effort to keep them up to date, they should not be considered as fully productized artifacts, and you should test them in your own environment before using them.

The Apache v2 license applies. Here's [the fine print](../license.txt).

Scans log files for REGEXes 

#####To implement

1. Edit the first three variables in [this file](./network-outage-error-finder.sh#L5) 

2. Run

+ Matching messages are reported to:
    * ```stderr```
    * ```stdout```
    * ```${OUTPUT_FILE_NAME}```

3. Parse or analyze

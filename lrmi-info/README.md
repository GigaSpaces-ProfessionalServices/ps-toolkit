# LRMI Info Tool

#####Reports LRMI statistics

######To use:

1. build

```bash
mvn clean package
```

2. run

```bash
java -jar target\lrmi-info-<version>.jar -h  
```
 
Statistics will be piped to ```stdout``` as csv. 

(**Note:** This is quite usable csv, so Excel may be a good option.) 

3. Point ```parse_lrmi_file.py``` at the log file to get graphical output.

{{% note "Important Note"%}}
The patterns and source code available in this repository are provided as is. While most of them are used in real life use cases and productions environments, and GigaSpaces is making its best effort to keep them up to date, they should not be considered as fully productized artifacts, and you should test them in your own environment before using them.
{{% /note %}}

The Apache v2 license applies. Here's [the fine print](../license.txt).
# LRMI Info Tool

#####Reports LRMI statistics

######To implement

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
# inspector

#####Reports org.openspaces.core.GigaSpace method calls
 
######Default statistics
 
- Exponential Moving Average
- Absolute Minimum
- Absolute Maximum
- 50th, 90th, 95th, 99th and 99.9th percentiles using 
    * piecewise constant function approximation 
    * t-Digest algorithm

######To implement

1. Add jar file to the client application's classpath (e.g. catalina_home/common/lib or bundled in war file)

1. Tell ProcessingUnit to scan `com.gigaspaces.gigapro.inspector` using
```
<context:component-scan base-package="your.app.directory,com.gigaspaces.gigapro.inspector"/>
```
or
 ```
 @ComponentScan({"com.gigaspaces.gigapro.inspector","your.app.directory"})
 ```

Statistics will be piped to ```stdout``` **and** ```${sys:java.io.tmpdir}/logs/datagrid_statistics.log```.
  
3. Point ```parse_inspector_file.py``` at the log file to get graphical output.  

##### Optional

JVM param   | default
---   |   ---
LOG_FREQUENCY|1000
MEASURES|ema,percentile,min,max,percentile_tdigest
DATASET_SIZE|1024
HEAD_SIZE|1024
TAIL_SIZE|1024

##### Math facts

###### Exponential Moving Average (EMA)

PsInspector computes EMA using `St=αyt−1+(1−α)St−1` formula where `St` is the smoothed value and `α` is a smoothing constant that was chosen as α=0.5

###### Min, Max

Minimum and maximum are selected among top N minimum/maximum latest values of response time. The default buffer size is set to 1024 and can be changed using 
`"-DHEAD_SIZE=10000"` for maximum values queue size and `"-DTAIL_SIZE=10000"` system property for minimum values queue size.

###### Piecewise-constant percentile approximation

It uses the most recent data to approximate the probability distribution. The buffer size can be customized using `"-DDATASET_SIZE=10000"` otherwise the default value of 1024 is used. The assumption is that probability density can be approximated by piecewise-constant function.

###### t-Digest percentile approximation

t-Digest algorithm is used for calculating approximate quantiles. Please, find more details on [https://github.com/tdunning/t-digest]().

> The patterns and source code available in this repository are provided as is. While most of them are used in real life use cases and productions environments, and GigaSpaces is making its best effort to keep them up to date, they should not be considered as fully productized artifacts, and you should test them in your own environment before using them.

The Apache v2 license applies. Here's [the fine print](../license.txt).

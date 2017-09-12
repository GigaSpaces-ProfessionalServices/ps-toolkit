These python scripts can be used to generate a csv file that lists the types of stop the world pauses. It also scans for certain keywords related to gc tuning.

..* See: [https://blogs.oracle.com/poonam/entry/understanding_g1_gc_logs] for a description of stop the world pauses.
..* See: [http://www.oracle.com/technetwork/articles/java/g1gc-1984535.html] for information on what to tune for.

These scripts have help available. See examples below.

..* If the JVM is passed -XX:+PrintAdaptiveSizePolicy then the gc log entries will span multiple lines.
..* If the JVM is passed -XX:+PrintGCDateStamps, the log entries will begin with a date time field.

To keep it simple separate scripts were created.

TODO: merge the scripts and reduce common code.

```python
> python gc-adaptive.py --help
usage: gc-adaptive.py [--start_dir START_DIR] [--output_dir OUTPUT_DIR]
                      [--log_level {CRITICAL,ERROR,WARNING,INFO,DEBUG}]
                      [--hosts HOSTS] [-h]

optional arguments:
  --start_dir START_DIR
                        the root directory to begin processing
  --output_dir OUTPUT_DIR
                        where the output file should be written to. By default
                        the output file will be located in a user's home
                        directory.
  --log_level {CRITICAL,ERROR,WARNING,INFO,DEBUG}
                        logging level
  --hosts HOSTS         list of hosts, separated by commas
  -h, --help            This program parses a gc log file and provides a
                        summary in csv format. The following JVM options
                        should be used to generate the log file:
                        -Xloggc:/path/to/file/gc_%p.log
                        -XX:+PrintCommandLineFlags -XX:+PrintGC
                        -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
                        -XX:+PrintAdaptiveSizePolicy
                        -XX:+PrintTenuringDistribution -XX:+PrintReferenceGC
                        
> python gc-adaptive-datetime.py --help
usage: gc-adaptive-datetime.py [--start_dir START_DIR]
                               [--output_dir OUTPUT_DIR]
                               [--log_level {CRITICAL,ERROR,WARNING,INFO,DEBUG}]
                               [--hosts HOSTS] [-h]

optional arguments:
  --start_dir START_DIR
                        the root directory to begin processing
  --output_dir OUTPUT_DIR
                        where the output file should be written to. By default
                        the output file will be located in a user's home
                        directory.
  --log_level {CRITICAL,ERROR,WARNING,INFO,DEBUG}
                        logging level
  --hosts HOSTS         list of hosts, separated by commas
  -h, --help            This program parses a gc log file and provides a
                        summary in csv format. The following JVM options
                        should be used to generate the log file:
                        -Xloggc:/path/to/file/gc_%p.log
                        -XX:+PrintCommandLineFlags -XX:+PrintGC
                        -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
                        -XX:+PrintGCDateStamps -XX:+PrintAdaptiveSizePolicy
                        -XX:+PrintTenuringDistribution -XX:+PrintReferenceGC                        
```

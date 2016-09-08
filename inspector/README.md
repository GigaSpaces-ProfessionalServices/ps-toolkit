# PsInspector

PsInspector provides proxy implementation for logging statistics of executing methods from `org.openspaces.core.GigaSpace`.
It hosts the functionality mentioned in [PST-16](http://10.8.1.184:8080/issue/PST-16), [PST-22](http://10.8.1.184:8080/issue/PST-22).
PsInspector jar can be placed to the XAP classpath, e.g. by copying the jar with dependencies to $XAP_HOME/lib/optional/pu-common directory.
All calls to `org.openspaces.core.GigaSpace` will be wrapped by `com.gigaspaces.gigapro.inspector.SpaceStatisticsRecordingProxy`.


To make PsInspector work in the client app the following changes must be made in the configuration of the app:
- For xml based configuration add `<context:component-scan base-package="com.base.app, com.gigaspaces.gigapro.inspector"/>` to pu.xml
- For Java based configuration add `@ComponentScan({"com.base.app", "com.gigaspaces.gigapro.inspector"})` to configuration class

Default log frequency is 1000 (on every 1000th call to GigaSpace the latest statistics will be logged), but it can be changed by setting parameter "LOG_FREQUENCY" (`"-DLOG_FREQUENCY=10000"`)
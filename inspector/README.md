# PsInspector

PsInspector allows to log response time statistics of `org.openspaces.core.GigaSpace` methods with a specific logging frequency per space on client side. 
To make PsInspector work the following changes must be made in the configuration of the client application:

- For xml based configuration add `<context:component-scan base-package="com.base.app, com.gigaspaces.gigapro.inspector"/>` to the application context
- For Java based configuration add `@ComponentScan({"com.base.app", "com.gigaspaces.gigapro.inspector"})` to configuration class.

And PsInspector jar should be added to the client application classpath in runtime. It is built with dependencies except gs-runtime and spring jars.

Default log frequency is 1000 (on every 1000th call to GigaSpace the latest statistics will be logged), but it can be changed by passing system property `"-DLOG_FREQUENCY=10000"`.

PsInspector calculates the following statistics by default:

- Exponential Moving Average
- Absolute Minimum
- Absolute Maximum
- Median(50th), 90th, 95th, 99th and 99.9th percentiles using 
  * piecewise constant function approximation 
  * t-Digest algorithm

To customize what statistics is logged you can pass `-DMEASURES` system property, that is a comma separated list of measurement types: 'ema', 'min', 'max', 'percentile' (based on piecewise constant function approximation), and 'percentile_tdigest'. For example `"-DMEASURES=ema,percentile"` enables calculation of exponential moving average and percentiles.

## Exponential Moving Average (EMA)

PsInspector computes EMA using `St=αyt−1+(1−α)St−1` formula where `St` is the smoothed value and `α` is a smoothing constant that was chosen as α=0.5

## Absolute Min/Max

Minimum and maximum are selected among top N minimum/maximum latest values of response time. The default buffer size is set to 1024 and can be changed using 
`"-DHEAD_SIZE=10000"` for maximum values queue size and `"-DTAIL_SIZE=10000"` system property for minimum values queue size.

## Percentiles

It uses the most recent data to approximate the probability distribution. The buffer size can be customized using `"-DDATASET_SIZE=10000"` otherwise the default value of 1024 is used. The assumption is that probability density can be approximated by piecewise-constant function.

## Percentiles based on t-Digest algorithm

t-Digest algorithm is used for calculating approximate quantiles. Please, find more details on https://github.com/tdunning/t-digest.
package com.gigaspaces.gigapro.inspector.measure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;


public class TimedExponentialMovingAverage implements StatisticalMeasure {

    private final double alpha = 0.5;
    private double accumulator;
    private boolean initialized;
    
    private static final Logger LOG = LoggerFactory.getLogger("ps-inspector");

    @Override
    public synchronized void addValue(long metricValue) {
        if (!initialized) {
            initialized = true;
            accumulator = metricValue;
        } else {
            accumulator = (alpha * metricValue) + (1.0 - alpha) * accumulator;
        }
    }

    @Override
    public synchronized Double getResult() {
        return accumulator / ONE_MILLISECOND;
    }

    @Override
    public void logStatistics() {
        LOG.info(format(FORMAT, "EMA", getResult()));
    }
}

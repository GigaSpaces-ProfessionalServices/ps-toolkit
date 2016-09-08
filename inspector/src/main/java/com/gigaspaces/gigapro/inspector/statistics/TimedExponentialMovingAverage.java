package com.gigaspaces.gigapro.inspector.statistics;

import java.util.Collection;

import static org.apache.commons.collections.CollectionUtils.isEmpty;

class TimedExponentialMovingAverage {

    private double accumulator;
    private double alpha = 0.5;
    private long invocationCount;
    private boolean initialized = false;
    private long startTime;

    TimedExponentialMovingAverage() {
        begin();
    }

    private TimedExponentialMovingAverage(double accumulator, long invocationCount) {
        this.accumulator = accumulator;
        this.invocationCount = invocationCount;
    }

    void begin() {
        startTime = System.currentTimeMillis();
    }

    void end() {
        invocationCount++;
        long endTime = System.currentTimeMillis();
        long delta = endTime - startTime;
        if (!initialized) {
            accumulator = delta;
            initialized = true;
        } else {
            accumulator = (alpha * delta) + (1.0 - alpha) * accumulator;
        }
    }

    static TimedExponentialMovingAverage combineMultiple(Collection<TimedExponentialMovingAverage> averages) {
        if (isEmpty(averages))
            throw new IllegalArgumentException("Collection averages must not be empty!");

        long totalInvocations = 0;
        double sumOfAverages = 0.0;
        for (TimedExponentialMovingAverage stat : averages) {
            totalInvocations += stat.getInvocationCount();
            sumOfAverages += stat.getAverageValue();
        }
        return new TimedExponentialMovingAverage(sumOfAverages / averages.size(), totalInvocations);
    }

    double getAverageValue() {
        return accumulator;
    }

    long getInvocationCount() {
        return invocationCount;
    }
}

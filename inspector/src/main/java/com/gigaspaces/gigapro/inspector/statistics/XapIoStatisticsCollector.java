package com.gigaspaces.gigapro.inspector.statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static com.gigaspaces.gigapro.inspector.statistics.SpaceIoOperation.validateParamsNotNull;
import static com.gigaspaces.gigapro.inspector.statistics.TimedExponentialMovingAverage.combineMultiple;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.getProperty;
import static java.lang.Thread.currentThread;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;

public class XapIoStatisticsCollector {
    private static final Logger LOG = LoggerFactory.getLogger("ps-inspector");
    private static final int DEFAULT_LOG_FREQUENCY = 1000;
//    private static final int DEFAULT_LOG_FREQUENCY = 1048576;
    private static Map<Thread, HashMap<SpaceIoOperation, TimedExponentialMovingAverage>> allThreadsStatistics = new ConcurrentHashMap<>();
    private final AtomicLong operationsCounter = new AtomicLong(0);
    private final int logFrequency;
    private String processingUnitName;

    public XapIoStatisticsCollector(String spaceName) {
        this.processingUnitName = spaceName;

        String logFrequency = getProperty("LOG_FREQUENCY");
        if (isBlank(logFrequency)) {
            LOG.warn(format("Property LOG_FREQUENCY is not set! Default value [%d] will be set.", DEFAULT_LOG_FREQUENCY));
            this.logFrequency = DEFAULT_LOG_FREQUENCY;
        } else if (!isNumeric(logFrequency)) {
            LOG.warn(format("Property LOG_FREQUENCY [%s] is not a number! Default value [%d] will be set.", logFrequency, DEFAULT_LOG_FREQUENCY));
            this.logFrequency = DEFAULT_LOG_FREQUENCY;
        } else {
            this.logFrequency = parseInt(logFrequency);
            LOG.info(format("Log frequency is set to [%s].", logFrequency));
        }
    }

    /**
     * Starts recording statistics for operation
     *
     * @param spaceIoOperation SpaceIoOperation
     * @throws IllegalArgumentException if any of params is null
     */
    public void operationStarted(SpaceIoOperation spaceIoOperation) {
        TimedExponentialMovingAverage avg = getLocalAverageStatistics(spaceIoOperation);
        if (avg == null) {
            avg = new TimedExponentialMovingAverage();
            avg.begin();
            putLocalAverageStatistics(spaceIoOperation, avg);
        } else {
            avg.begin();
        }
    }

    /**
     * Finish recording statistics for operation
     *
     * @param spaceIoOperation SpaceIoOperation
     * @throws IllegalArgumentException if any of params is null
     */
    public void operationFinished(SpaceIoOperation spaceIoOperation) {
        TimedExponentialMovingAverage avg = getLocalAverageStatistics(spaceIoOperation);
        if (avg == null)
            throw new IllegalStateException("Misuse of XapIoStatisticsCollector. You are telling me that a method returned before it was called for: " + getDescription(spaceIoOperation));
        avg.end();
        operationsCounter.incrementAndGet();
        logIfNecessary();
    }

    TimedExponentialMovingAverage getLocalAverageStatistics(SpaceIoOperation spaceIoOperation) {
        HashMap<SpaceIoOperation, TimedExponentialMovingAverage> statistics = allThreadsStatistics.get(currentThread());
        if (statistics == null) {
            statistics = new HashMap<>();
            allThreadsStatistics.put(currentThread(), statistics);
            return null;
        }
        return statistics.get(spaceIoOperation);
    }

    private void putLocalAverageStatistics(SpaceIoOperation spaceIoOperation, TimedExponentialMovingAverage avg) {
        HashMap<SpaceIoOperation, TimedExponentialMovingAverage> statistics = allThreadsStatistics.get(currentThread());
        if (statistics == null) {
            statistics = new HashMap<>();
            allThreadsStatistics.put(currentThread(), statistics);
        }
        statistics.put(spaceIoOperation, avg);
    }

    private void logIfNecessary() {
        if (shouldLog())
            logStatistics();
    }

    private boolean shouldLog() {
        long localCounter = operationsCounter.get();
        long totalReads = totalReadsCount();
        return totalReads != 0 && totalReads % logFrequency == 0 && localCounter == operationsCounter.get();
    }

    private long totalReadsCount() {
        long totalReads = 0;
        for (TimedExponentialMovingAverage avg : collectAllThreadsStatistics().values()) {
            totalReads += avg.getInvocationCount();
        }
        return totalReads;
    }

    private void logStatistics() {
        LOG.info("XAP IO Statistics:");
        Map<SpaceIoOperation, TimedExponentialMovingAverage> allThreadsStatistics = collectAllThreadsStatistics();
        for (SpaceIoOperation operation : allThreadsStatistics.keySet()) {
            TimedExponentialMovingAverage avg = allThreadsStatistics.get(operation);
            LOG.info(format("For %s:\t count = %d, average = %.1f ms", getDescription(operation), avg.getInvocationCount(), avg.getAverageValue()));
        }
    }

    private Map<SpaceIoOperation, TimedExponentialMovingAverage> collectAllThreadsStatistics() {
        Map<SpaceIoOperation, TimedExponentialMovingAverage> combined = new HashMap<>();
        Map<SpaceIoOperation, ArrayList<TimedExponentialMovingAverage>> allStats = new HashMap<>();

        allThreadsStatistics.values().forEach(map -> map.forEach((operation, stats) -> {
            if (allStats.putIfAbsent(operation, newArrayList(stats)) != null)
                allStats.get(operation).add(stats);
        }));
        allStats.forEach((operation, stats) -> combined.put(operation, combineMultiple(stats)));
        return combined;
    }

    private String getDescription(SpaceIoOperation spaceIoOperation) {
        return getDescription(spaceIoOperation.getClass(), spaceIoOperation.getOperation(), spaceIoOperation.getOperationType(), spaceIoOperation.getOperationModifier());
    }

    private String getDescription(Class<?> spaceClass, IoOperation operation, IoOperationType operationType, IoOperationModifier operationModifier) {
        validateParamsNotNull(spaceClass, operation, operationType, operationModifier);

        return format("Space [%s], operation = %-14s, operationType = %-8s, operationModifier = %-9s, space class = %s",
                processingUnitName, operation.toString(), operationType.toString(), operationModifier.toString(), spaceClass.getSimpleName());
    }
}
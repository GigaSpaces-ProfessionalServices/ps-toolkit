package com.gigaspaces.gigapro.inspector.statistics;

import com.gigaspaces.gigapro.inspector.measure.StatisticalMeasure;
import com.gigaspaces.gigapro.inspector.measure.StatisticalMeasureFactory;
import com.gigaspaces.gigapro.inspector.model.SpaceIoOperation;
import com.gigaspaces.gigapro.inspector.utils.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class XapIoStatisticsCollector implements StatisticsCollector {

    private final static StatisticalMeasureFactory statisticsMetricFactory = new StatisticalMeasureFactory();

    private final ConcurrentMap<SpaceIoOperation, List<StatisticalMeasure>> statistics = new ConcurrentHashMap<>();
    
    private ThreadLocal<Long> startTime = new ThreadLocal<Long>();
    private final AtomicLong operationsCounter = new AtomicLong(0);
    
    private final int logFrequency = Configuration.getLogFrequency();

    private static final Logger LOG = LoggerFactory.getLogger("ps-inspector");
    
    @Override
    public void operationStarted(SpaceIoOperation spaceIoOperation) {
        startTime.set(System.nanoTime());
    }

    @Override
    public void operationFinished(SpaceIoOperation spaceIoOperation) {
        Long startTs = startTime.get();
        if (startTs == null) {
            throw new IllegalStateException("Misuse of XapStatisticsCollector. 'operationStarted' method should be called at first");
        }
        final long latency = System.nanoTime() - startTs;

        statistics.computeIfAbsent(spaceIoOperation, (SpaceIoOperation) -> Collections.unmodifiableList(statisticsMetricFactory.create(Configuration.getMeasureTypes())))
            .forEach(m -> m.addValue(latency));
        logIfNeeded(operationsCounter.incrementAndGet());
    }

    @Override
    public void logIfNeeded(long invocationCount) {
        if (invocationCount != 0 && invocationCount % logFrequency == 0) {
            LOG.info("XAP IO Statistics for count = {}:", invocationCount);
            for (Entry<SpaceIoOperation, List<StatisticalMeasure>> entry : statistics.entrySet()) {
                SpaceIoOperation operation = entry.getKey();
                LOG.info(format("Space = %s, operation = %s, operationType = %s, operationModifier = %s, space class = %s", operation.getSpaceName(), operation.getOperation(), operation.getOperationType(), operation.getOperationModifier(), operation.getTrackedClass().getSimpleName()));
                entry.getValue().forEach(m -> m.logStatistics());
                LOG.info("\n");
            }
        }
    }
    
    /**
     * For test usage only
     */
    long getInvocationCount() {
        return operationsCounter.get();
    }
    
}
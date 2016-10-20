package com.gigaspaces.gigapro.inspector.measure;

import com.gigaspaces.gigapro.inspector.utils.Configuration;
import com.google.common.collect.MinMaxPriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Queue;

import static java.lang.String.format;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class AbsoluteMinMax implements StatisticalMeasure {

    private final boolean isMax;
    private static final Logger LOG = LoggerFactory.getLogger("ps-inspector");

    private final Queue<Long> queue;

    public AbsoluteMinMax(boolean isMax) {
        this.isMax = isMax;
        this.queue = isMax ? MinMaxPriorityQueue.orderedBy(Collections.reverseOrder()).maximumSize(Configuration.getHeadSize()).<Long>create() 
                : MinMaxPriorityQueue.maximumSize(Configuration.getTallSize()).<Long>create();
    }

    @Override
    public void addValue(long metricValue) {
        synchronized (queue) {
            queue.offer(metricValue);
        }
    }

    @Override
    public Double getResult() {
        synchronized (queue) {
            return queue.peek() / ONE_MILLISECOND;
        }
    }

    @Override
    public void logStatistics() {
        LOG.info(format(FORMAT, isMax ? "MAX" : "MIN", getResult()));
    }
}

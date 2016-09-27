package com.gigaspaces.gigapro.inspector.measure;

import com.gigaspaces.gigapro.inspector.math.PercentileCalculator;
import com.gigaspaces.gigapro.inspector.utils.Configuration;
import com.google.common.collect.EvictingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.lang.String.format;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class Percentile implements StatisticalMeasure {

    private static final Logger LOG = LoggerFactory.getLogger("ps-inspector");

    private final Queue<Long> queue;
    private final PercentileCalculator percentileCalculator;

    public Percentile(PercentileCalculator percentileCalculator) {
        this.percentileCalculator = percentileCalculator;
        this.queue = EvictingQueue.create(Configuration.getDataSetSize());
    }

    @Override
    public void addValue(long metricValue) {
        synchronized (queue) {
            queue.offer(metricValue);
        }
    }

    private List<Long> getDataSet() {
        synchronized (queue) {
            return new ArrayList<Long>(queue);
        }
    }

    @Override
    public Object getResult() {
        Map<PercentileRatio, Double> resultMap = new EnumMap<>(PercentileRatio.class);
        List<Long> dataSet = getDataSet();
        for (PercentileRatio percentileRatio : PercentileRatio.values()) {
            Double result = percentileCalculator.calculate(dataSet, percentileRatio.getValue());
            resultMap.put(percentileRatio, result);
        }
        return resultMap;
    }

    @Override
    public void logStatistics() {
        List<Long> dataSet = getDataSet();
        LOG.info("Percentiles calculated using piecewise constant approximation: ");
        for (PercentileRatio percentileRatio : PercentileRatio.values()) {
            LOG.info(format(DECIMAL_FORMAT, percentileRatio, percentileCalculator.calculate(dataSet, percentileRatio.getValue())));
        }
    }
}

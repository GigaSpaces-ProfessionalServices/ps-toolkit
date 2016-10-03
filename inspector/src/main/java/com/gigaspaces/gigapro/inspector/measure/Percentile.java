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
    public Map<PercentileRatio, Double> getResult() {
        Map<PercentileRatio, Double> resultMap = new EnumMap<>(PercentileRatio.class);
        List<Long> dataSet = getDataSet();
        for (PercentileRatio percentileRatio : PercentileRatio.values()) {
            Double result = calculate(dataSet, percentileRatio);
            if (result != null) {
                resultMap.put(percentileRatio, result);
            }
        }
        return resultMap;
    }

    private Double calculate(List<Long> dataSet, PercentileRatio percentileRatio) {
        Double result = percentileCalculator.calculate(dataSet, percentileRatio.getValue());
        return result != null ? result / ONE_MILLISECOND : null;
    }
    
    @Override
    public void logStatistics() {
        List<Long> dataSet = getDataSet();
        LOG.info("Percentiles calculated using piecewise constant approximation: ");
        for (PercentileRatio percentileRatio : PercentileRatio.values()) {
            LOG.info(format(FORMAT, percentileRatio, calculate(dataSet, percentileRatio)));
        }
    }
}

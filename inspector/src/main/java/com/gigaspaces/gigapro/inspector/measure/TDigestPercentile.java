package com.gigaspaces.gigapro.inspector.measure;

import com.tdunning.math.stats.TDigest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

import static java.lang.String.format;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class TDigestPercentile implements StatisticalMeasure {

    private static final int compress = 100;
    
    private final TDigest digest = TDigest.createDigest(compress);

    private static final Logger LOG = LoggerFactory.getLogger("ps-inspector");
    
    @Override
    public void addValue(long metricValue) {
        synchronized (digest) {
            digest.add(metricValue);
        }
    }

    @Override
    public Map<PercentileRatio, Double> getResult() {
        Map<PercentileRatio, Double> resultMap = new EnumMap<>(PercentileRatio.class);
        for (PercentileRatio percentileRatio : PercentileRatio.values()) {
            resultMap.put(percentileRatio, calculatePercentile(percentileRatio));
        }
        return resultMap;
    }
    
    private double calculatePercentile(PercentileRatio percentileRatio) {
        synchronized(digest) {
            return digest.quantile(percentileRatio.getValue()) / ONE_MILLISECOND;
        }
    }

    @Override
    public void logStatistics() {
        LOG.info("Percentiles calculated using t-Digest: ");
        for (PercentileRatio percentileRatio : PercentileRatio.values()) {
            LOG.info(format(FORMAT, percentileRatio, calculatePercentile(percentileRatio)));
        }
    }
}

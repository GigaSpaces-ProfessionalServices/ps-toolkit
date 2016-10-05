package com.gigaspaces.gigapro.inspector.measure;

import com.gigaspaces.gigapro.inspector.utils.Configuration.MeasureType;
import com.gigaspaces.gigapro.inspector.math.PercentileCalculator;
import com.gigaspaces.gigapro.inspector.math.PiecewiseConstantPercentileCalculator;

import java.util.*;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class StatisticalMeasureFactory {

    public List<StatisticalMeasure> create(Set<MeasureType> measures) {
        Objects.requireNonNull(measures, "'measures' must not be null");
        List<StatisticalMeasure> statisticalMeasures = new ArrayList<>(measures.size());
        for (MeasureType measureType : measures) {
            statisticalMeasures.add(create(measureType));
        }
        return statisticalMeasures;
    }

    private StatisticalMeasure create(MeasureType measureType) {
        switch(measureType) {
            case EMA:                return getTimedExponentialMovingAverage();
            case MAX:                return geAbsoluteMax();
            case MIN:                return geAbsoluteMin();
            case PERCENTILE:         return getPercentile();
            case PERCENTILE_TDIGEST: return getTDigestPercentile();
            default: throw new UnsupportedOperationException("Unsupported measure type: " + measureType);
        }
    }
    public StatisticalMeasure getTimedExponentialMovingAverage() {
        return new TimedExponentialMovingAverage();
    }

    public StatisticalMeasure geAbsoluteMax() {
        return new AbsoluteMinMax(true);
    }

    public StatisticalMeasure geAbsoluteMin() {
        return new AbsoluteMinMax(false);
    }

    public StatisticalMeasure getTDigestPercentile() {
        return new TDigestPercentile();
    }

    public StatisticalMeasure getPercentile() {
        return new Percentile(getPercentileCalculator());
    }

    public PercentileCalculator getPercentileCalculator() {
        return new PiecewiseConstantPercentileCalculator();
    }
}

package com.gigaspaces.gigapro.inspector.measure;

import com.gigaspaces.gigapro.inspector.math.PercentileCalculator;
import com.gigaspaces.gigapro.inspector.math.PiecewiseConstantPercCalculator;

import java.util.Arrays;
import java.util.List;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class StatisticalMeasureFactory {

    public List<StatisticalMeasure> createAll() {
        return Arrays.asList(getTimedExponentialMovingAverage(), geAbsoluteMax(), geAbsoluteMin(), getPercentile(), getTDigestPercentile());
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
        return new PiecewiseConstantPercCalculator();
    }
}

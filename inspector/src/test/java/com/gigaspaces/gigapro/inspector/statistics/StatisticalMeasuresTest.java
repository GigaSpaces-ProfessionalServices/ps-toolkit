package com.gigaspaces.gigapro.inspector.statistics;

import com.gigaspaces.gigapro.inspector.math.PiecewiseConstantPercentileCalculator;
import com.gigaspaces.gigapro.inspector.measure.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.*;
/**
 * @author Svitlana_Pogrebna
 *
 */
public class StatisticalMeasuresTest {

    private TimedExponentialMovingAverage ema;
    private AbsoluteMinMax max;
    private AbsoluteMinMax min;
    private TDigestPercentile tDigestPercentile;
    private Percentile percentile;

    private List<StatisticalMeasure> measures;
    
    @Before
    public void setup() {
        this.ema = new TimedExponentialMovingAverage();
        this.max = new AbsoluteMinMax(true);
        this.min = new AbsoluteMinMax(false);
        this.tDigestPercentile = new TDigestPercentile();
        this.percentile = new Percentile(new PiecewiseConstantPercentileCalculator());
        
        measures = Arrays.asList(ema, max, min, tDigestPercentile, percentile);
    }

    @Test
    public void testMeasures() throws Exception {
        int size = 100;
        long[] values = new long[size];
        for (int i = 0; i < size; i++) {
            values[i] = i + 1;
        }

        double error = 1;
        addValue(values, 0, 1);
        validate(1, 1, 1, new double [] {1, 1, 1, 1, 1}, null, error);

        int expectedCount = size / 2;
        addValue(values, 1, expectedCount);

        double[] expectedPercentiles = {25.5, 45.5, 48, 50, 50};
        validate(49, 50, 1, expectedPercentiles, expectedPercentiles, error);

        addValue(values, expectedCount, values.length);

        expectedPercentiles = new double [] {50.5, 90.5, 95.9, 99.5, 99.9};
        validate(99, 100, 1, expectedPercentiles, expectedPercentiles, error);
    }

    @Test
    public void testMeasuresMultithreaded() throws InterruptedException {
        int size = 100;
        long[] values = new long[size];
        for (int i = 0; i < size; i++) {
            values[i] = i + 1;
        }

        CountDownLatch latch = new CountDownLatch(5);
        ExecutorService executorService = newFixedThreadPool(4);

        Runnable task = () -> {
            for (int i = 0; i < size; i++) {
                addValue(values, 0, size);
            }
            latch.countDown();
        };

        executorService.submit(task);
        executorService.submit(task);
        executorService.submit(task);
        executorService.submit(task);
        latch.countDown();
        latch.await();

        double error = 1;
        double[] expectedPercentiles = {50.5, 90.5, 95.9, 99.5, 99.9};
        validate(99, 100, 1, expectedPercentiles, expectedPercentiles, error);
    }

    private void validate(double expectedEma, long expectedMax, long expectedMin, double[] expectedTDigestPercentiles, double[] expectedPercentiles, double error) {
        assertThat(ema.getResult(), closeTo(expectedEma, error));
        assertThat(Double.valueOf(min.getResult()), closeTo(expectedMin, error));
        assertThat(Double.valueOf(max.getResult()), closeTo(expectedMax, error));
        Map<PercentileRatio, Double> percentiles = tDigestPercentile.getResult();
        validatePercentiles(percentiles, expectedTDigestPercentiles, error);
        percentiles = percentile.getResult();
        if (expectedPercentiles == null) {
            assertTrue(percentiles.isEmpty());
        } else {
            validatePercentiles(percentiles, expectedPercentiles, error);
        }
    }
    
    private void validatePercentiles(Map<PercentileRatio, Double> actual, double[] expected, double error) {
        assertThat(actual.get(PercentileRatio.MEDIAN), closeTo(expected[0], error));
        assertThat(actual.get(PercentileRatio.P90th), closeTo(expected[1], error));
        assertThat(actual.get(PercentileRatio.P95th), closeTo(expected[2], error));
        assertThat(actual.get(PercentileRatio.P99th), closeTo(expected[3], error));
        assertThat(actual.get(PercentileRatio.P999th), closeTo(expected[4], error));
    }
    
    private void addValue(long[] values, int start, int length) {
        measures.forEach((m) -> {
            for (int i = start; i < length; i++) {
                m.addValue(TimeUnit.MILLISECONDS.toNanos(values[i]));
            }
        });
    }
}

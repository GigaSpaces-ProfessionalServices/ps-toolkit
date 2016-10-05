package com.gigaspaces.gigapro.inspector.statistics;

import com.gigaspaces.gigapro.inspector.measure.StatisticalMeasure;
import com.gigaspaces.gigapro.inspector.measure.StatisticalMeasureFactory;
import com.gigaspaces.gigapro.inspector.model.SpaceIoOperation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.gigaspaces.gigapro.inspector.model.IoOperation.CHANGE;
import static com.gigaspaces.gigapro.inspector.model.IoOperationModifier.NONE;
import static com.gigaspaces.gigapro.inspector.model.IoOperationType.SQL;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

@RunWith(JMockit.class)
public class XapIoStatisticsCollectorTest {

    @Mocked
    private StatisticalMeasureFactory statisticalMeasureFactory;
    private XapIoStatisticsCollector statisticsCollector;

    @Before
    public void setup() {
        this.statisticsCollector = new XapIoStatisticsCollector();
    }

    @Test
    public void testXapIoStatistics() throws Exception {
        int size = 100;
        TestStatisticalMeasure statisticalMeasure = new TestStatisticalMeasure(size);

        new Expectations() {
            {
                statisticalMeasureFactory.create(withNotNull());
                result = Collections.singletonList(statisticalMeasure);
            }
        };

        SpaceIoOperation spaceIoOperation = new SpaceIoOperation("space", Object.class, CHANGE, SQL, NONE);
        long[] sleeps = new long[size];
        for (int i = 0; i < size; i++) {
            sleeps[i] = i + 1;
        }

        perform(statisticsCollector, spaceIoOperation, sleeps[0]);
        assertThat(statisticsCollector.getInvocationCount(), is(1l));

        int expectedCount = size / 2;
        for (int i = 1; i < expectedCount; i++) {
            perform(statisticsCollector, spaceIoOperation, sleeps[i]);
        }

        assertThat(statisticsCollector.getInvocationCount(), is((long) expectedCount));

        for (int i = expectedCount; i < size; i++) {
            perform(statisticsCollector, spaceIoOperation, sleeps[i]);
        }

        assertThat(statisticsCollector.getInvocationCount(), is((long) size));
        validate(sleeps, statisticalMeasure.getResult(), 1);
    }

    @Test
    public void testXapIoStatisticsMultithreaded() throws InterruptedException {
        int size = 100;
        new Expectations() {
            {
                statisticalMeasureFactory.create(withNotNull());
                result = Collections.emptyList();
            }
        };

        SpaceIoOperation spaceIoOperation = new SpaceIoOperation("space", Object.class, CHANGE, SQL, NONE);

        long[] sleeps = new long[size];
        for (int i = 0; i < size; i++) {
            sleeps[i] = i + 1;
        }

        CountDownLatch latch = new CountDownLatch(5);
        ExecutorService executorService = newFixedThreadPool(4);

        Runnable task = () -> {
            for (int i = 0; i < size; i++) {
                perform(statisticsCollector, spaceIoOperation, sleeps[i]);
            }
            latch.countDown();
        };

        executorService.submit(task);
        executorService.submit(task);
        executorService.submit(task);
        executorService.submit(task);
        latch.countDown();
        latch.await();

        assertThat(statisticsCollector.getInvocationCount(), is((long) 4 * size));
    }

    public void validate(long[] expected, long[] actual, double error) {
        for (int i = 0; i < expected.length; i++) {
            assertThat(Double.valueOf(actual[i]) / 1_000_000.0, closeTo(expected[i], error));
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testOperationFinishedBeforeStarted() {
        XapIoStatisticsCollector statisticsCollector = new XapIoStatisticsCollector();
        SpaceIoOperation spaceIoOperation = new SpaceIoOperation("space", Object.class, CHANGE, SQL, NONE);

        statisticsCollector.operationFinished(spaceIoOperation);
    }

    private void perform(XapIoStatisticsCollector statisticsCollector, SpaceIoOperation spaceIoOperation, long sleep) {
        statisticsCollector.operationStarted(spaceIoOperation);

        long start = System.nanoTime();
        long interval = TimeUnit.MILLISECONDS.toNanos(sleep);
        while (start + interval > System.nanoTime()) {
        }

        statisticsCollector.operationFinished(spaceIoOperation);
    }

    private static class TestStatisticalMeasure implements StatisticalMeasure {

        private List<Long> latencyValues;
        private int size;

        public TestStatisticalMeasure(int size) {
            this.size = size;
            this.latencyValues = new ArrayList<>(size);

        }

        @Override
        public void addValue(long metricValue) {
            latencyValues.add(metricValue);
        }

        @Override
        public long[] getResult() {
            return ArrayUtils.toPrimitive(latencyValues.toArray(new Long[size]));
        }

        @Override
        public void logStatistics() {
        }
    }
}

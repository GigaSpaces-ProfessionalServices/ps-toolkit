package com.gigaspaces.gigapro.inspector.statistics;

import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.gigaspaces.gigapro.inspector.statistics.IoOperation.CHANGE;
import static com.gigaspaces.gigapro.inspector.statistics.IoOperationModifier.NONE;
import static com.gigaspaces.gigapro.inspector.statistics.IoOperationType.SQL;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;

public class XapIoStatisticsCollectorTest {

    @Test
    public void testXapIoStatistics() throws Exception {
        XapIoStatisticsCollector statisticsCollector = new XapIoStatisticsCollector("space");
        SpaceIoOperation spaceIoOperation = new SpaceIoOperation(Object.class, CHANGE, SQL, NONE);
        TimedExponentialMovingAverage avg;
        long[] sleeps = {1L, 2L, 3L};
        float alpha = 0.5f;
        double actual;
        double lastActual;
        double expected;

        avg = getTimedExponentialMovingAverage(statisticsCollector, spaceIoOperation, sleeps[0]);
        actual = avg.getAverageValue();
        expected = sleeps[0];

        assertThat(avg.getInvocationCount(), is(1L));
        assertThat(actual, closeTo(expected, 1));

        avg = getTimedExponentialMovingAverage(statisticsCollector, spaceIoOperation, sleeps[1]);
        lastActual = actual;
        actual = avg.getAverageValue();
        expected = alpha * (sleeps[1] + lastActual);

        assertThat(avg.getInvocationCount(), is(2L));
        assertThat(actual, closeTo(expected, 1));

        avg = getTimedExponentialMovingAverage(statisticsCollector, spaceIoOperation, sleeps[2]);
        lastActual = actual;
        actual = avg.getAverageValue();
        expected = alpha * (sleeps[2] + lastActual);

        assertThat(avg.getInvocationCount(), is(3L));
        assertThat(actual, closeTo(expected, 1));
    }

    @Test
    public void testXapIoStatisticsMultithreaded() throws InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        System.setProperty("LOG_FREQUENCY", "12");
        XapIoStatisticsCollector statisticsCollector = new XapIoStatisticsCollector("space");
        SpaceIoOperation spaceIoOperation = new SpaceIoOperation(Object.class, CHANGE, SQL, NONE);
        long[] sleeps = {1L, 2L, 3L};

        CountDownLatch latch = new CountDownLatch(5);
        ExecutorService executorService = newFixedThreadPool(4);

        Callable<TimedExponentialMovingAverage> task = () -> {
            getTimedExponentialMovingAverage(statisticsCollector, spaceIoOperation, sleeps[0]);
            getTimedExponentialMovingAverage(statisticsCollector, spaceIoOperation, sleeps[1]);
            getTimedExponentialMovingAverage(statisticsCollector, spaceIoOperation, sleeps[2]);
            latch.countDown();
            return null;
        };

        executorService.submit(task);
        executorService.submit(task);
        executorService.submit(task);
        executorService.submit(task);
        latch.countDown();
        latch.await();


        Field allThreadsStatisticsField = XapIoStatisticsCollector.class.getDeclaredField("allThreadsStatistics");
        allThreadsStatisticsField.setAccessible(true);
        Map<Thread, HashMap<SpaceIoOperation, TimedExponentialMovingAverage>> allThreadsStatistics = (Map<Thread, HashMap<SpaceIoOperation, TimedExponentialMovingAverage>>) allThreadsStatisticsField.get(statisticsCollector);

        assertThat(allThreadsStatistics.keySet().size(), is(4));


        Method collectAllThreadsStatistics = XapIoStatisticsCollector.class.getDeclaredMethod("collectAllThreadsStatistics");
        collectAllThreadsStatistics.setAccessible(true);
        Map<SpaceIoOperation, TimedExponentialMovingAverage> stats = (Map<SpaceIoOperation, TimedExponentialMovingAverage>) collectAllThreadsStatistics.invoke(statisticsCollector);
        TimedExponentialMovingAverage average = stats.get(spaceIoOperation);

        assertThat(average.getInvocationCount(), is(12L));
        assertThat(average.getAverageValue(), closeTo(3, 1));
    }

    @Test(expected = IllegalStateException.class)
    public void operationFinishedBeforeStartedTest() {
        XapIoStatisticsCollector statisticsCollector = new XapIoStatisticsCollector("space");
        SpaceIoOperation spaceIoOperation = new SpaceIoOperation(Object.class, CHANGE, SQL, NONE);

        statisticsCollector.operationFinished(spaceIoOperation);
    }

    private TimedExponentialMovingAverage getTimedExponentialMovingAverage(XapIoStatisticsCollector statisticsCollector, SpaceIoOperation spaceIoOperation, long sleep) {
        statisticsCollector.operationStarted(spaceIoOperation);
        long start = System.nanoTime();
        long interval = TimeUnit.MILLISECONDS.toNanos(sleep);
        while(start + interval > System.nanoTime()) {}
        statisticsCollector.operationFinished(spaceIoOperation);
        return statisticsCollector.getLocalAverageStatistics(spaceIoOperation);
    }
}

package com.gigaspaces.gigapro.inspector.statistics;

import org.junit.Test;

import static com.gigaspaces.gigapro.inspector.statistics.IoOperation.CHANGE;
import static com.gigaspaces.gigapro.inspector.statistics.IoOperationModifier.NONE;
import static com.gigaspaces.gigapro.inspector.statistics.IoOperationType.SQL;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SpaceIoOperationTest {

    @Test
    public void createStatisticsEntryAllParamsTest() {
        IoOperation operation = CHANGE;
        IoOperationType operationType = SQL;
        IoOperationModifier operationModifier = NONE;
        Class<?> clazz = Object.class;

        SpaceIoOperation spaceIoOperation = new SpaceIoOperation(clazz, operation, operationType, operationModifier);

        assertThat(spaceIoOperation.getTrackedClass(), is(equalTo(clazz)));
        assertThat(spaceIoOperation.getOperation(), is(operation));
        assertThat(spaceIoOperation.getOperationModifier(), is(operationModifier));
        assertThat(spaceIoOperation.getOperationType(), is(operationType));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStatisticsEntryNoFirstParamTest() {
        new SpaceIoOperation(null, CHANGE, SQL, NONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStatisticsEntryNoSecondParamTest() {
        new SpaceIoOperation(Object.class, null, SQL, NONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStatisticsEntryNoThirdParamTest() {
        new SpaceIoOperation(Object.class, CHANGE, null, NONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStatisticsEntryNoFourthParamTest() {
        new SpaceIoOperation(Object.class, CHANGE, SQL, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStatisticsEntryNoParamsTest() {
        new SpaceIoOperation(null, null, null, null);
    }
}

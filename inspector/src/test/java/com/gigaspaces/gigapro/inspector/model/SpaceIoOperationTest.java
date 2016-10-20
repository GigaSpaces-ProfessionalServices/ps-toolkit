package com.gigaspaces.gigapro.inspector.model;

import org.junit.Test;

import static com.gigaspaces.gigapro.inspector.model.IoOperation.CHANGE;
import static com.gigaspaces.gigapro.inspector.model.IoOperationModifier.NONE;
import static com.gigaspaces.gigapro.inspector.model.IoOperationType.SQL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class SpaceIoOperationTest {

    @Test
    public void createStatisticsEntryAllParamsTest() {
        String spaceName = "space";
        IoOperation operation = CHANGE;
        IoOperationType operationType = SQL;
        IoOperationModifier operationModifier = NONE;
        Class<?> clazz = Object.class;

        SpaceIoOperation spaceIoOperation = new SpaceIoOperation(spaceName, clazz, operation, operationType, operationModifier);

        assertEquals(spaceIoOperation.getTrackedClass(), clazz);
        assertThat(spaceIoOperation.getOperation(), is(operation));
        assertThat(spaceIoOperation.getOperationModifier(), is(operationModifier));
        assertThat(spaceIoOperation.getOperationType(), is(operationType));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStatisticsEntryNoFirstParamTest() {
        new SpaceIoOperation(null, Object.class, CHANGE, SQL, NONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStatisticsEntryNoSecondParamTest() {
        new SpaceIoOperation("space", null, CHANGE, SQL, NONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStatisticsEntryNoThirdParamTest() {
        new SpaceIoOperation("space", Object.class, null, SQL, NONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStatisticsEntryNoFourthParamTest() {
        new SpaceIoOperation("space", Object.class, CHANGE, null, NONE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStatisticsEntryNoFifthParamTest() {
        new SpaceIoOperation("space", Object.class, CHANGE, SQL, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createStatisticsEntryNoParamsTest() {
        new SpaceIoOperation(null, null, null, null, null);
    }
}

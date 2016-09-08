package com.gigaspaces.gigapro.inspector;

import com.gigaspaces.gigapro.inspector.statistics.IoOperation;
import com.gigaspaces.gigapro.inspector.statistics.IoOperationModifier;
import com.gigaspaces.gigapro.inspector.statistics.IoOperationType;
import com.gigaspaces.gigapro.inspector.statistics.SpaceIoOperation;
import com.gigaspaces.internal.query.CustomSpaceQuery;
import com.gigaspaces.internal.query.predicate.composite.AllSpacePredicate;
import com.gigaspaces.query.IdQuery;
import com.gigaspaces.query.IdsQuery;
import com.j_spaces.core.client.SQLQuery;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openspaces.core.GigaSpace;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.gigaspaces.gigapro.inspector.statistics.IoOperation.*;
import static com.gigaspaces.gigapro.inspector.statistics.IoOperationModifier.*;
import static com.gigaspaces.gigapro.inspector.statistics.IoOperationType.SQL;
import static com.gigaspaces.gigapro.inspector.statistics.IoOperationType.TEMPLATE;
import static java.lang.Integer.valueOf;
import static org.apache.commons.lang.StringUtils.containsIgnoreCase;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class SpaceStatisticsRecordingProxyTest {

    @Test
    public void determineParamTypeSqlQueryTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method determineParamType = SpaceStatisticsRecordingProxy.class.getDeclaredMethod("determineParamType", Object.class);
        determineParamType.setAccessible(true);

        Class<?> sqlQueryClass = (Class<?>) determineParamType.invoke(new SpaceStatisticsRecordingProxy(), new SQLQuery<>(SpaceIoOperation.class, ""));
        assertThat(sqlQueryClass, is(equalTo(SpaceIoOperation.class)));
    }

    @Test
    public void determineParamTypeIdQueryTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method determineParamType = SpaceStatisticsRecordingProxy.class.getDeclaredMethod("determineParamType", Object.class);
        determineParamType.setAccessible(true);

        Class<?> idQueryClass = (Class<?>) determineParamType.invoke(new SpaceStatisticsRecordingProxy(), new IdQuery<>(SpaceIoOperation.class, new Object()));
        assertThat(idQueryClass, is(equalTo(SpaceIoOperation.class)));
    }

    @Test
    public void determineParamTypeIdsQueryTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method determineParamType = SpaceStatisticsRecordingProxy.class.getDeclaredMethod("determineParamType", Object.class);
        determineParamType.setAccessible(true);

        Class<?> idsQueryClass = (Class<?>) determineParamType.invoke(new SpaceStatisticsRecordingProxy(), new IdsQuery<>(SpaceIoOperation.class, new Object[]{}));
        assertThat(idsQueryClass, is(equalTo(SpaceIoOperation.class)));
    }

    @Test
    public void determineParamTypeCustomQueryTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method determineParamType = SpaceStatisticsRecordingProxy.class.getDeclaredMethod("determineParamType", Object.class);
        determineParamType.setAccessible(true);

        Class<?> customQueryClass = (Class<?>) determineParamType.invoke(new SpaceStatisticsRecordingProxy(), new CustomSpaceQuery<>(SpaceIoOperation.class, new AllSpacePredicate()));
        assertThat(customQueryClass, is(equalTo(SpaceIoOperation.class)));
    }

    @Test
    public void determineParamTypeClassInstanceTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method determineParamType = SpaceStatisticsRecordingProxy.class.getDeclaredMethod("determineParamType", Object.class);
        determineParamType.setAccessible(true);

        Class<?> classInstance = (Class<?>) determineParamType.invoke(new SpaceStatisticsRecordingProxy(), SpaceIoOperation.class);
        assertThat(classInstance, is(equalTo(SpaceIoOperation.class)));
    }

    @Test
    public void determineParamTypeArrayInstanceTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method determineParamType = SpaceStatisticsRecordingProxy.class.getDeclaredMethod("determineParamType", Object.class);
        determineParamType.setAccessible(true);

        Class<?> arrayInstance = (Class<?>) determineParamType.invoke(new SpaceStatisticsRecordingProxy(), (Object) new Integer[]{valueOf(1), valueOf(2)});
        assertThat(arrayInstance, is(equalTo(Integer.class)));
    }

    @Test
    public void determineParamTypeInstanceTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method determineParamType = SpaceStatisticsRecordingProxy.class.getDeclaredMethod("determineParamType", Object.class);
        determineParamType.setAccessible(true);

        Class<?> arrayInstance = (Class<?>) determineParamType.invoke(new SpaceStatisticsRecordingProxy(), "");
        assertThat(arrayInstance, is(equalTo(String.class)));
    }

    @Test
    public void determineOperationTypeSQLTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method determineOperationType = SpaceStatisticsRecordingProxy.class.getDeclaredMethod("determineOperationType", Object.class);
        determineOperationType.setAccessible(true);

        IoOperationType operationType = (IoOperationType) determineOperationType.invoke(new SpaceStatisticsRecordingProxy(), new SQLQuery<>(SpaceIoOperation.class, ""));
        assertThat(operationType, is(SQL));

    }

    @Test
    public void determineOperationTypeTemplateTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method determineOperationType = SpaceStatisticsRecordingProxy.class.getDeclaredMethod("determineOperationType", Object.class);
        determineOperationType.setAccessible(true);

        IoOperationType operationType = (IoOperationType) determineOperationType.invoke(new SpaceStatisticsRecordingProxy(), 1);
        assertThat(operationType, is(TEMPLATE));

    }

    @Test
    public void determineOperationModifierTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method determineOperationModifier = SpaceStatisticsRecordingProxy.class.getDeclaredMethod("determineOperationModifier", String.class);
        determineOperationModifier.setAccessible(true);

        IoOperationModifier operationModifier;
        for (Method method : GigaSpace.class.getMethods()) {
            String methodName = method.getName();
            operationModifier = (IoOperationModifier) determineOperationModifier.invoke(new SpaceStatisticsRecordingProxy(), methodName);
            if (containsIgnoreCase(methodName, "async"))
                assertThat(operationModifier, is(ASYNC));
            else if (containsIgnoreCase(methodName, "ifExists"))
                assertThat(operationModifier, is(IF_EXISTS));
            else if (containsIgnoreCase(methodName, "byId"))
                assertThat(operationModifier, is(BY_ID));
            else
                assertThat(operationModifier, is(NONE));
        }
    }

    @Test
    public void determineOperationTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method determineOperation = SpaceStatisticsRecordingProxy.class.getDeclaredMethod("determineOperation", String.class);
        determineOperation.setAccessible(true);

        IoOperation operation;
        for (Method method : GigaSpace.class.getMethods()) {
            String methodName = method.getName();
            try {
                operation = (IoOperation) determineOperation.invoke(new SpaceStatisticsRecordingProxy(), methodName);
            } catch (Exception e) {
                if (ExceptionUtils.getCause(e) instanceof IllegalArgumentException) {
                    assertThat(methodName, not(anyOf(
                            is("read"), is("readById"), is("asyncRead"), is("readIfExists"), is("readIfExistsById"),
                            is("readByIds"), is("readMultiple"), is("write"), is("writeMultiple"), is("take"),
                            is("takeById"), is("asyncTake"), is("takeIfExists"), is("takeIfExistsById"), is("takeByIds"),
                            is("takeMultiple"), is("change"), is("asyncChange"), is("clear"), is("count"))));
                    continue;
                }
                throw e;
            }
            if (methodName.equals("read") || methodName.equals("readById") || methodName.equals("asyncRead") || methodName.equals("readIfExists") || methodName.equals("readIfExistsById"))
                assertThat(operation, is(READ));
            else if (methodName.equals("readByIds") || methodName.equals("readMultiple"))
                assertThat(operation, is(READ_MULTIPLE));
            else if (methodName.equals("write"))
                assertThat(operation, is(WRITE));
            else if (methodName.equals("writeMultiple"))
                assertThat(operation, is(WRITE_MULTIPLE));
            else if (methodName.equals("take") || methodName.equals("takeById") || methodName.equals("asyncTake") || methodName.equals("takeIfExists") || methodName.equals("takeIfExistsById"))
                assertThat(operation, is(TAKE));
            else if (methodName.equals("takeByIds") || methodName.equals("takeMultiple"))
                assertThat(operation, is(TAKE_MULTIPLE));
            else if (methodName.equals("change") || methodName.equals("asyncChange"))
                assertThat(operation, is(CHANGE));
            else if (methodName.equals("clear"))
                assertThat(operation, is(CLEAR));
            else if (methodName.equals("count"))
                assertThat(operation, is(COUNT));
            else
                Assert.fail("It shouldn't has happened!");
        }
    }
}

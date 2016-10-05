package com.gigaspaces.gigapro.inspector;

import com.gigaspaces.gigapro.inspector.model.IoOperation;
import com.gigaspaces.gigapro.inspector.model.IoOperationModifier;
import com.gigaspaces.gigapro.inspector.model.IoOperationType;
import com.gigaspaces.gigapro.inspector.model.SpaceIoOperation;
import com.gigaspaces.gigapro.inspector.statistics.StatisticsCollector;
import com.gigaspaces.gigapro.inspector.statistics.XapIoStatisticsCollector;
import com.gigaspaces.internal.exceptions.IllegalArgumentNullException;
import com.gigaspaces.internal.query.AbstractSpaceQuery;
import com.gigaspaces.query.ISpaceQuery;
import com.gigaspaces.query.IdQuery;
import com.gigaspaces.query.IdsQuery;
import com.j_spaces.core.client.SQLQuery;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.openspaces.core.GigaSpace;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.gigaspaces.gigapro.inspector.model.IoOperation.*;
import static com.gigaspaces.gigapro.inspector.model.IoOperationModifier.*;
import static com.gigaspaces.gigapro.inspector.model.IoOperationType.SQL;
import static com.gigaspaces.gigapro.inspector.model.IoOperationType.TEMPLATE;
import static java.lang.String.format;
import static org.apache.commons.lang.StringUtils.containsIgnoreCase;

/**
 * This class assumes the presence of a pre-existing Spring ApplicationContext
 * that contains at least one org.openspaces.core.GigaSpace.
 */
@Configuration
@EnableAspectJAutoProxy
@Aspect
public class SpaceStatisticsRecordingProxy {

    private final ConcurrentMap<String, StatisticsCollector> statisticsCollectors = new ConcurrentHashMap<>();

    @Pointcut("(execution(public * org.openspaces.core.GigaSpace.read*(..)) " +
            " || execution(public * org.openspaces.core.GigaSpace.write*(..))" +
            " || execution(public * org.openspaces.core.GigaSpace.take*(..)) " +
            " || execution(public * org.openspaces.core.GigaSpace.change*(..))" +
            " || execution(public * org.openspaces.core.GigaSpace.clear*(..)) " +
            " || execution(public * org.openspaces.core.GigaSpace.count*(..)) " +
            " || execution(public * org.openspaces.core.GigaSpace.async*(..))) && args(param,..)")
    public void universalPointcut(Object param) {
    }

    @Around(value = "universalPointcut(param)", argNames = "joinPoint,param")
    public Object recordStatistics(ProceedingJoinPoint joinPoint, Object param) throws Throwable {
        if (param == null) {
            throw new IllegalArgumentNullException("param");
        }

        String gigaSpaceName = ((GigaSpace) joinPoint.getTarget()).getName();
        StatisticsCollector statisticsCollector = statisticsCollectors.computeIfAbsent(gigaSpaceName, (string) -> new XapIoStatisticsCollector());
      
        String methodName = joinPoint.getSignature().getName();
        SpaceIoOperation spaceIoOperation = createSpaceIoOperation(param, methodName, gigaSpaceName);

        statisticsCollector.operationStarted(spaceIoOperation);
        Object result = joinPoint.proceed();
        statisticsCollector.operationFinished(spaceIoOperation);
        return result;
    }
    
    private SpaceIoOperation createSpaceIoOperation(Object param, String methodName, String spaceName) throws ClassNotFoundException {
        IoOperation operation = determineOperation(methodName);
        IoOperationModifier operationModifier = determineOperationModifier(methodName);
        IoOperationType operationType = determineOperationType(param);
        Class<?> type = determineParamType(param);

        return new SpaceIoOperation(spaceName, type, operation, operationType, operationModifier);
    }

    private Class<?> determineParamType(Object param) throws ClassNotFoundException {
        if (param instanceof SQLQuery)
            return Class.forName(((SQLQuery) param).getTypeName());
        if (param instanceof IdQuery)
            return Class.forName(((IdQuery) param).getTypeName());
        if (param instanceof IdsQuery)
            return Class.forName(((IdsQuery) param).getTypeName());
        if (param instanceof AbstractSpaceQuery)
            return Class.forName(((AbstractSpaceQuery) param).getEntryTypeName());
        if (param.getClass().isArray())
            return param.getClass().getComponentType();
        if (param instanceof Class)
            return (Class<?>) param;
        return param.getClass();
    }

    private IoOperation determineOperation(String methodName) {
        if (methodName.equals("read") || methodName.equals("readById") || methodName.equals("asyncRead") || methodName.equals("readIfExists") || methodName.equals("readIfExistsById"))
            return READ;
        if (methodName.equals("readByIds") || methodName.equals("readMultiple"))
            return READ_MULTIPLE;
        if (methodName.equals("write"))
            return WRITE;
        if (methodName.equals("writeMultiple"))
            return WRITE_MULTIPLE;
        if (methodName.equals("take") || methodName.equals("takeById") || methodName.equals("asyncTake") || methodName.equals("takeIfExists") || methodName.equals("takeIfExistsById"))
            return TAKE;
        if (methodName.equals("takeByIds") || methodName.equals("takeMultiple"))
            return TAKE_MULTIPLE;
        if (methodName.equals("change") || methodName.equals("asyncChange"))
            return CHANGE;
        if (methodName.equals("clear"))
            return CLEAR;
        if (methodName.equals("count"))
            return COUNT;
        throw new IllegalArgumentException(format("Operation %s is not supported by XAP IO Statistics Collector.", methodName));
    }

    private IoOperationType determineOperationType(Object param) {
        if (param instanceof ISpaceQuery)
            return SQL;
        return TEMPLATE;
    }

    private IoOperationModifier determineOperationModifier(String methodName) {
        if (containsIgnoreCase(methodName, "ifExists"))
            return IF_EXISTS;
        if (containsIgnoreCase(methodName, "async"))
            return ASYNC;
        if (containsIgnoreCase(methodName, "byId"))
            return BY_ID;
        return NONE;
    }
}

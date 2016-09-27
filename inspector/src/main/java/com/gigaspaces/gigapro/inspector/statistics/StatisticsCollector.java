package com.gigaspaces.gigapro.inspector.statistics;

import com.gigaspaces.gigapro.inspector.model.SpaceIoOperation;

/**
 *
 */
public interface StatisticsCollector {

    /**
     * Starts recording statistics for operation
     *
     * @param spaceIoOperation
     *            SpaceIoOperation
     * @throws IllegalArgumentException
     *             if any of params is null
     */
    void operationStarted(SpaceIoOperation spaceIoOperation);

    /**
     * Finishes recording statistics for operation
     *
     * @param spaceIoOperation
     *            SpaceIoOperation
     * @throws IllegalArgumentException
     *             if any of params is null
     */
    void operationFinished(SpaceIoOperation spaceIoOperation);

    void logStatistics(long invocationCount);
}

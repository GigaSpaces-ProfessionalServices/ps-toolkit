package com.gigaspaces.gigapro.inspector.statistics;


import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.join;

public class SpaceIoOperation {

    private final Class<?> trackedClass;
    private final IoOperation operation;
    private final IoOperationType operationType;
    private final IoOperationModifier operationModifier;

    /**
     * @param trackedClass      trackedClass
     * @param operation         IoOperation
     * @param operationType     IoOperationType
     * @param operationModifier IoOperationModifier
     * @throws IllegalArgumentException if any of params is null
     */
    public SpaceIoOperation(Class<?> trackedClass, IoOperation operation, IoOperationType operationType, IoOperationModifier operationModifier) {
        validateParamsNotNull(trackedClass, operation, operationType, operationModifier);
        this.trackedClass = trackedClass;
        this.operation = operation;
        this.operationType = operationType;
        this.operationModifier = operationModifier;
    }

    /**
     * Validates all params if they're null and throws IllegalArgumentException if any of them is null
     *
     * @param trackedClass      trackedClass
     * @param operation         IoOperation
     * @param operationType     IoOperationType
     * @param operationModifier IoOperationModifier
     * @throws IllegalArgumentException if any of params is null
     */
    static void validateParamsNotNull(Class<?> trackedClass, IoOperation operation, IoOperationType operationType, IoOperationModifier operationModifier) {
        List<String> errorMessage = new ArrayList<>();
        if (trackedClass == null) errorMessage.add("Space class must be provided.");
        if (operation == null) errorMessage.add("Operation must be provided.");
        if (operationType == null) errorMessage.add("Operation type must be provided.");
        if (operationModifier == null) errorMessage.add("Operation modifier must be provided.");
        if (isNotEmpty(errorMessage)) throw new IllegalArgumentException(join(errorMessage, " "));
    }

    IoOperation getOperation() {
        return operation;
    }

    IoOperationType getOperationType() {
        return operationType;
    }

    IoOperationModifier getOperationModifier() {
        return operationModifier;
    }

    Class<?> getTrackedClass() {
        return trackedClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpaceIoOperation spaceIoOperation = (SpaceIoOperation) o;

        if (!trackedClass.equals(spaceIoOperation.trackedClass)) return false;
        if (operation != spaceIoOperation.operation) return false;
        if (operationType != spaceIoOperation.operationType) return false;
        return operationModifier == spaceIoOperation.operationModifier;
    }

    @Override
    public int hashCode() {
        int result = trackedClass.hashCode();
        result = 31 * result + operation.hashCode();
        result = 31 * result + operationType.hashCode();
        result = 31 * result + operationModifier.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SpaceIoOperation{" +
                "trackedClass=" + trackedClass +
                ", operation=" + operation +
                ", operationType=" + operationType +
                ", operationModifier=" + operationModifier +
                '}';
    }
}
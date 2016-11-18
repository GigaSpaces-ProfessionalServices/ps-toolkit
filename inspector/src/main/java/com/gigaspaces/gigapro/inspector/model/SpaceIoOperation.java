package com.gigaspaces.gigapro.inspector.model;


import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.join;

public class SpaceIoOperation {

    private final String spaceName;
    private final Class<?> trackedClass;
    private final IoOperation operation;
    private final IoOperationType operationType;
    private final IoOperationModifier operationModifier;

    /**
     * @param spaceName         spaceName
     * @param trackedClass      trackedClass
     * @param operation         IoOperation
     * @param operationType     IoOperationType
     * @param operationModifier IoOperationModifier
     * @throws IllegalArgumentException if any of params is null
     */
    public SpaceIoOperation(String spaceName, Class<?> trackedClass, IoOperation operation, IoOperationType operationType, IoOperationModifier operationModifier) {
        validateParamsNotNull(spaceName, trackedClass, operation, operationType, operationModifier);
        this.spaceName = spaceName;
        this.trackedClass = trackedClass;
        this.operation = operation;
        this.operationType = operationType;
        this.operationModifier = operationModifier;
    }

    /**
     * Validates all params if they're null and throws IllegalArgumentException if any of them is null
     *
     * @param spaceName         spaceName
     * @param trackedClass      trackedClass
     * @param operation         IoOperation
     * @param operationType     IoOperationType
     * @param operationModifier IoOperationModifier
     * @throws IllegalArgumentException if any of params is null
     */
    public static void validateParamsNotNull(String spaceName, Class<?> trackedClass, IoOperation operation, IoOperationType operationType, IoOperationModifier operationModifier) {
        List<String> errorMessage = new ArrayList<>();
        if (spaceName == null) {
            errorMessage.add("Space name must be provided.");
        }
        if (trackedClass == null) {
            errorMessage.add("Space class must be provided.");
        }
        if (operation == null) {
            errorMessage.add("Operation must be provided.");
        }
        if (operationType == null) {
            errorMessage.add("Operation type must be provided.");
        }
        if (operationModifier == null) {
            errorMessage.add("Operation modifier must be provided.");
        }
        if (!errorMessage.isEmpty()) {
            throw new IllegalArgumentException(join(errorMessage.iterator(), " "));
        }
    }

    public IoOperation getOperation() {
        return operation;
    }

    public IoOperationType getOperationType() {
        return operationType;
    }

    public IoOperationModifier getOperationModifier() {
        return operationModifier;
    }

    public Class<?> getTrackedClass() {
        return trackedClass;
    }

    public String getSpaceName() {
        return spaceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpaceIoOperation spaceIoOperation = (SpaceIoOperation) o;

        if (!spaceName.equals(spaceIoOperation.spaceName)) return false;
        if (!trackedClass.equals(spaceIoOperation.trackedClass)) return false;
        if (operation != spaceIoOperation.operation) return false;
        if (operationType != spaceIoOperation.operationType) return false;
        return operationModifier == spaceIoOperation.operationModifier;
    }

    @Override
    public int hashCode() {
        int result = trackedClass.hashCode();
        result = 31 * result + spaceName.hashCode();
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
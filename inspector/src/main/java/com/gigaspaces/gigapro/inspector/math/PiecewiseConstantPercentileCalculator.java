package com.gigaspaces.gigapro.inspector.math;

import java.util.*;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class PiecewiseConstantPercentileCalculator implements PercentileCalculator {

    @Override
    public <T extends Number & Comparable<T>> Double calculate(List<? extends T> dataSet, float percentileRatio) {
        return calculate(dataSet, percentileRatio, Comparator.naturalOrder());
    }

    @Override
    public <T extends Number> Double calculate(List<? extends T> dataCollection, float percentileRatio, Comparator<T> comparator) {
        if (dataCollection == null || dataCollection.size() < 2) {
            return null;
        }
        Objects.requireNonNull(comparator, "'comparator' must not be null");
        if (percentileRatio <= 0 || percentileRatio >= 1) {
            throw new IllegalArgumentException("'percentileRatio' must be in exclusive [0,1] range");
        }
        Collections.sort(dataCollection, comparator);

        return calculateSorted(dataCollection, percentileRatio);
    }

    private <T extends Number> Double calculateSorted(List<? extends T> sortedData, float percentileRatio) {
        if (sortedData == null || sortedData.size() < 2) {
            return null;
        }
        final int size = sortedData.size();
        int zoneCount = size * 2;
        double zoneShare = 1 / (double) zoneCount;
        int lastZone = zoneCount - 1;

        // Parentheses are paramount below
        int zoneIndex = (int) (percentileRatio * zoneCount);
        // Handling the case for 100% percentile
        if (zoneIndex == zoneCount) {
            zoneIndex--;
        }

        double zoneRatio = zoneIndex * zoneShare;
        double multiplier = zoneCount * (percentileRatio - zoneRatio);

        if (zoneIndex == 0) {
            double firstItem = sortedData.get(0).doubleValue();
            double zoneDelta = (firstItem - sortedData.get(1).doubleValue()) / 2;
            double origin = firstItem - zoneDelta;
            return origin + zoneDelta * multiplier;
        } else if (zoneIndex == lastZone) {
            int lastIndex = size - 1;
            double lastItem = sortedData.get(lastIndex).doubleValue();
            double zoneDelta = (lastItem - sortedData.get(lastIndex - 1).doubleValue()) / 2;
            return lastItem + zoneDelta * multiplier;
        } else if (zoneIndex % 2 == 1) {
            int leftIndex = zoneIndex / 2;
            double leftItem = sortedData.get(leftIndex).doubleValue();
            double zoneDelta = (sortedData.get(leftIndex + 1).doubleValue() - leftItem) / 2;
            return leftItem + zoneDelta * multiplier;
        } else {
            int rightIndex = zoneIndex / 2;
            double rightItem = sortedData.get(rightIndex).doubleValue();
            double zoneDelta = (rightItem - sortedData.get(rightIndex - 1).doubleValue()) / 2;
            double origin = rightItem - zoneDelta;
            return origin + zoneDelta * multiplier;
        }
    }
}

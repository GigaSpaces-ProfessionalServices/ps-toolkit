package com.gigaspaces.gigapro.inspector.math;

import java.util.Comparator;
import java.util.List;

/**
 * @author Svitlana_Pogrebna
 *
 */
public interface PercentileCalculator {

    <T extends Number & Comparable<T>> Double calculate(List<? extends T> dataSet, float percentileRatio);

    <T extends Number> Double calculate(List<? extends T> dataCollection, float percentileRatio, Comparator<T> comparator);
}

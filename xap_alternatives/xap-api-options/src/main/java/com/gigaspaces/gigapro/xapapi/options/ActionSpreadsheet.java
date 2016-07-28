package com.gigaspaces.gigapro.xapapi.options;

import java.util.*;
import java.text.DecimalFormat;
import javax.annotation.Nullable;

public class ActionSpreadsheet {
    private String _unitName;
    private Map<String, ArrayList<Double>> _actionMap = new HashMap<>();

    public ActionSpreadsheet(String unitName) {
        _unitName = unitName;
    }

    public void setUnitName(String unitName) {
        _unitName = unitName;
    }

    public void fileMeasurement(String actionTag, Double measurement) {
        ArrayList<Double> currentList = _actionMap.get(actionTag);
        if (currentList == null) {
            currentList = new ArrayList<Double>();
            _actionMap.put(actionTag, currentList);
        }
        currentList.add(measurement);

        System.out.println(actionTag + ": " +
            measurement.toString() + " " + _unitName + "(s)");
    }

    public void printAverage(String actionTag, @Nullable String decimalFormat) {
        ArrayList<Double> currentList = _actionMap.get(actionTag);
        if (currentList == null) {
            System.out.println("Wrong action tag encountered: " + actionTag);
            return;
        }

        Double sum = 0.0, squareSum = 0.0;
        int listSize = currentList.size();
        if (listSize == 0) return;

        for (int i = 0; i < listSize; i++)
            sum += currentList.get(i);
        Double average = sum / listSize;

        for (int i = 0; i < listSize; i++) {
            Double difference = currentList.get(i) - average;
            squareSum += difference * difference;
        }

        if (listSize == 1) {
            String averageTag = format(average, decimalFormat);

            System.out.println(actionTag + ": " + averageTag + " " + _unitName + "(s)");
        }
        else // listSize > 1
        {
            // Getting unbiased sample variance
            Double variance = squareSum / (listSize - 1);
            // Corrected sample standard deviation
            Double deviation = Math.sqrt(variance);
            Double error = deviation / Math.sqrt(listSize);

            String averageTag = format(average, decimalFormat);
            String deviationTag = format(deviation, decimalFormat);
            String errorTag = format(error, decimalFormat);

            System.out.println(actionTag + ": " + averageTag + " " +
                _unitName + "(s), standard deviation " + deviationTag +
                ", standard error " + errorTag);
        }
    }

    private String format(Double value, @Nullable String decimalFormat) {
        return (decimalFormat == null) ? value.toString() :
                new DecimalFormat(decimalFormat).format(value);
    }
}

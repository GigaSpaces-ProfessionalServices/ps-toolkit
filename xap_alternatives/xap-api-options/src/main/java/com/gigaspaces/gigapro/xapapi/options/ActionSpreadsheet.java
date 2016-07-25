package com.gigaspaces.gigapro.xapapi.options;

import java.text.DecimalFormat;
import java.util.*;

public class ActionSpreadsheet {
    private String _unitName;
    private Map<String, ArrayList<Double>> _actionMap = new HashMap();

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

    public void printAverage(String actionTag, String decimalFormat) {
        ArrayList<Double> currentList = _actionMap.get(actionTag);
        if (currentList == null) {
            System.out.println("Wrong action tag encountered: " + actionTag);
            return;
        }

        Double sum = 0.0;
        int listSize = currentList.size();
        for (int i = 0; i < listSize; i++)
            sum += currentList.get(i);

        Double average = sum / listSize;
        String result = (decimalFormat == null) ? average.toString() :
            new DecimalFormat(decimalFormat).format(average);
        System.out.println(actionTag + ": " + result + " " + _unitName + "(s)");
    }
}

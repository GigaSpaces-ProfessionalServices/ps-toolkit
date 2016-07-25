package com.gigaspaces.gigapro.xapapi.options;

public class NanoTimeHelper {
    private long _startTime;
    private ActionSpreadsheet _spreadsheet;

    public NanoTimeHelper(ActionSpreadsheet spreadsheet) {
        if (spreadsheet == null) {
            _spreadsheet = new ActionSpreadsheet("second");
        }
        else {
            _spreadsheet = spreadsheet;
            _spreadsheet.setUnitName("second");
        }
        _startTime = System.nanoTime();
    }

    public void reset() {
        _startTime = System.nanoTime();
    }

    public void printElapsedTime(String messagePrefix) {
        long finishTime = System.nanoTime();
        long nanoSeconds = finishTime - _startTime;
        Double seconds = nanoSeconds / 1000000000.0;
        _spreadsheet.fileMeasurement(messagePrefix, seconds);
        _startTime = System.nanoTime();
    }
}

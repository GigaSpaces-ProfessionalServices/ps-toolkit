package com.gigaspaces.gigapro.xapapi.options;

public class NanoTimeHelper {
    private long _startTime;

    public NanoTimeHelper() {
        _startTime = System.nanoTime();
    }

    public void printElapsedTime(String messagePrefix) {
        long finishTime = System.nanoTime();
        long nanoSeconds = finishTime - _startTime;
        Double milliSeconds = nanoSeconds / 1000000.0;
        System.out.println(messagePrefix + ": " +
            milliSeconds.toString() + " milliseconds");
    }
}

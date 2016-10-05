package com.gigaspaces.gigapro.inspector.measure;


/**
 * @author Svitlana_Pogrebna
 *
 */
public interface StatisticalMeasure {
    
    double ONE_MILLISECOND = 1_000_000; //in nanos
    String FORMAT = "%-7s = %.3f ms";
    
    void addValue(long metricValue);
    
    Object getResult();
    
    void logStatistics();
}

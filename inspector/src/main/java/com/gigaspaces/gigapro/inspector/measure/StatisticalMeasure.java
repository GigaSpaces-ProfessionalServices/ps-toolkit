package com.gigaspaces.gigapro.inspector.measure;


/**
 * @author Svitlana_Pogrebna
 *
 */
public interface StatisticalMeasure {
    
    String DECIMAL_FORMAT = "%-7s = %.1f ms";
    String FORMAT = "%-7s = %d ms";
    
    void addValue(long metricValue);
    
    Object getResult();
    
    void logStatistics();
}

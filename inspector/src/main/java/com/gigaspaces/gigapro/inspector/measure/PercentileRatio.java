package com.gigaspaces.gigapro.inspector.measure;

/**
 * @author Svitlana_Pogrebna
 *
 */
public enum PercentileRatio {

    MEDIAN(0.5f),
    P90th(0.9f),
    P95th(0.95f),
    P99th(0.99f),
    P999th(0.999f) {
        @Override
        public String toString() {
            return "P99.9th";
        }
    };
    
    private final float value;
    
    private PercentileRatio(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return name();
    }
}

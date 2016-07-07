package com.gigaspaces.gigapro.convert;


/**
 * @author Svitlana_Pogrebna
 *
 */
public interface Converter {

    <T> String convert(T data);
    
    <T> String convert(String keyPrefix, T data);
}

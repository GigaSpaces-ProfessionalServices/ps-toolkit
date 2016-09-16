package com.gigaspaces.gigapro.convert.property;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class PropertiesFormatConverter {

    private static final char PROPERTIES_SEPARATOR = '=';
    private static final char MULTI_VALUES_SEPARATOR = ',';
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final char KEY_SEPARATOR = '_';
    private static final char NEWLINE_SEPARATOR = '\n';
    
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesFormatConverter.class);
    
    public <T> String convert(T data) {
        return convert("", data);
    }
    
    public <T> String convert(String keyPrefix, T data) {
        if (data == null) {
            return "";
        }
        Class<? extends Object> clazz = data.getClass();
        Field[] fields = clazz.getDeclaredFields();
        StringBuilder output = new StringBuilder();
        for (Field field : fields) {
            field.setAccessible(true);

            if (field.isSynthetic() || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            convertItem(keyPrefix, field, data, output);
        }
        return output.toString();
    }

    protected void convertKey(String keyPrefix, Field field, StringBuilder output) {
        PropertyKey propertyKey = field.getDeclaredAnnotation(PropertyKey.class);
        String key = propertyKey != null ? propertyKey.value() : field.getName();

        if (keyPrefix != null && !keyPrefix.isEmpty()) {
            key = keyPrefix + KEY_SEPARATOR + key;
        }
        output.append(key);
    }
    
    protected void convertValue(Class<? extends Object> fieldType, Object value, StringBuilder output) {
        if (isMap(fieldType)) {
            convertMapValue((Map<?, ?>) value, output);
        } else if (isIterable(fieldType)) {
            convertIterableValue((Iterable<?>) value, output);
        } else if (fieldType.isArray()) {
            convertIterableValue(Arrays.asList((Object[]) value), output);
        } else {
            output.append(value == null ? "" : value);
        }
    }
    
    protected void convertItem(String keyPrefix, Field field, Object data, StringBuilder output) {
        try {
            convertKey(keyPrefix, field, output);
             
            output.append(PROPERTIES_SEPARATOR);

            Class<? extends Object> fieldType = field.getType();
            Object value = field.get(data);
            convertValue(fieldType, value, output);
            
            output.append(NEWLINE_SEPARATOR);
            
         } catch (IllegalAccessException e) {
             LOG.error("Error while accessing property {}", field.getName(), e);
         }
    }
    
    protected void convertMapValue(Map<?, ?> map, StringBuilder output) {
        boolean first = true;
        for (Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                output.append(MULTI_VALUES_SEPARATOR);
            }
            Object value = entry.getValue();
            output.append(entry.getKey()).append(KEY_VALUE_SEPARATOR).append(value == null ? "" : value);
            first = false;
        }
    }

    protected void convertIterableValue(Iterable<?> data, StringBuilder output) {
        boolean first = true;
        for (Object entry : data) {
            if (!first) {
                output.append(MULTI_VALUES_SEPARATOR);
            }
            output.append(entry == null ? "" : entry);
            first = false;
        }
    }

    private static boolean isMap(Class<? extends Object> type) {
        return Map.class.isAssignableFrom(type);
    }

    private static boolean isIterable(Class<? extends Object> type) {
        return Iterable.class.isAssignableFrom(type);
    }
}

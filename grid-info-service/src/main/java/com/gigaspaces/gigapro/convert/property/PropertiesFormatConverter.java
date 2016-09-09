package com.gigaspaces.gigapro.convert.property;

import com.gigaspaces.gigapro.convert.Converter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class PropertiesFormatConverter implements Converter {

    private static final char PROPERTIES_SEPARATOR = '=';
    private static final char MULTI_VALUES_SEPARATOR = ',';
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final char KEY_SEPARATOR = '_';

    @Override
    public <T> String convert(T data) {
        return convert("", data);
    }

    @Override
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

            try {
                PropertyKey propertyKey = field.getDeclaredAnnotation(PropertyKey.class);
                String key = propertyKey != null ? propertyKey.value() : field.getName();
                
                if (keyPrefix != null && !keyPrefix.isEmpty()) {
                    key = keyPrefix + KEY_SEPARATOR + key;
                }
                
                output.append(key).append(PROPERTIES_SEPARATOR);
              
                Class<? extends Object> fieldType = field.getType();
                Object value = field.get(data);
                if (isMap(fieldType)) {
                    convertMapValue(key, (Map<?, ?>) value, output);
                } else if (isIterable(fieldType)) {
                    convertIterableValue(key, (Iterable<?>) value, output);
                } else if (fieldType.isArray()) {
                    convertIterableValue(key, Arrays.asList((Object[]) value), output);
                } else {
                    output.append(value == null ? "" : value).append("\n");
                }
            } catch (IllegalAccessException e) {
                System.err.printf("Error while accessing property %s. %s", field.getName(), e.getMessage());
            }
        }
        return output.toString();
    }

    private void convertMapValue(String key, Map<?, ?> map, StringBuilder output) {
        boolean first = true;
        for (Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                output.append(MULTI_VALUES_SEPARATOR);
            }
            Object value = entry.getValue();
            output.append(entry.getKey()).append(KEY_VALUE_SEPARATOR).append(value == null ? "" : value);
            first = false;
        }
        output.append("\n");
    }

    private void convertIterableValue(String key, Iterable<?> data, StringBuilder output) {
        boolean first = true;
        for (Object entry : data) {
            if (!first) {
                output.append(MULTI_VALUES_SEPARATOR);
            }
            output.append(entry == null ? "" : entry);
            first = false;
        }
        output.append("\n");
    }

    private static boolean isMap(Class<? extends Object> type) {
        return Map.class.isAssignableFrom(type);
    }

    private static boolean isIterable(Class<? extends Object> type) {
        return Iterable.class.isAssignableFrom(type);
    }
}

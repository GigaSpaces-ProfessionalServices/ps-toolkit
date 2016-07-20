package com.gigaspaces.gigapro.xapapi.options;

import com.gigaspaces.gigapro.xapapi.entities.ToolkitJavaBean;

import java.util.*;

public class DataObjectFactory {
    enum ToolkitObjectType {
        JAVA_BEAN,
        SPACE_CLASS,
        SPACE_DOCUMENT
    }

    public static Random ToolkitRandom;

    static {
        ToolkitRandom = new Random();
        ToolkitRandom.setSeed(System.currentTimeMillis());
    }

    public Object GenerateInstance(ToolkitObjectType objectType) {
        switch (objectType) {
        case JAVA_BEAN:
            return new ToolkitJavaBean();
        case SPACE_CLASS:
            return null;
        case SPACE_DOCUMENT:
            return null;
        default:
            throw new IllegalArgumentException();
        }
    }

    public Object[] GenerateArray(ToolkitObjectType objectType) {
        switch (objectType) {
            case JAVA_BEAN:
                return null;
            case SPACE_CLASS:
                return null;
            case SPACE_DOCUMENT:
                return null;
            default:
                throw new IllegalArgumentException();
        }
    }
}

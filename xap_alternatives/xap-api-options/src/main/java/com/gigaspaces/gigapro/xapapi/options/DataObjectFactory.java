package com.gigaspaces.gigapro.xapapi.options;

import java.util.*;

import com.gigaspaces.gigapro.xapapi.entities.*;

public class DataObjectFactory {
    public enum ToolkitObjectType {
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
            return new ToolkitSpaceClass();
        case SPACE_DOCUMENT:
            return null;
        default:
            throw new IllegalArgumentException();
        }
    }

    public Object[] GenerateArray(ToolkitObjectType objectType, int arraySize) {
        switch (objectType) {
            case JAVA_BEAN: {
                ToolkitJavaBean[] javaBeanArray = new ToolkitJavaBean[arraySize];
                for (int i = 0; i < arraySize; i++)
                    javaBeanArray[i] = new ToolkitJavaBean();
                return javaBeanArray;
            }
            case SPACE_CLASS: {
                ToolkitSpaceClass[] spaceClassArray = new ToolkitSpaceClass[arraySize];
                for (int i = 0; i < arraySize; i++)
                    spaceClassArray[i] = new ToolkitSpaceClass();
                return spaceClassArray;
            }
            case SPACE_DOCUMENT:
                return null; // TODO:
            default:
                throw new IllegalArgumentException();
        }
    }
}

package com.gigaspaces.gigapro.xapapi.options;

import java.util.*;

import com.gigaspaces.metadata.*;
import com.gigaspaces.metadata.index.SpaceIndexType;

import com.gigaspaces.gigapro.xapapi.entities.*;

public class DataObjectFactory {
    public enum ToolkitObjectType {
        JAVA_BEAN,
        SPACE_CLASS,
        SPACE_DOCUMENT
    }

    public static Random ToolkitRandom;
    public static SpaceTypeDescriptor TypeDescriptor;

    static {
        ToolkitRandom = new Random();
        ToolkitRandom.setSeed(System.currentTimeMillis());

        // Below descriptor is used for space documents
        TypeDescriptor = new SpaceTypeDescriptorBuilder("ToolkitSpaceDocument")
            .idProperty("ObjectId").routingProperty("ObjectId")
            .addPropertyIndex("ObjectType", SpaceIndexType.EXTENDED, false).create();
    }

    public Object GenerateInstance(ToolkitObjectType objectType) {
        switch (objectType) {
        case JAVA_BEAN:
            return new ToolkitJavaBean();
        case SPACE_CLASS:
            return new ToolkitSpaceClass();
        case SPACE_DOCUMENT:
            return new ToolkitSpaceDocument();
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
            case SPACE_DOCUMENT: {
                ToolkitSpaceDocument[] spaceDocumentArray = new ToolkitSpaceDocument[arraySize];
                for (int i = 0; i < arraySize; i++)
                    spaceDocumentArray[i] = new ToolkitSpaceDocument();
                return spaceDocumentArray;
            }
            default:
                throw new IllegalArgumentException();
        }
    }
}

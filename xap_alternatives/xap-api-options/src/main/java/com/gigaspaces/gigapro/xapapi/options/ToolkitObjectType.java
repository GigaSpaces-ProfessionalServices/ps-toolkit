package com.gigaspaces.gigapro.xapapi.options;

public enum ToolkitObjectType {
    JAVA_BEAN,
    SPACE_CLASS,
    SPACE_DOCUMENT;

    public static final int Permutations = 6;

    // When writing and reading objects from space the time measurement
    // statistics is biased if operations are done in predefined order
    // The method below helps to apply different order for object types

    public static ToolkitObjectType[] GetPermutation(int seedNumber) {
        switch (seedNumber % Permutations) {
            case 0: return NewArray(JAVA_BEAN, SPACE_CLASS, SPACE_DOCUMENT);
            case 1: return NewArray(JAVA_BEAN, SPACE_DOCUMENT, SPACE_CLASS);
            case 2: return NewArray(SPACE_CLASS, JAVA_BEAN, SPACE_DOCUMENT);
            case 3: return NewArray(SPACE_DOCUMENT, JAVA_BEAN, SPACE_CLASS);
            case 4: return NewArray(SPACE_CLASS, SPACE_DOCUMENT, JAVA_BEAN);
            case 5: return NewArray(SPACE_DOCUMENT, SPACE_CLASS, JAVA_BEAN);
            default:
                throw new IllegalArgumentException("Invalid seed number encountered");
        }
    }

    private static ToolkitObjectType[] NewArray(ToolkitObjectType type1,
        ToolkitObjectType type2, ToolkitObjectType type3) {

        return new ToolkitObjectType[] { type1, type2, type3 };
    }
}

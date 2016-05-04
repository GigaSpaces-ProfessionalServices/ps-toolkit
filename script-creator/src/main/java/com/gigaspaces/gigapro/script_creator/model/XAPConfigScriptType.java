package com.gigaspaces.gigapro.script_creator.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum XAPConfigScriptType {
    BAT(0, "Bat"),
    SHELL(1, "Shell");

    private int typeCode;

    private String name;

    XAPConfigScriptType(int typeCode, String name) {
        this.typeCode = typeCode;
        this.name = name;
    }

    @JsonValue
    public int getTypeCode() { return typeCode; }

    public String getName() { return name; }

    @Override
    public String toString() {
        return getName();
    }
}

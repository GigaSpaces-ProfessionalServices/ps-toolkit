package com.gigaspaces.gigapro.xapapi.entities;

import java.io.*;
import java.util.Random;

import com.gigaspaces.gigapro.xapapi.options.DataObjectFactory;

public class ToolkitBaseEntity implements Serializable {
    protected String _objectId;
    protected int _objectType;
    protected double _objectData;
    protected boolean _objectFlag;

    static String GetRandomHexString(int length) {
        Random random = DataObjectFactory.ToolkitRandom;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int symbol = random.nextInt(16);
            if (symbol >= 10)
                symbol = 'a' + (symbol - 10);
            else
                symbol = '0' + symbol;
            stringBuilder.append((char) symbol);
        }
        return stringBuilder.toString();
    }

    public ToolkitBaseEntity() {
        Random random = DataObjectFactory.ToolkitRandom;
        _objectId = GetRandomHexString(16);
        _objectType = random.nextInt(256);
        _objectData = random.nextDouble();
        _objectFlag = random.nextBoolean();
    }

    public double getObjectData() {
        return _objectData;
    }

    public void setObjectData(double objectData) {
        _objectData = objectData;
    }

    public boolean getObjectFlag() {
        return _objectFlag;
    }

    public void setObjectFlag(boolean objectFlag) {
        _objectFlag = objectFlag;
    }
}

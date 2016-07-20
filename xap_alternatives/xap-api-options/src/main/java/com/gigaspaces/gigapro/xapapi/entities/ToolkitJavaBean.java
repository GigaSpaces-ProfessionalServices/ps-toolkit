package com.gigaspaces.gigapro.xapapi.entities;

import com.gigaspaces.gigapro.xapapi.options.DataObjectFactory;

import java.io.*;
import java.util.*;

public class ToolkitJavaBean implements Serializable {
    private String _objectId;
    private int _objectType;
    private double _objectData;
    private boolean _objectFlag;

    public ToolkitJavaBean() {
        Random random = DataObjectFactory.ToolkitRandom;
        _objectType = random.nextInt() % 256;
        _objectData = random.nextDouble();
        _objectFlag = random.nextBoolean();

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int symbol = random.nextInt(16) + '0';
            if (symbol > '9')
                symbol = 'a' + (symbol - '9');
            stringBuilder.append((char) symbol);
        }
        _objectId = stringBuilder.toString();
    }

    public String getObjectId() {
        return _objectId;
    }

    public void setObjectId(String objectId) {
        _objectId = objectId;
    }


    public int getObjectType() {
        return _objectType;
    }

    public void setObjectType(int objectType) {
        _objectType = objectType;
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

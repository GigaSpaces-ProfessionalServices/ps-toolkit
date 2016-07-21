package com.gigaspaces.gigapro.xapapi.entities;


import java.io.*;
import java.util.*;

import com.gigaspaces.annotation.pojo.*;
import com.gigaspaces.metadata.index.SpaceIndexType;
import com.gigaspaces.gigapro.xapapi.options.*;

public class ToolkitJavaBean implements Serializable {
    private String _objectId;
    private int _objectType;
    private double _objectData;
    private boolean _objectFlag;

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

    public ToolkitJavaBean() {
        Random random = DataObjectFactory.ToolkitRandom;
        _objectId = GetRandomHexString(16);
        _objectType = random.nextInt(256);
        _objectData = random.nextDouble();
        _objectFlag = random.nextBoolean();
    }

    @SpaceId(autoGenerate = false) @SpaceRouting
    public String getObjectId() {
        return _objectId;
    }

    public void setObjectId(String objectId) {
        _objectId = objectId;
    }

	@SpaceIndex(type = SpaceIndexType.EXTENDED, unique = false)
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

    // Other popular annotations: SpaceProperty, SpaceExclude
}

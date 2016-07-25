package com.gigaspaces.gigapro.xapapi.entities;

import java.io.*;
import java.util.Random;

import com.gigaspaces.gigapro.xapapi.options.*;

public class ToolkitBaseEntity implements Serializable {

    protected String _objectId;
    protected Integer _objectType;
    protected Double _objectData;
    protected Boolean _objectFlag;

    public ToolkitBaseEntity() {}

    public ToolkitBaseEntity RandomInitialize() {
        Random random = DataObjectFactory.ToolkitRandom;
        _objectId = AbstractUtilities.GetRandomHexString(16);
        _objectType = random.nextInt(256);
        _objectData = random.nextDouble();
        _objectFlag = random.nextBoolean();
        return this;
    }

    public Double getObjectData() {
        return _objectData;
    }

    public void setObjectData(Double objectData) {
        _objectData = objectData;
    }

    public Boolean getObjectFlag() {
        return _objectFlag;
    }

    public void setObjectFlag(Boolean objectFlag) {
        _objectFlag = objectFlag;
    }
}

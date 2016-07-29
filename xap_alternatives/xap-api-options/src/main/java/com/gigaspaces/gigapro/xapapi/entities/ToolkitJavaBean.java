package com.gigaspaces.gigapro.xapapi.entities;

import com.gigaspaces.annotation.pojo.SpaceId;

public class ToolkitJavaBean extends ToolkitBaseEntity {

    @SpaceId // Unavoidable annotation
    public String getObjectId() {
        return _objectId;
    }

    public void setObjectId(String objectId) {
        _objectId = objectId;
    }

    public Integer getObjectType() {
        return _objectType;
    }

    public void setObjectType(Integer objectType) {
        _objectType = objectType;
    }
}

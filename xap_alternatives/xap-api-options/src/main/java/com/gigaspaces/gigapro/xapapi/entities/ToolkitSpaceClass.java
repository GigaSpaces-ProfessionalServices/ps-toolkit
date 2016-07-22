package com.gigaspaces.gigapro.xapapi.entities;

import com.gigaspaces.annotation.pojo.*;
import com.gigaspaces.metadata.index.SpaceIndexType;

@SpaceClass
public class ToolkitSpaceClass extends ToolkitBaseEntity {

    @SpaceId(autoGenerate = false) @SpaceRouting
    public String getObjectId() {
        return _objectId;
    }

    public void setObjectId(String objectId) {
        _objectId = objectId;
    }

    @SpaceIndex(type = SpaceIndexType.EXTENDED, unique = false)
    public Integer getObjectType() {
        return _objectType;
    }

    public void setObjectType(Integer objectType) {
        _objectType = objectType;
    }
}

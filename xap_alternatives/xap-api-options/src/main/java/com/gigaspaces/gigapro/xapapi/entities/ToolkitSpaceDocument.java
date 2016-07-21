package com.gigaspaces.gigapro.xapapi.entities;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.gigapro.xapapi.options.DataObjectFactory;

import java.util.Random;

public class ToolkitSpaceDocument extends SpaceDocument {

    public ToolkitSpaceDocument() {
        Random random = DataObjectFactory.ToolkitRandom;
        setProperty("ObjectId", ToolkitBaseEntity.GetRandomHexString(16));
        setProperty("ObjectType", random.nextInt(256));
        setProperty("ObjectData", random.nextDouble());
        setProperty("ObjectFlag", random.nextBoolean());

        // SpaceTypeDescriptor?
    }
}

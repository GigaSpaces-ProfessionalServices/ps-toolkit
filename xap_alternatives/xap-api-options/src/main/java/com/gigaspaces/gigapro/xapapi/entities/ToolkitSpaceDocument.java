package com.gigaspaces.gigapro.xapapi.entities;

import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.gigapro.xapapi.options.*;

import java.util.Random;

public class ToolkitSpaceDocument extends SpaceDocument {

    public ToolkitSpaceDocument() {}

    public ToolkitSpaceDocument RandomInitialize() {
        Random random = DataObjectFactory.ToolkitRandom;
        setProperty(DataObjectFactory.OBJECT_ID,
            AbstractUtilities.GetRandomHexString(16));

        // Random methods return primitives that are wrapped anyway
        setProperty(DataObjectFactory.OBJECT_TYPE, random.nextInt(256));
        setProperty(DataObjectFactory.OBJECT_DATA, random.nextDouble());
        setProperty(DataObjectFactory.OBJECT_FLAG, random.nextBoolean());
        setTypeName(DataObjectFactory.TOOLKIT_SPACE_DOCUMENT_TYPE);
        return this;
    }
}

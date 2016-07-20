package com.gigaspaces.gigapro.xapapi.options;

//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;

public interface SpacePerformanceHarness {
    // generic?

    enum SpaceAccessMode {
        CLUSTERED_PROXY,
        REMOTE_EXECUTOR,
        COLLOCATED_SPACE
    }

    //SpaceClass[] readSpaceClassObjects(String[] ids, ... );
    //Object[] writeSpaceClassObjects(@SpaceClass[] objects, ...);

    Object[] readObjectsFromSpace(String[] objectIds,
        SpaceAccessMode spaceAccessMode,
        DataObjectFactory.ToolkitObjectType objectTypeMode);
    void writeObjectsToSpace(Object[] dataObjects,
        SpaceAccessMode spaceAccessMode,
        DataObjectFactory.ToolkitObjectType objectTypeMode);
}

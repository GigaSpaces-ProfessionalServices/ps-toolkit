package com.gigaspaces.gigapro.xapapioptions;

//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;

public interface SpacePerformanceHarness {
    // generic?

    enum SpaceAccessMode {
        CLUSTERED_PROXY,
        REMOTE_EXECUTOR,
        COLLOCATED_SPACE
    }

    enum ObjectTypeMode {
        JAVA_BEAN,
        SPACE_CLASS,
        SPACE_DOCUMENT
    }

    //SpaceClass[] readSpaceClassObjects(String[] ids, ... );
    //Object[] writeSpaceClassObjects(@SpaceClass[] objects, ...);

    Object[] readObjectsFromSpace(String[] objectIds,
        SpaceAccessMode spaceAccessMode, ObjectTypeMode objectTypeMode);
    void writeObjectsToSpace(Object[] dataObjects,
        SpaceAccessMode spaceAccessMode, ObjectTypeMode objectTypeMode);
}

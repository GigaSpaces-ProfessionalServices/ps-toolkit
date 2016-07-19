package com.gigaspaces.pstoolkit.xapapioptions;

//import java.util.concurrent.Executor;
//import java.util.concurrent.Executors;

public interface SpacePerformanceHarness {
    // generic?

    public enum SpaceAccessMode {
        CLUSTERED_PROXY,
        REMOTE_EXECUTOR
    }

    public enum ObjectTypeMode {
        SPACE_CLASS,
        JAVA_BEAN
    }

    //SpaceClass[] readSpaceClassObjects(String[] ids, ... );
    //Object[] writeSpaceClassObjects(@SpaceClass[] objects, ...);

    Object[] readObjectsFromSpace(String[] objectIds,
        SpaceAccessMode spaceAccessMode, ObjectTypeMode objectTypeMode);
    void writeObjectsToSpace(Object[] dataObjects,
        SpaceAccessMode spaceAccessMode, ObjectTypeMode objectTypeMode);
}

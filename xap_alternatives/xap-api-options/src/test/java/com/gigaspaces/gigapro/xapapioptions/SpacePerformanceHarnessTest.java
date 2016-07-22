package com.gigaspaces.gigapro.xapapioptions;

import org.junit.*;
import static org.junit.Assert.*;

import com.j_spaces.core.IJSpace;
import org.openspaces.core.*;
import org.openspaces.core.space.SpaceProxyConfigurer;
import com.gigaspaces.document.SpaceDocument;
import com.gigaspaces.gigapro.xapapi.entities.*;
import com.gigaspaces.gigapro.xapapi.options.*;

public class SpacePerformanceHarnessTest
{
    private static final int ARRAY_SIZE = 100;

    private DataObjectFactory _factory;
    private GigaSpace _gigaSpace;

    @Before
    public void prepareObjectFactory()
    {
        _factory = new DataObjectFactory();

        // Initialize giga-space interface to target cluster
        SpaceProxyConfigurer spaceProxyConfigurer = new
            SpaceProxyConfigurer(DataObjectFactory.TOOLKIT_SPACE_NAME);
        IJSpace ijSpace = spaceProxyConfigurer.space();
        _gigaSpace = new GigaSpaceConfigurer(ijSpace).gigaSpace();

        // Register document type descriptor with the space
        _gigaSpace.getTypeManager().registerTypeDescriptor(DataObjectFactory.TypeDescriptor);
    }

    @Test
    public void testSpaceProxyPerformance()
    {
        Object[] sourceJavaBeanArray = _factory.GenerateArray
            (DataObjectFactory.ToolkitObjectType.JAVA_BEAN, ARRAY_SIZE);

        ToolkitJavaBean javaBean = (ToolkitJavaBean) sourceJavaBeanArray[0];
        assertTrue("Failed to initialize java bean object instance", javaBean != null);
        System.out.print("First java bean id: ");
        System.out.println(javaBean.getObjectId());

        Object[] sourceSpaceClassArray = _factory.GenerateArray
            (DataObjectFactory.ToolkitObjectType.SPACE_CLASS, ARRAY_SIZE);

        ToolkitSpaceClass spaceClass = (ToolkitSpaceClass) sourceSpaceClassArray[0];
        assertTrue("Failed to initialize space class object instance", spaceClass != null);
        System.out.print("First space class id: ");
        System.out.println(spaceClass.getObjectId());

        Object[] sourceSpaceDocumentArray = _factory.GenerateArray
            (DataObjectFactory.ToolkitObjectType.SPACE_DOCUMENT, ARRAY_SIZE);

        ToolkitSpaceDocument spaceDocument = (ToolkitSpaceDocument) sourceSpaceDocumentArray[0];
        assertTrue("Failed to initialize space document instance", spaceDocument != null);
        System.out.print("First space document id: ");
        System.out.println((String) spaceDocument.getProperty("objectId"));

        System.out.println();
        NanoTimeHelper timeChecker = null;

        timeChecker = new NanoTimeHelper();
        _gigaSpace.writeMultiple(sourceJavaBeanArray);
        timeChecker.printElapsedTime("Writing java bean array");

        timeChecker = new NanoTimeHelper();
        _gigaSpace.writeMultiple(sourceSpaceClassArray);
        timeChecker.printElapsedTime("Writing space class array");

        timeChecker = new NanoTimeHelper();
        _gigaSpace.writeMultiple(sourceSpaceDocumentArray);
        timeChecker.printElapsedTime("Writing space document array");

        timeChecker = new NanoTimeHelper();
        ToolkitJavaBean[] targetJavaBeanArray =
            _gigaSpace.readMultiple(new ToolkitJavaBean(), ARRAY_SIZE);
        timeChecker.printElapsedTime("Reading java bean array");

        timeChecker = new NanoTimeHelper();
        ToolkitSpaceClass[] targetSpaceClassArray =
            _gigaSpace.readMultiple(new ToolkitSpaceClass(), ARRAY_SIZE);
        timeChecker.printElapsedTime("Reading space class array");

        timeChecker = new NanoTimeHelper();
        SpaceDocument[] targetSpaceDocumentArray =
            _gigaSpace.readMultiple(new ToolkitSpaceDocument(), ARRAY_SIZE);
        timeChecker.printElapsedTime("Reading space document array");
    }
}

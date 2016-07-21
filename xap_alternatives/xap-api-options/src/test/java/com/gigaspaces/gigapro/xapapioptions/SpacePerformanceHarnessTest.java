package com.gigaspaces.gigapro.xapapioptions;

import junit.framework.*;

import com.gigaspaces.gigapro.xapapi.entities.*;
import com.gigaspaces.gigapro.xapapi.options.*;

/**
 * Unit test for simple App.
 */
public class SpacePerformanceHarnessTest extends TestCase
{
    private DataObjectFactory _factory;

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SpacePerformanceHarnessTest(String testName)
    {
        super(testName);
        _factory = new DataObjectFactory();
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite(SpacePerformanceHarnessTest.class);
    }

    public void testApp()
    {
        Object[] javaBeanArray = _factory.GenerateArray
            (DataObjectFactory.ToolkitObjectType.JAVA_BEAN, 10);
        assertTrue("Failed to initialize java bean object instance",
            javaBeanArray[0] != null);

        ToolkitJavaBean javaBean = (ToolkitJavaBean) javaBeanArray[0];
        System.out.print("First java bean id: ");
        System.out.println(javaBean.getObjectId());

        Object[] spaceClassArray = _factory.GenerateArray
                (DataObjectFactory.ToolkitObjectType.SPACE_CLASS, 10);
        assertTrue("Failed to initialize space class object instance",
                spaceClassArray[0] != null);

        ToolkitSpaceClass spaceClass = (ToolkitSpaceClass) spaceClassArray[0];
        System.out.print("First space class id: ");
        System.out.println(spaceClass.getObjectId());
    }
}

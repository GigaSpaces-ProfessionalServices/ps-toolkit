package com.gigaspaces.gigapro.xapapioptions;

import org.junit.*;
import static org.junit.Assert.*;

import com.gigaspaces.gigapro.xapapi.entities.*;
import com.gigaspaces.gigapro.xapapi.options.*;

public class SpacePerformanceHarnessTest
{
    private DataObjectFactory _factory;

    @Before
    public void prepareObjectFactory()
    {
        _factory = new DataObjectFactory();
    }

    @Test
    public void testSpacePerformance()
    {
        Object[] javaBeanArray = _factory.GenerateArray
            (DataObjectFactory.ToolkitObjectType.JAVA_BEAN, 10);

        ToolkitJavaBean javaBean = (ToolkitJavaBean) javaBeanArray[0];
        assertTrue("Failed to initialize java bean object instance", javaBean != null);
        System.out.print("First java bean id: ");
        System.out.println(javaBean.getObjectId());

        Object[] spaceClassArray = _factory.GenerateArray
            (DataObjectFactory.ToolkitObjectType.SPACE_CLASS, 10);

        ToolkitSpaceClass spaceClass = (ToolkitSpaceClass) spaceClassArray[0];
        assertTrue("Failed to initialize space class object instance", spaceClass != null);
        System.out.print("First space class id: ");
        System.out.println(spaceClass.getObjectId());
    }
}

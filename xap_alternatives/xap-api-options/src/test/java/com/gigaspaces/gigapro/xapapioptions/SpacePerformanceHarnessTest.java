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
    private static final int EPOCH_COUNT = 10;
    private static final int LOOP_SIZE = 5000;
    private static final int ARRAY_SIZE = 100000;

    private static final String WRITE_JAVA_BEAN_ARRAY = "Writing java bean array";
    private static final String WRITE_SPACE_CLASS_ARRAY = "Writing space class array";
    private static final String WRITE_SPACE_DOCUMENT_ARRAY = "Writing space document array";
    private static final String READ_JAVA_BEAN_ARRAY = "Reading java bean array";
    private static final String READ_SPACE_CLASS_ARRAY = "Reading space class array";
    private static final String READ_SPACE_DOCUMENT_ARRAY = "Reading space document array";
    private static final String WRITE_JAVA_BEAN_LOOP = "Writing java beans in a loop";
    private static final String WRITE_SPACE_CLASS_LOOP = "Writing space classes in a loop";
    private static final String WRITE_SPACE_DOCUMENT_LOOP = "Writing space documents in a loop";
    private static final String READ_JAVA_BEAN_LOOP = "Reading java beans in a loop";
    private static final String READ_SPACE_CLASS_LOOP = "Reading space classes in a loop";
    private static final String READ_SPACE_DOCUMENT_LOOP = "Reading space documents in a loop";

    private DataObjectFactory _factory;
    private GigaSpace _gigaSpace;

    private ToolkitJavaBean _blankJavaBean = new ToolkitJavaBean();
    private ToolkitSpaceClass _blankSpaceClass = new ToolkitSpaceClass();
    private ToolkitSpaceDocument _blankSpaceDocument = new ToolkitSpaceDocument();

    private Object[] _sourceJavaBeanArray = null;
    private Object[] _sourceSpaceClassArray = null;
    private Object[] _sourceSpaceDocumentArray = null;

    private void clearSpaceObjects() {
        _gigaSpace.clear(_blankJavaBean);
        _gigaSpace.clear(_blankSpaceClass);
        _gigaSpace.clear(_blankSpaceDocument);
    }

    private void generateSourceArrays() {

        System.out.println();
        System.out.println("=> Array size " +
            AbstractUtilities.NiceInteger(ARRAY_SIZE) + ", Loop size " +
            AbstractUtilities.NiceInteger(LOOP_SIZE));
        System.out.println();
        NanoTimeHelper timeChecker = new NanoTimeHelper(null);

        timeChecker.reset();
        _sourceJavaBeanArray = _factory.GenerateArray
            (DataObjectFactory.ToolkitObjectType.JAVA_BEAN, ARRAY_SIZE);
        timeChecker.printElapsedTime("Generating java bean array");

        ToolkitJavaBean javaBean = (ToolkitJavaBean) _sourceJavaBeanArray[0];
        assertTrue("Failed to initialize java bean object instance", javaBean != null);

        /* System.out.print("First java bean id: ");
        System.out.println(javaBean.getObjectId()); */

        timeChecker.reset();
        _sourceSpaceClassArray = _factory.GenerateArray
            (DataObjectFactory.ToolkitObjectType.SPACE_CLASS, ARRAY_SIZE);
        timeChecker.printElapsedTime("Generating space class array");

        ToolkitSpaceClass spaceClass = (ToolkitSpaceClass) _sourceSpaceClassArray[0];
        assertTrue("Failed to initialize space class object instance", spaceClass != null);

        /* System.out.print("First space class id: ");
        System.out.println(spaceClass.getObjectId()); */

        timeChecker.reset();
        _sourceSpaceDocumentArray = _factory.GenerateArray
            (DataObjectFactory.ToolkitObjectType.SPACE_DOCUMENT, ARRAY_SIZE);
        timeChecker.printElapsedTime("Generating space document array");

        ToolkitSpaceDocument spaceDocument = (ToolkitSpaceDocument) _sourceSpaceDocumentArray[0];
        assertTrue("Failed to initialize space document instance", spaceDocument != null);

        /* System.out.print("First space document id: ");
        System.out.println((String) spaceDocument.getProperty("objectId")); */
    }

    public void checkObjectCounts(int expectedCount) {
        int javaBeanCount = _gigaSpace.count(new ToolkitJavaBean());
        assertTrue("Incorrect number of java beans stored in the space",
            javaBeanCount == expectedCount);

        int spaceClassCount = _gigaSpace.count(new ToolkitSpaceClass());
        assertTrue("Incorrect number of space classes stored in the space",
            spaceClassCount == expectedCount);

        ToolkitSpaceDocument emptySpaceDocument = new ToolkitSpaceDocument();
        emptySpaceDocument.setTypeName(DataObjectFactory.TOOLKIT_SPACE_DOCUMENT_TYPE);
        int spaceDocumentCount = _gigaSpace.count(emptySpaceDocument);
        assertTrue("Incorrect number of space documents stored in the space",
            spaceDocumentCount == expectedCount);
    }

    @Before
    public void prepareObjectFactory()
    {
        _factory = new DataObjectFactory();

        // Initialize gigaspace interface to target grid
        SpaceProxyConfigurer spaceProxyConfigurer = new
            SpaceProxyConfigurer(DataObjectFactory.TOOLKIT_SPACE_NAME);
        IJSpace ijSpace = spaceProxyConfigurer.space();
        _gigaSpace = new GigaSpaceConfigurer(ijSpace).gigaSpace();

        // Register document type descriptor with the space
        _gigaSpace.getTypeManager().registerTypeDescriptor(DataObjectFactory.TypeDescriptor);
        clearSpaceObjects(); // For multiple runs from the IDE
        generateSourceArrays();
    }

    @Test
    public void testBatch() {
        System.out.println();
        System.out.println("WARMING PHASE: SPACE PROXY MODE");
        System.out.println();
        testSpaceProxyPerformance(null);

        ActionSpreadsheet dataSpreadsheet = new ActionSpreadsheet("unit");
        for (int i = 0; i < EPOCH_COUNT; i++) {
            System.out.println();
            System.out.println("MEASUREMENT #" +
                String.valueOf(i + 1) + ": SPACE PROXY MODE");
            System.out.println();
            testSpaceProxyPerformance(dataSpreadsheet);
        }

        System.out.println();
        System.out.println("AVERAGE RESULTS: SPACE PROXY MODE");
        System.out.println();

        dataSpreadsheet.printAverage(WRITE_JAVA_BEAN_ARRAY);
        dataSpreadsheet.printAverage(WRITE_SPACE_CLASS_ARRAY);
        dataSpreadsheet.printAverage(WRITE_SPACE_DOCUMENT_ARRAY);
        System.out.println();

        dataSpreadsheet.printAverage(READ_JAVA_BEAN_ARRAY);
        dataSpreadsheet.printAverage(READ_SPACE_CLASS_ARRAY);
        dataSpreadsheet.printAverage(READ_SPACE_DOCUMENT_ARRAY);
        System.out.println();

        dataSpreadsheet.printAverage(WRITE_JAVA_BEAN_LOOP);
        dataSpreadsheet.printAverage(WRITE_SPACE_CLASS_LOOP);
        dataSpreadsheet.printAverage(WRITE_SPACE_DOCUMENT_LOOP);
        System.out.println();

        dataSpreadsheet.printAverage(READ_JAVA_BEAN_LOOP);
        dataSpreadsheet.printAverage(READ_SPACE_CLASS_LOOP);
        dataSpreadsheet.printAverage(READ_SPACE_DOCUMENT_LOOP);
    }

    public void testSpaceProxyPerformance(ActionSpreadsheet spreadsheet)
    {
        NanoTimeHelper timeChecker = new NanoTimeHelper(spreadsheet);

        _gigaSpace.writeMultiple(_sourceJavaBeanArray);
        timeChecker.printElapsedTime(WRITE_JAVA_BEAN_ARRAY);

        _gigaSpace.writeMultiple(_sourceSpaceClassArray);
        timeChecker.printElapsedTime(WRITE_SPACE_CLASS_ARRAY);

        _gigaSpace.writeMultiple(_sourceSpaceDocumentArray);
        timeChecker.printElapsedTime(WRITE_SPACE_DOCUMENT_ARRAY);

        System.out.println();
        checkObjectCounts(ARRAY_SIZE);
        timeChecker.reset();

        ToolkitJavaBean[] readJavaBeanArray =
            _gigaSpace.readMultiple(_blankJavaBean, ARRAY_SIZE);
        timeChecker.printElapsedTime(READ_JAVA_BEAN_ARRAY);

        ToolkitSpaceClass[] readSpaceClassArray =
            _gigaSpace.readMultiple(_blankSpaceClass, ARRAY_SIZE);
        timeChecker.printElapsedTime(READ_SPACE_CLASS_ARRAY);

        SpaceDocument[] readSpaceDocumentArray =
            _gigaSpace.readMultiple(_blankSpaceDocument, ARRAY_SIZE);
        timeChecker.printElapsedTime(READ_SPACE_DOCUMENT_ARRAY);

        System.out.println();
        clearSpaceObjects();
        timeChecker.reset();

        for (int i = 0; i < LOOP_SIZE; i++)
            _gigaSpace.write(_sourceJavaBeanArray[i]);
        timeChecker.printElapsedTime(WRITE_JAVA_BEAN_LOOP);

        for (int i = 0; i < LOOP_SIZE; i++)
            _gigaSpace.write(_sourceSpaceClassArray[i]);
        timeChecker.printElapsedTime(WRITE_SPACE_CLASS_LOOP);

        for (int i = 0; i < LOOP_SIZE; i++)
            _gigaSpace.write(_sourceSpaceDocumentArray[i]);
        timeChecker.printElapsedTime(WRITE_SPACE_DOCUMENT_LOOP);

        System.out.println();
        checkObjectCounts(LOOP_SIZE);
        timeChecker.reset();

        ToolkitJavaBean templateJavaBean = new ToolkitJavaBean();
        for (int i = 0; i < LOOP_SIZE; i++) {
            ToolkitJavaBean sourceJavaBean = (ToolkitJavaBean) _sourceJavaBeanArray[i];
            templateJavaBean.setObjectId(sourceJavaBean.getObjectId());
            ToolkitJavaBean readJavaBean = _gigaSpace.read(templateJavaBean);
        }
        timeChecker.printElapsedTime(READ_JAVA_BEAN_LOOP);

        ToolkitSpaceClass templateSpaceClass = new ToolkitSpaceClass();
        for (int i = 0; i < LOOP_SIZE; i++) {
            ToolkitSpaceClass sourceSpaceClass = (ToolkitSpaceClass) _sourceSpaceClassArray[i];
            templateSpaceClass.setObjectId(sourceSpaceClass.getObjectId());
            ToolkitSpaceClass readSpaceClass = _gigaSpace.read(templateSpaceClass);
        }
        timeChecker.printElapsedTime(READ_SPACE_CLASS_LOOP);

        ToolkitSpaceDocument templateSpaceDocument = new ToolkitSpaceDocument();
        templateSpaceDocument.setTypeName(DataObjectFactory.TOOLKIT_SPACE_DOCUMENT_TYPE);
        for (int i = 0; i < LOOP_SIZE; i++) {
            ToolkitSpaceDocument sourceSpaceDocument = (ToolkitSpaceDocument) _sourceSpaceDocumentArray[i];
            templateSpaceDocument.setProperty(DataObjectFactory.OBJECT_ID,
                sourceSpaceDocument.getProperty(DataObjectFactory.OBJECT_ID));
            SpaceDocument readSpaceDocument = _gigaSpace.read(templateSpaceDocument);
        }
        timeChecker.printElapsedTime(READ_SPACE_DOCUMENT_LOOP);

        clearSpaceObjects();
    }
}

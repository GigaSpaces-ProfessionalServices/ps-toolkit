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

    private static final String AVERAGE_FORMAT = "0.#########";

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

    private static DataObjectFactory _factory;
    private static GigaSpace _gigaSpace;

    private static ToolkitJavaBean _blankJavaBean = new ToolkitJavaBean();
    private static ToolkitSpaceClass _blankSpaceClass = new ToolkitSpaceClass();
    private static ToolkitSpaceDocument _blankSpaceDocument = new ToolkitSpaceDocument();

    private static Object[] _sourceJavaBeanArray = null;
    private static Object[] _sourceSpaceClassArray = null;
    private static Object[] _sourceSpaceDocumentArray = null;

    private static void clearSpaceObjects() {
        _gigaSpace.clear(_blankJavaBean);
        _gigaSpace.clear(_blankSpaceClass);
        _gigaSpace.clear(_blankSpaceDocument);
    }

    private static void clearSpaceObjects(ToolkitObjectType objectType) {
        switch (objectType) {
            case JAVA_BEAN:
                _gigaSpace.clear(_blankJavaBean);
                return;
            case SPACE_CLASS:
                _gigaSpace.clear(_blankSpaceClass);
                return;
            case SPACE_DOCUMENT:
                _gigaSpace.clear(_blankSpaceDocument);
                return;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void checkObjectCounts(int expectedCount) {
        checkObjectCounts(expectedCount, ToolkitObjectType.JAVA_BEAN);
        checkObjectCounts(expectedCount, ToolkitObjectType.SPACE_CLASS);
        checkObjectCounts(expectedCount, ToolkitObjectType.SPACE_DOCUMENT);
    }

    public void checkObjectCounts(int expectedCount, ToolkitObjectType objectType) {

        Object templateObject;
        switch (objectType) {
            case JAVA_BEAN:
                templateObject = new ToolkitJavaBean();
                break;
            case SPACE_CLASS:
                templateObject = new ToolkitSpaceClass();
                break;
            case SPACE_DOCUMENT:
                ToolkitSpaceDocument emptyDocument = new ToolkitSpaceDocument();
                emptyDocument.setTypeName(DataObjectFactory.TOOLKIT_SPACE_DOCUMENT_TYPE);
                templateObject = emptyDocument;
                break;
            default:
                throw new IllegalArgumentException();
        }

        int objectCount = _gigaSpace.count(templateObject);
        assertTrue("Incorrect number of objects stored in the space: " +
            objectType.toString(), objectCount == expectedCount);
    }

    private static void generateSourceArrays() {
        System.out.println();
        System.out.println("=> Array size " +
                AbstractUtilities.NiceInteger(ARRAY_SIZE) + ", Loop size " +
                AbstractUtilities.NiceInteger(LOOP_SIZE));
        System.out.println();
        NanoTimeHelper timeChecker = new NanoTimeHelper(null);

        timeChecker.reset();
        _sourceJavaBeanArray = _factory.GenerateArray
                (ToolkitObjectType.JAVA_BEAN, ARRAY_SIZE);
        timeChecker.printElapsedTime("Generating java bean array");
        assertTrue("Failed to initialize java bean object instance",
            _sourceJavaBeanArray[0] != null);

        timeChecker.reset();
        _sourceSpaceClassArray = _factory.GenerateArray
                (ToolkitObjectType.SPACE_CLASS, ARRAY_SIZE);
        timeChecker.printElapsedTime("Generating space class array");
        assertTrue("Failed to initialize space class object instance",
            _sourceSpaceClassArray[0] != null);

        timeChecker.reset();
        _sourceSpaceDocumentArray = _factory.GenerateArray
                (ToolkitObjectType.SPACE_DOCUMENT, ARRAY_SIZE);
        timeChecker.printElapsedTime("Generating space document array");
        assertTrue("Failed to initialize space document instance",
            _sourceSpaceDocumentArray[0] != null);
    }

    @BeforeClass
    public static void prepareObjectFactory()
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
    public void testRemoteExecutor() {
        // TODO:
    }

    @Test
    public void testClusteredProxy() {
        System.out.println();
        System.out.println("WARMING PHASE: CLUSTERED PROXY MODE");
        System.out.println();
        testProxySteps(null);

        ActionSpreadsheet dataSpreadsheet = new ActionSpreadsheet("unit");
        for (int i = 0; i < EPOCH_COUNT; i++) {
            System.out.println();
            System.out.println("MEASUREMENT #" +
                String.valueOf(i + 1) + ": CLUSTERED PROXY MODE");
            System.out.println();
            testProxySteps(dataSpreadsheet);
        }

        System.out.println();
        System.out.println("AVERAGE RESULTS: CLUSTERED PROXY MODE");
        System.out.println();

        dataSpreadsheet.printAverage(WRITE_JAVA_BEAN_ARRAY, AVERAGE_FORMAT);
        dataSpreadsheet.printAverage(READ_JAVA_BEAN_ARRAY, AVERAGE_FORMAT);
        System.out.println();

        dataSpreadsheet.printAverage(WRITE_SPACE_CLASS_ARRAY, AVERAGE_FORMAT);
        dataSpreadsheet.printAverage(READ_SPACE_CLASS_ARRAY, AVERAGE_FORMAT);
        System.out.println();

        dataSpreadsheet.printAverage(WRITE_SPACE_DOCUMENT_ARRAY, AVERAGE_FORMAT);
        dataSpreadsheet.printAverage(READ_SPACE_DOCUMENT_ARRAY, AVERAGE_FORMAT);
        System.out.println();

        dataSpreadsheet.printAverage(WRITE_JAVA_BEAN_LOOP, AVERAGE_FORMAT);
        dataSpreadsheet.printAverage(READ_JAVA_BEAN_LOOP, AVERAGE_FORMAT);
        System.out.println();

        dataSpreadsheet.printAverage(WRITE_SPACE_CLASS_LOOP, AVERAGE_FORMAT);
        dataSpreadsheet.printAverage(READ_SPACE_CLASS_LOOP, AVERAGE_FORMAT);
        System.out.println();

        dataSpreadsheet.printAverage(WRITE_SPACE_DOCUMENT_LOOP, AVERAGE_FORMAT);
        dataSpreadsheet.printAverage(READ_SPACE_DOCUMENT_LOOP, AVERAGE_FORMAT);
    }

    public void testProxySteps(ActionSpreadsheet spreadsheet)
    {
        NanoTimeHelper timeChecker = new NanoTimeHelper(spreadsheet);

        _gigaSpace.writeMultiple(_sourceJavaBeanArray);
        timeChecker.printElapsedTime(WRITE_JAVA_BEAN_ARRAY);

        ToolkitJavaBean[] readJavaBeanArray =
                _gigaSpace.readMultiple(_blankJavaBean, ARRAY_SIZE);
        timeChecker.printElapsedTime(READ_JAVA_BEAN_ARRAY);

        checkObjectCounts(ARRAY_SIZE, ToolkitObjectType.JAVA_BEAN);
        clearSpaceObjects(ToolkitObjectType.JAVA_BEAN);
        System.out.println();
        timeChecker.reset();

        _gigaSpace.writeMultiple(_sourceSpaceClassArray);
        timeChecker.printElapsedTime(WRITE_SPACE_CLASS_ARRAY);

        ToolkitSpaceClass[] readSpaceClassArray =
                _gigaSpace.readMultiple(_blankSpaceClass, ARRAY_SIZE);
        timeChecker.printElapsedTime(READ_SPACE_CLASS_ARRAY);

        checkObjectCounts(ARRAY_SIZE, ToolkitObjectType.SPACE_CLASS);
        clearSpaceObjects(ToolkitObjectType.SPACE_CLASS);
        System.out.println();
        timeChecker.reset();

        _gigaSpace.writeMultiple(_sourceSpaceDocumentArray);
        timeChecker.printElapsedTime(WRITE_SPACE_DOCUMENT_ARRAY);

        SpaceDocument[] readSpaceDocumentArray =
            _gigaSpace.readMultiple(_blankSpaceDocument, ARRAY_SIZE);
        timeChecker.printElapsedTime(READ_SPACE_DOCUMENT_ARRAY);

        checkObjectCounts(ARRAY_SIZE, ToolkitObjectType.SPACE_DOCUMENT);
        clearSpaceObjects(ToolkitObjectType.SPACE_DOCUMENT);
        System.out.println();
        timeChecker.reset();

        for (int i = 0; i < LOOP_SIZE; i++)
            _gigaSpace.write(_sourceJavaBeanArray[i]);
        timeChecker.printElapsedTime(WRITE_JAVA_BEAN_LOOP);

        ToolkitJavaBean templateJavaBean = new ToolkitJavaBean();
        for (int i = 0; i < LOOP_SIZE; i++) {
            ToolkitJavaBean sourceJavaBean = (ToolkitJavaBean) _sourceJavaBeanArray[i];
            templateJavaBean.setObjectId(sourceJavaBean.getObjectId());
            ToolkitJavaBean readJavaBean = _gigaSpace.read(templateJavaBean);
        }
        timeChecker.printElapsedTime(READ_JAVA_BEAN_LOOP);

        checkObjectCounts(LOOP_SIZE, ToolkitObjectType.JAVA_BEAN);
        clearSpaceObjects(ToolkitObjectType.JAVA_BEAN);
        System.out.println();
        timeChecker.reset();

        for (int i = 0; i < LOOP_SIZE; i++)
            _gigaSpace.write(_sourceSpaceClassArray[i]);
        timeChecker.printElapsedTime(WRITE_SPACE_CLASS_LOOP);

        ToolkitSpaceClass templateSpaceClass = new ToolkitSpaceClass();
        for (int i = 0; i < LOOP_SIZE; i++) {
            ToolkitSpaceClass sourceSpaceClass = (ToolkitSpaceClass) _sourceSpaceClassArray[i];
            templateSpaceClass.setObjectId(sourceSpaceClass.getObjectId());
            ToolkitSpaceClass readSpaceClass = _gigaSpace.read(templateSpaceClass);
        }
        timeChecker.printElapsedTime(READ_SPACE_CLASS_LOOP);

        checkObjectCounts(LOOP_SIZE, ToolkitObjectType.SPACE_CLASS);
        clearSpaceObjects(ToolkitObjectType.SPACE_CLASS);
        System.out.println();
        timeChecker.reset();

        for (int i = 0; i < LOOP_SIZE; i++)
            _gigaSpace.write(_sourceSpaceDocumentArray[i]);
        timeChecker.printElapsedTime(WRITE_SPACE_DOCUMENT_LOOP);

        ToolkitSpaceDocument templateSpaceDocument = new ToolkitSpaceDocument();
        templateSpaceDocument.setTypeName(DataObjectFactory.TOOLKIT_SPACE_DOCUMENT_TYPE);
        for (int i = 0; i < LOOP_SIZE; i++) {
            ToolkitSpaceDocument sourceSpaceDocument = (ToolkitSpaceDocument) _sourceSpaceDocumentArray[i];
            templateSpaceDocument.setProperty(DataObjectFactory.OBJECT_ID,
                sourceSpaceDocument.getProperty(DataObjectFactory.OBJECT_ID));
            SpaceDocument readSpaceDocument = _gigaSpace.read(templateSpaceDocument);
        }
        timeChecker.printElapsedTime(READ_SPACE_DOCUMENT_LOOP);

        checkObjectCounts(LOOP_SIZE, ToolkitObjectType.SPACE_DOCUMENT);
        clearSpaceObjects(ToolkitObjectType.SPACE_DOCUMENT);
    }
}

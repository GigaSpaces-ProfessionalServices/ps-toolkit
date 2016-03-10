package com.gigaspaces.gigapro.web.validation.service;

import com.gigaspaces.gigapro.web.validation.model.ValidationRequest;
import com.gigaspaces.gigapro.web.validation.model.ValidationResponse;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;

import static java.lang.System.getenv;
import static java.nio.file.Files.exists;
import static java.nio.file.Paths.get;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.Assume.assumeNoException;
import static org.junit.Assume.assumeTrue;

@SuppressWarnings("Duplicates")
public class JavaHomeValidationServiceTest {

    private JavaHomeValidationService validationService = new JavaHomeValidationService();

    @Test
    public void validateJavaHomeIsSetTest() {
        String javaHomePath = getenv("JAVA_HOME");
        assumeTrue("Environment variable 'JAVA_HOME' is not set", isNotBlank(javaHomePath));

        Path javaHome = get(javaHomePath);
        assumeTrue("Folder " + javaHome.toString() +" doesn't exist", exists(javaHome));

        ValidationRequest request = new ValidationRequest();
        request.setValue(javaHome.toString());
        ValidationResponse response = validationService.validate(request);

        Assert.assertTrue(response.isValid());
    }

    @Test
    public void validateWrongJavaHomeTest() {
        String path = ofNullable(getenv("TEMP")).orElse(getenv("HOME"));
        assumeTrue("Environment variables 'TEMP' and 'HOME' are not set", isNotBlank(path));

        Path javaHome = get(path);
        assumeTrue("Folder " + javaHome.toString() +" doesn't exist", exists(javaHome));

        ValidationRequest request = new ValidationRequest();
        request.setValue(javaHome.toString());
        ValidationResponse response = validationService.validate(request);

        Assert.assertFalse(response.isValid());
    }

    @Test
    public void isValidJavaHomeTest() throws InvocationTargetException, IllegalAccessException {
        Method isValidJavaHome = null;
        try {
            isValidJavaHome = JavaHomeValidationService.class.getDeclaredMethod("isValidJavaHome", Path.class);
        } catch (NoSuchMethodException e) {
            assumeNoException("Cannot find method boolean isValidJavaHome(..) in class JavaHomeValidationService", e);
        }
        isValidJavaHome.setAccessible(true);

        String javaHomePath = getenv("JAVA_HOME");
        assumeTrue("Environment variable 'JAVA_HOME' is not set", isNotBlank(javaHomePath));

        Path javaHome = get(javaHomePath);
        assumeTrue("Folder " + javaHome.toString() +" doesn't exist", exists(javaHome));

        Boolean validationResult = (Boolean) isValidJavaHome.invoke(new JavaHomeValidationService(), javaHome);
        Assert.assertTrue(validationResult);
    }
}

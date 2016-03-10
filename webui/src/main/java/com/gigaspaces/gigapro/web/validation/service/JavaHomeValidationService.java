package com.gigaspaces.gigapro.web.validation.service;

import com.gigaspaces.gigapro.web.validation.model.ValidationRequest;
import com.gigaspaces.gigapro.web.validation.model.ValidationResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static java.nio.file.Files.*;
import static java.nio.file.Paths.get;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.SystemUtils.IS_OS_UNIX;

@Service
public class JavaHomeValidationService implements ValidationService {

    public static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    @Override
    public ValidationResponse validate(ValidationRequest request) {
        ValidationResponse response = new ValidationResponse();
        if (request == null || isBlank(request.getValue())) {
            response.setValue("Invalid value: " + request);
            return response;
        }

        Path pathToValidate = get(request.getValue());
        try {
            if (isEqualToJavaHome(pathToValidate) || isValidJavaHome(pathToValidate)) {
                response.setValid(true);
                response.setValue("JAVA_HOME: " + request.getValue() + " is ok!");
            } else {
                response.setValue("JDK cannot be found by this path: " + request.getValue());
            }
        } catch (Exception e) {
            response.setValue("Error: " + e.getMessage());
        }
        return response;
    }

    private boolean isValidJavaHome(Path pathToValidate) {
        Path bin = get(pathToValidate.toString() + SEPARATOR + "bin");
        Path java = get(bin.toString() + SEPARATOR + (IS_OS_UNIX ? "java" : "java.exe"));
        return exists(pathToValidate) && isDirectory(pathToValidate) &&
                exists(bin) && isDirectory(bin) &&
                exists(java) && isExecutable(java);
    }

    private boolean isEqualToJavaHome(Path pathToValidate) throws IOException {
        String javaHomePath = System.getenv("JAVA_HOME");
        return isNotBlank(javaHomePath) && isSameFile(get(javaHomePath), pathToValidate);
    }
}

package com.gigaspaces.gigapro.web.controller;

import com.gigaspaces.gigapro.web.model.RestError;
import com.gigaspaces.gigapro.web.model.ValidationRequest;
import com.gigaspaces.gigapro.web.model.ValidationResponse;
import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.service.ZippedConfigCreator;
import com.gigaspaces.gigapro.web.service.validation.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static com.gigaspaces.gigapro.web.model.XAPConfigScriptType.SHELL;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.*;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class XapConfigController {

    private static final Logger LOG = LoggerFactory.getLogger(XapConfigController.class);

    @Autowired
    private ValidationService validationService;

    @Autowired
    private ZippedConfigCreator zippedConfigCreator;

    @RequestMapping(value = "/generate", method = POST, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity generate(@RequestBody XapConfigOptions xapConfigOptions) throws IOException {
        // SHELL script type is set manually until another script types are implemented
        xapConfigOptions.setScriptType(SHELL);

        xapConfigOptions.validate();
        LOG.info("Generating script using options: " + xapConfigOptions);
        Path zippedConfig = zippedConfigCreator.createZippedConfig(xapConfigOptions);
        FileSystemResource body = new FileSystemResource(zippedConfig.toFile());
        return ResponseEntity.ok()
                .headers(getHttpHeaders(body))
                .contentLength(body.contentLength())
                .contentType(parseMediaType("application/zip"))
                .body(body);
    }

    @RequestMapping(value = "/validjavahome", method = POST, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity validJavaHome(@RequestBody ValidationRequest request) {
        LOG.info("Validating JAVA_HOME: " + request);
        ValidationResponse response = validationService.validate(request);
        LOG.info("Validation result: " + response);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleError(Exception exception) {
        String message = Optional.ofNullable(exception.getMessage()).orElse("Ooops! Something bad has happened!");
        String detailedMessage = exception.getCause() != null ? exception.getCause().toString() : exception.toString();
        RestError restError = new RestError(INTERNAL_SERVER_ERROR.value(), message, detailedMessage);
        LOG.error(message, exception);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).contentType(APPLICATION_JSON).body(restError);
    }

    private HttpHeaders getHttpHeaders(FileSystemResource fileSystemResource) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Content-Description", "File Transfer");
        headers.add("Content-Disposition", "attachment; filename=" + fileSystemResource.getFile().getName());
        headers.add("Content-Transfer-Encoding", "binary");
        return headers;
    }
}

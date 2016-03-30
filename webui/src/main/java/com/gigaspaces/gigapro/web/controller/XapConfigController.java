package com.gigaspaces.gigapro.web.controller;

import com.gigaspaces.gigapro.web.model.RestError;
import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.service.ZippedConfigCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
    private ZippedConfigCreator zippedConfigCreator;

    @RequestMapping(value = "/generate", method = POST, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity generate(@RequestBody XapConfigOptions xapConfigOptions) throws IOException {
        // SHELL script type is set manually until another script types are implemented
        xapConfigOptions.setScriptType(SHELL);

        LOG.info("Generating script using options: " + xapConfigOptions);
        Path zippedConfig = zippedConfigCreator.createZippedConfig(xapConfigOptions);
        FileSystemResource body = new FileSystemResource(zippedConfig.toFile());
        return ResponseEntity.ok()
                .headers(getHttpHeaders(body))
                .contentLength(body.contentLength())
                .contentType(parseMediaType("application/zip"))
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handleError(Exception exception) {
        String message = Optional.ofNullable(exception.getMessage()).orElse("Ooops! Something bad has happened!");
        String detailedMessage = Optional.ofNullable(exception.getCause()).orElse(exception).toString();
        HttpStatus httpStatus = INTERNAL_SERVER_ERROR;
        LOG.error(message, exception);

        RestError restError = new RestError(httpStatus.value(), message, detailedMessage);
        return ResponseEntity.status(httpStatus).contentType(APPLICATION_JSON).body(restError);
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

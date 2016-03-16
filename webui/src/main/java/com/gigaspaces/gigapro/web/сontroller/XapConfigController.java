package com.gigaspaces.gigapro.web.сontroller;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.service.ZippedConfigCreator;
import com.gigaspaces.gigapro.web.model.ValidationRequest;
import com.gigaspaces.gigapro.web.model.ValidationResponse;
import com.gigaspaces.gigapro.web.service.validation.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static com.gigaspaces.gigapro.web.model.XAPConfigScriptType.SHELL;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class XapConfigController {

    private static final Logger LOG = LoggerFactory.getLogger(XapConfigController.class);

    @Autowired
    private ValidationService validationService;

    @Autowired
    private ZippedConfigCreator zippedConfigCreator;

    @RequestMapping(value = "/generate", method = POST, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity generate(@RequestBody XapConfigOptions xapConfigOptions) throws IOException, URISyntaxException {
        xapConfigOptions.setScriptType(SHELL);  // SHELL script type is set manually until another script types are implemented

        LOG.info("Generating script using options: " + xapConfigOptions);
        Path zippedConfig = zippedConfigCreator.createZippedConfig(xapConfigOptions);
        FileSystemResource body = new FileSystemResource(zippedConfig.toFile());
        return ResponseEntity.ok()
                .headers(getHttpHeaders(body))
                .contentLength(body.contentLength())
                .body(body);
    }

    @RequestMapping(value = "/validjavahome", method = POST, consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity validJavaHome(@RequestBody ValidationRequest request) throws IOException {
        LOG.info("Validating JAVA_HOME: " + request);
        ValidationResponse response = validationService.validate(request);
        LOG.info("Validation result: " + response);
        return ResponseEntity.ok(response);
    }

    private HttpHeaders getHttpHeaders(FileSystemResource fileSystemResource) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Content-Description", "File Transfer");
        headers.add("Content-Type", "application/zip");
        headers.add("Content-Disposition", "attachment; filename=" + fileSystemResource.getFile().getName());
        headers.add("Content-Transfer-Encoding", "binary");
        return headers;
    }
}

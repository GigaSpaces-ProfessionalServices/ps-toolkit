package com.gigaspaces.gigapro.web.сontroller;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.validation.model.ValidationRequest;
import com.gigaspaces.gigapro.web.validation.model.ValidationResponse;
import com.gigaspaces.gigapro.web.validation.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class XapConfigController {

    private static final Logger LOG = LoggerFactory.getLogger(XapConfigController.class);

    @Autowired
    private ValidationService validationService;

    @RequestMapping(value = "/generate", method = POST)
    public void generate(@RequestBody XapConfigOptions xapConfigOptions) {
        LOG.info("Generating script using options: \n" + xapConfigOptions);
    }

    @RequestMapping(value = "/validjavahome", method = POST)
    public ResponseEntity validJavaHome(@RequestBody ValidationRequest request) throws IOException {
        LOG.info("Validating JAVA_HOME: " + request);
        ValidationResponse response = validationService.validate(request);
        LOG.info("Validation result: " + response);
        return ResponseEntity.ok(response);
    }
}

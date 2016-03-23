package com.gigaspaces.gigapro.web.controller;

import com.gigaspaces.gigapro.web.Application;
import com.gigaspaces.gigapro.web.model.RestError;
import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import static com.gigaspaces.gigapro.web.XAPTestOptions.getNamedZoneOptions;
import static com.gigaspaces.gigapro.web.model.XAPConfigScriptType.SHELL;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest("server.port:9999")
@ActiveProfiles("test")
public class XapConfigControllerTest {

    RestTemplate template = new TestRestTemplate();

    @Test
    public void generateIsReachableTest() {
        XapConfigOptions options = getNamedZoneOptions();
        options.setScriptType(SHELL);
        ResponseEntity<InputStreamResource> responseEntity = template.postForEntity("http://localhost:9999/generate", options, InputStreamResource.class);

        assertThat(responseEntity.getStatusCode(), is(OK));
    }

    @Test
    public void generateHeadersTest() {
        XapConfigOptions options = getNamedZoneOptions();
        options.setScriptType(SHELL);
        ResponseEntity<InputStreamResource> responseEntity = template.postForEntity("http://localhost:9999/generate", options, InputStreamResource.class);

        assertThat(responseEntity.getHeaders().toSingleValueMap(), allOf(
                hasEntry(equalTo("Content-Length"), greaterThan("0")),
                hasEntry(equalTo("Content-Type"), containsString("application/zip")),
                hasEntry("Cache-Control", "no-cache, no-store, must-revalidate"),
                hasEntry("Pragma", "no-cache"),
                hasEntry("Expires", "0"),
                hasEntry("Content-Description", "File Transfer"))
        );
    }

    @Test
    public void exceptionHandlerTest() {
        ResponseEntity<RestError> responseEntity = template.postForEntity("http://localhost:9999/generate", new XapConfigOptions(), RestError.class);
        String errorMessage = "javaHome cannot be null or empty!<br/>" +
                "xapHome cannot be null or empty!<br/>" +
                "lookupGroups cannot be null or empty!<br/>" +
                "maxProcessesNumber cannot be null!<br/>" +
                "maxOpenFileDescriptorsNumber cannot be null!<br/>" +
                "zoneOptions cannot be null or empty!<br/>";

        String detailedMessage = "javax.validation.ValidationException: " + errorMessage;

        assertThat(responseEntity.getBody().getStatusCode(), is(500));
        assertThat(responseEntity.getBody().getMessage(), is(errorMessage));
        assertThat(responseEntity.getBody().getDetailedMessage(), is(detailedMessage));
    }
}

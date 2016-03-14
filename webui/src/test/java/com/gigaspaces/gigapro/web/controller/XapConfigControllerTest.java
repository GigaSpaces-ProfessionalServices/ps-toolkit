package com.gigaspaces.gigapro.web.controller;

import com.gigaspaces.gigapro.web.Application;
import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.validation.model.ValidationRequest;
import com.gigaspaces.gigapro.web.validation.model.ValidationResponse;
import org.hamcrest.CoreMatchers;
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

import static java.lang.System.getenv;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assume.assumeTrue;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class})
@WebIntegrationTest("server.port:9999")
@ActiveProfiles("test")
public class XapConfigControllerTest {

    RestTemplate template = new TestRestTemplate();

    @Test
    public void validJavaHomeIsReachableTest() throws Exception {
        ResponseEntity<ValidationResponse> responseEntity = template.postForEntity("http://localhost:9999/validjavahome", new ValidationRequest(), ValidationResponse.class);

        assertThat(responseEntity.getStatusCode(), is(OK));
    }

    @Test
    public void validJavaHomeCorrectTest() throws Exception {
        String javaHome = getenv("JAVA_HOME");
        assumeTrue("Environment variable 'JAVA_HOME' is not set", isNotBlank(javaHome));

        ValidationRequest request = new ValidationRequest();
        request.setValue(javaHome);
        ResponseEntity<ValidationResponse> responseEntity = template.postForEntity("http://localhost:9999/validjavahome", request, ValidationResponse.class);

        assertThat(responseEntity.getBody().isValid(), is(true));
    }

    @Test
    public void validJavaHomeIncorrectTest() throws Exception {
        ResponseEntity<ValidationResponse> responseEntity = template.postForEntity("http://localhost:9999/validjavahome", new ValidationRequest(), ValidationResponse.class);

        assertThat(responseEntity.getBody().isValid(), is(false));
    }

    @Test
    public void generateIsReachableTest() throws Exception {
        ResponseEntity<InputStreamResource> responseEntity = template.postForEntity("http://localhost:9999/generate", new XapConfigOptions(), InputStreamResource.class);

        assertThat(responseEntity.getStatusCode(), is(OK));
    }

    @Test
    public void generateHeadersTest() throws Exception {
        ResponseEntity<InputStreamResource> responseEntity = template.postForEntity("http://localhost:9999/generate", new XapConfigOptions(), InputStreamResource.class);

        assertThat(responseEntity.getHeaders().toSingleValueMap(), CoreMatchers.allOf(
                hasEntry(equalTo("Content-Length"), greaterThan("0")),
                hasEntry(equalTo("Content-Type"), containsString("application/zip")),
                hasEntry("Cache-Control", "no-cache, no-store, must-revalidate"),
                hasEntry("Pragma", "no-cache"),
                hasEntry("Expires", "0"),
                hasEntry("Content-Description", "File Transfer"))
        );

    }
}

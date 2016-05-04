package com.gigaspaces.gigapro.script_creator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gigaspaces.gigapro.script_creator.Application;
import com.gigaspaces.gigapro.script_creator.model.Profile;
import com.gigaspaces.gigapro.script_creator.model.XapConfigOptions;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.gigaspaces.gigapro.script_creator.XAPTestOptions.getDefaultOptions;
import static com.gigaspaces.gigapro.script_creator.XAPTestOptions.getNamedZoneOptions;
import static com.gigaspaces.gigapro.script_creator.model.XAPConfigScriptType.SHELL;
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
    public void profilesTest() throws IOException {
        ResponseEntity<List> responseEntity = template.getForEntity("http://localhost:9999/profiles", List.class);
        List<HashMap> profiles = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        String json = objectMapper.writeValueAsString(profiles.get(0));
        Profile profile = objectMapper.readValue(json, Profile.class);


        assertThat(responseEntity.getStatusCode(), is(OK));
        assertThat(profiles.size(), is(1));
        assertThat(profile.getName(), is("default"));
        assertThat(profile.getOptions(), is(getDefaultOptions()));
    }
}

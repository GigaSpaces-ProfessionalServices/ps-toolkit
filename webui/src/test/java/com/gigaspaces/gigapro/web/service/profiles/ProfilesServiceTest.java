package com.gigaspaces.gigapro.web.service.profiles;

import com.gigaspaces.gigapro.web.Application;
import com.gigaspaces.gigapro.web.model.Profile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.gigaspaces.gigapro.web.XAPTestOptions.getDefaultOptions;
import static com.gigaspaces.gigapro.web.service.profiles.ProfilesService.UNIX_PATH_REPLACEMENT_PATTERN;
import static com.gigaspaces.gigapro.web.service.profiles.ProfilesService.WIN_PATH_REPLACEMENT_PATTERN;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.replacePattern;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@ActiveProfiles("test")
public class ProfilesServiceTest {

    @Autowired
    private ProfilesService profilesService;

    @Test
    public void pathReplacementPatternTest() {
        String applicationPath = "jar:file:/D:/path/to/file/app.jar!/";
        String winExpected = "D:/path/to/file/app.jar";
        String unixExpected = "/D:/path/to/file/app.jar";
        String winActual = replacePattern(applicationPath, WIN_PATH_REPLACEMENT_PATTERN, EMPTY);
        String unixActual = replacePattern(applicationPath, UNIX_PATH_REPLACEMENT_PATTERN, EMPTY);

        assertThat(winActual, is(winExpected));
        assertThat(unixActual, is(unixExpected));
    }

    @Test
    public void getProfilesTest() {
        List<Profile> profiles = profilesService.getProfiles();

        assertThat(profiles.size(), is(1));
        assertThat(profiles.get(0).getName(), is("default"));
        assertThat(profiles.get(0).getOptions(), is(getDefaultOptions()));
    }
}

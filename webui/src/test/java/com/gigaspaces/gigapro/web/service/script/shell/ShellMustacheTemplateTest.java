package com.gigaspaces.gigapro.web.service.script.shell;

import com.gigaspaces.gigapro.web.Application;
import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.github.mustachejava.Mustache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static com.gigaspaces.gigapro.web.XAPTestOptions.*;
import static org.apache.commons.lang3.StringUtils.wrap;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@ActiveProfiles("test")
public class ShellMustacheTemplateTest {

    private static XapConfigOptions optionsUnicastTrue = getOptionsUnicastTrue();
    private static XapConfigOptions optionsUnicastFalse = getOptionsUnicastFalse();
    private static XapConfigOptions optionsManyLocators = getOptionsManyLocators();

    @Resource(name = "shellMustache")
    private Mustache shellMustache;

    @Test
    public void unicastIsTrueTest() throws IOException {
        String result;
        try (Writer writer = new StringWriter()) {
            shellMustache.execute(writer, optionsUnicastTrue).flush();
            result = writer.toString();
        }

        assertThat(result, allOf(
                containsString("JAVA_HOME=" + wrap(optionsUnicastTrue.getJavaHome(), '"')),
                containsString("JSHOMEDIR=" + wrap(optionsUnicastTrue.getXapHome(), '"')),
                containsString("DISCOVERY_PORT=" + wrap(optionsUnicastTrue.getDiscoveryPort().toString(), '"')),
                containsString("LOOKUPGROUPS=" + wrap(optionsUnicastTrue.getLookupGroups(), '"')),
                containsString("LOOKUPLOCATORS=" + wrap(optionsUnicastTrue.getLookupLocators() + ":" + optionsUnicastTrue.getDiscoveryPort(), '"')),
                containsString("-Dcom.gs.multicast.enabled=" + !optionsUnicastTrue.getIsUnicast()),
                containsString("uname -u < " + optionsUnicastTrue.getMaxProcessesNumber()),
                containsString("uname -n < " + optionsUnicastTrue.getMaxOpenFileDescriptorsNumber())
        ));
    }

    @Test
    public void unicastIsFalseTest() throws IOException {
        String result;
        try (Writer writer = new StringWriter()) {
            shellMustache.execute(writer, optionsUnicastFalse).flush();
            result = writer.toString();
        }

        assertThat(result, allOf(
                containsString("JAVA_HOME=" + wrap(optionsUnicastFalse.getJavaHome(), '"')),
                containsString("JSHOMEDIR=" + wrap(optionsUnicastFalse.getXapHome(), '"')),
                containsString("DISCOVERY_PORT=" + wrap(optionsUnicastFalse.getDiscoveryPort().toString(), '"')),
                containsString("LOOKUPGROUPS=" + wrap(optionsUnicastFalse.getLookupGroups(), '"')),
                containsString("LOOKUPLOCATORS=" + wrap(optionsUnicastFalse.getLookupLocators() + ":" + optionsUnicastTrue.getDiscoveryPort(), '"')),
                containsString("-Dcom.gs.multicast.enabled=" + !optionsUnicastFalse.getIsUnicast()),
                containsString("uname -u < " + optionsUnicastFalse.getMaxProcessesNumber()),
                containsString("uname -n < " + optionsUnicastFalse.getMaxOpenFileDescriptorsNumber())
        ));
    }

    @Test
    public void manyLookupLocatorsTest() throws IOException {
        String result;
        try (Writer writer = new StringWriter()) {
            shellMustache.execute(writer, optionsManyLocators).flush();
            result = writer.toString();
        }

        assertThat(result, containsString("LOOKUPLOCATORS=" + wrap(optionsManyLocators.getLookupLocatorsWithPort(), '"')));
    }
}

package com.gigaspaces.gigapro.script_creator.service.script.shell;

import com.gigaspaces.gigapro.script_creator.Application;
import com.gigaspaces.gigapro.script_creator.model.XapConfigOptions;
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

import static com.gigaspaces.gigapro.script_creator.XAPTestOptions.*;
import static org.apache.commons.lang3.StringUtils.wrap;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@ActiveProfiles("test")
public class SetAppEnvShellMustacheTemplateTest {

    private static XapConfigOptions optionsUnicastTrue = getOptionsUnicastTrue();
    private static XapConfigOptions optionsUnicastFalse = getOptionsUnicastFalse();
    private static XapConfigOptions optionsManyLocators = getOptionsManyLocators();

    @Resource(name = "setAppEnvShellMustache")
    private Mustache setAppEnvShellMustache;

    @Test
    public void unicastIsTrueTest() throws IOException {
        String result;
        try (Writer writer = new StringWriter()) {
            setAppEnvShellMustache.execute(writer, optionsUnicastTrue).flush();
            result = writer.toString();
        }

        assertThat(result, allOf(
                containsString("JAVA_HOME=" + wrap(optionsUnicastTrue.getJavaHome(), '"')),
                containsString("JSHOMEDIR=" + wrap(optionsUnicastTrue.getXapHome(), '"')),
                containsString("DISCOVERY_PORT=" + wrap(optionsUnicastTrue.getDiscoveryPort().toString(), '"')),
                containsString("LOOKUPGROUPS=" + wrap(optionsUnicastTrue.getLookupGroups(), '"')),
                containsString("LOOKUPLOCATORS=" + wrap(optionsUnicastTrue.getLookupLocators() + ":" + optionsUnicastTrue.getDiscoveryPort(), '"')),
                containsString("-Dcom.gs.multicast.enabled=" + !optionsUnicastTrue.getIsUnicast()),
                containsString("$ulimitu -lt " + optionsUnicastTrue.getMaxProcessesNumber()),
                containsString("$ulimitn -lt " + optionsUnicastTrue.getMaxOpenFileDescriptorsNumber())
        ));
    }

    @Test
    public void unicastIsFalseTest() throws IOException {
        String result;
        try (Writer writer = new StringWriter()) {
            setAppEnvShellMustache.execute(writer, optionsUnicastFalse).flush();
            result = writer.toString();
        }

        assertThat(result, allOf(
                containsString("JAVA_HOME=" + wrap(optionsUnicastFalse.getJavaHome(), '"')),
                containsString("JSHOMEDIR=" + wrap(optionsUnicastFalse.getXapHome(), '"')),
                containsString("DISCOVERY_PORT=" + wrap("4174", '"')),
                containsString("LOOKUPGROUPS=" + wrap(optionsUnicastFalse.getLookupGroups(), '"')),
                containsString("LOOKUPLOCATORS=" + "\"\""),
                containsString("-Dcom.gs.multicast.enabled=" + !optionsUnicastFalse.getIsUnicast()),
                containsString("$ulimitu -lt " + optionsUnicastFalse.getMaxProcessesNumber()),
                containsString("$ulimitn -lt " + optionsUnicastFalse.getMaxOpenFileDescriptorsNumber())
        ));
    }

    @Test
    public void manyLookupLocatorsTest() throws IOException {
        String result;
        try (Writer writer = new StringWriter()) {
            setAppEnvShellMustache.execute(writer, optionsManyLocators).flush();
            result = writer.toString();
        }

        assertThat(result, containsString("LOOKUPLOCATORS=" + wrap(optionsManyLocators.getLookupLocatorsWithPort(), '"')));
    }
}

package com.gigaspaces.gigapro.web.service.script.shell;

import com.gigaspaces.gigapro.web.Application;
import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreator;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreatorFactory;
import com.github.mustachejava.Mustache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;

import static com.gigaspaces.gigapro.web.model.XAPConfigScriptType.SHELL;
import static com.gigaspaces.gigapro.web.service.script.shell.XAPTestOptions.getOptionsUnicastFalse;
import static com.gigaspaces.gigapro.web.service.script.shell.XAPTestOptions.getOptionsUnicastTrue;
import static java.nio.file.Files.readAllBytes;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@SuppressWarnings("Duplicates")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@ActiveProfiles("test")
public class XAPConfigShellScriptCreatorTest {

    @Autowired
    private XAPConfigScriptCreatorFactory scriptCreatorFactory;

    @Resource(name = "shellMustache")
    private Mustache shellMustache;

    private static XapConfigOptions optionsUnicastTrue = getOptionsUnicastTrue();
    private static XapConfigOptions optionsUnicastFalse = getOptionsUnicastFalse();

    @Test
    public void createShellScriptUnicastFalseTest() throws IOException {
        XAPConfigScriptCreator scriptCreator = scriptCreatorFactory.getXAPConfigScriptCreator(SHELL);

        Path script = scriptCreator.createScript(optionsUnicastFalse);
        String actual = new String(readAllBytes(script));

        String expected;
        try (Writer writer = new StringWriter()) {
            shellMustache.execute(writer, optionsUnicastFalse).flush();
            expected = writer.toString();
        }

        assertThat(actual, is(expected));
    }

    @Test
    public void createShellScriptUnicastTrueTest() throws IOException {
        XAPConfigScriptCreator scriptCreator = scriptCreatorFactory.getXAPConfigScriptCreator(SHELL);

        Path script = scriptCreator.createScript(optionsUnicastTrue);
        String actual = new String(readAllBytes(script));

        String expected;
        try (Writer writer = new StringWriter()) {
            shellMustache.execute(writer, optionsUnicastTrue).flush();
            expected = writer.toString();
        }

        assertThat(actual, is(expected));
    }
}

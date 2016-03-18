package com.gigaspaces.gigapro.web.service.script.shell;

import com.gigaspaces.gigapro.web.Application;
import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreator;
import com.gigaspaces.gigapro.web.service.script.XAPConfigScriptCreatorFactory;
import com.github.mustachejava.Mustache;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;

import static com.gigaspaces.gigapro.web.XAPTestOptions.*;
import static com.gigaspaces.gigapro.web.model.XAPConfigScriptType.SHELL;
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

    @Resource(name = "setAppEnvShellMustache")
    private Mustache setAppEnvShellMustache;

    @Resource(name = "startGridShellMustache")
    private Mustache startGridShellMustache;

    @Value("${app.scripts.static-scripts.path}")
    private String staticScriptsPath;

    @Value("${app.scripts.extension.shell}")
    private String fileExtension;

    @Value("${app.scripts.web-ui.name}")
    private String webuiScriptName;

    @Value("${app.scripts.cli.name}")
    private String cliScriptName;

    private static XapConfigOptions optionsUnicastTrue = getOptionsUnicastTrue();
    private static XapConfigOptions optionsUnicastFalse = getOptionsUnicastFalse();
    private static XapConfigOptions optionsUnnamedZone = getUnnamedZoneOptions();
    private static XapConfigOptions optionsNamedZone = getNamedZoneOptions();
    private static XapConfigOptions optionsManyZones = getManyZonesOptions();

    @Test
    public void createSetAppEnvScriptUnicastFalseTest() throws IOException {
        XAPConfigScriptCreator scriptCreator = scriptCreatorFactory.getXAPConfigScriptCreator(SHELL);

        Path script = scriptCreator.createSetAppEnvScript(optionsUnicastFalse);
        String actual = new String(readAllBytes(script));

        String expected;
        try (Writer writer = new StringWriter()) {
            setAppEnvShellMustache.execute(writer, optionsUnicastFalse).flush();
            expected = writer.toString();
        }

        assertThat(actual, is(expected));
    }

    @Test
    public void createSetAppEnvScriptUnicastTrueTest() throws IOException {
        XAPConfigScriptCreator scriptCreator = scriptCreatorFactory.getXAPConfigScriptCreator(SHELL);

        Path script = scriptCreator.createSetAppEnvScript(optionsUnicastTrue);
        String actual = new String(readAllBytes(script));

        String expected;
        try (Writer writer = new StringWriter()) {
            setAppEnvShellMustache.execute(writer, optionsUnicastTrue).flush();
            expected = writer.toString();
        }

        assertThat(actual, is(expected));
    }

    @Test
    public void createWebuiScriptTest() throws IOException {
        XAPConfigScriptCreator scriptCreator = scriptCreatorFactory.getXAPConfigScriptCreator(SHELL);

        Path script = scriptCreator.getWebuiScript();
        String actual = new String(readAllBytes(script));
        File file = new File(getClass().getClassLoader().getResource(staticScriptsPath + webuiScriptName + fileExtension).getFile());
        String expected = FileUtils.readFileToString(file);

        assertThat(actual, is(expected));
    }

    @Test
    public void createCliScriptTest() throws IOException {
        XAPConfigScriptCreator scriptCreator = scriptCreatorFactory.getXAPConfigScriptCreator(SHELL);

        Path script = scriptCreator.getCliScript();
        String actual = new String(readAllBytes(script));

        File file = new File(getClass().getClassLoader().getResource(staticScriptsPath + cliScriptName + fileExtension).getFile());
        String expected = FileUtils.readFileToString(file);

        assertThat(actual, is(expected));
    }

    @Test
    public void createStartGridUnnamedZoneScriptTest() throws IOException {
        XAPConfigScriptCreator scriptCreator = scriptCreatorFactory.getXAPConfigScriptCreator(SHELL);

        List<Path> scripts = scriptCreator.createStartGridScripts(optionsUnnamedZone);
        for (int i = 0; i < scripts.size(); i++) {
            Path script = scripts.get(i);
            String actual = new String(readAllBytes(script));

            String expected;
            try (Writer writer = new StringWriter()) {
                startGridShellMustache.execute(writer, optionsUnnamedZone.getZoneOptions().get(i)).flush();
                expected = writer.toString();
            }
            assertThat(actual, is(expected));
        }
    }

    @Test
    public void createStartGridNamedZoneScriptTest() throws IOException {
        XAPConfigScriptCreator scriptCreator = scriptCreatorFactory.getXAPConfigScriptCreator(SHELL);

        List<Path> scripts = scriptCreator.createStartGridScripts(optionsNamedZone);
        for (int i = 0; i < scripts.size(); i++) {
            Path script = scripts.get(i);
            String actual = new String(readAllBytes(script));

            String expected;
            try (Writer writer = new StringWriter()) {
                startGridShellMustache.execute(writer, optionsNamedZone.getZoneOptions().get(i)).flush();
                expected = writer.toString();
            }
            assertThat(actual, is(expected));
        }
    }

    @Test
    public void createStartGridManyZonesScriptTest() throws IOException {
        XAPConfigScriptCreator scriptCreator = scriptCreatorFactory.getXAPConfigScriptCreator(SHELL);

        List<Path> scripts = scriptCreator.createStartGridScripts(optionsManyZones);
        for (int i = 0; i < scripts.size(); i++) {
            Path script = scripts.get(i);
            String actual = new String(readAllBytes(script));

            String expected;
            try (Writer writer = new StringWriter()) {
                startGridShellMustache.execute(writer, optionsManyZones.getZoneOptions().get(i)).flush();
                expected = writer.toString();
            }
            assertThat(actual, is(expected));
        }
    }
}
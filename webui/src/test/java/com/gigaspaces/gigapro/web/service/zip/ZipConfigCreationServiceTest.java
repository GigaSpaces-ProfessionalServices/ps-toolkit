package com.gigaspaces.gigapro.web.service.zip;

import com.gigaspaces.gigapro.web.Application;
import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.service.ZippedConfigCreator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.gigaspaces.gigapro.web.XAPTestOptions.getManyZonesOptions;
import static com.gigaspaces.gigapro.web.XAPTestOptions.getNamedZone;
import static com.gigaspaces.gigapro.web.model.XAPConfigScriptType.SHELL;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@ActiveProfiles("test")
public class ZipConfigCreationServiceTest {
    private static boolean setUpIsDone = false;

    @Value("${app.scripts.extension.shell}")
    private String sh;

    @Value("${app.scripts.set-app-env.name}")
    private String setAppEnvScriptName;

    @Value("${app.scripts.web-ui.name}")
    private String webuiScriptName;

    @Value("${app.scripts.cli.name}")
    private String cliScriptName;

    @Value("${app.scripts.start-grid.name}")
    private String startGridScriptName;

    @Autowired
    private ZippedConfigCreator configCreationService;

    private static Path zippedConfig;

    @Before
    public void setUp() {
        if (setUpIsDone) {
            return;
        }
        XapConfigOptions xapConfigOptions = getManyZonesOptions();
        xapConfigOptions.setScriptType(SHELL);

        zippedConfig = configCreationService.createZippedConfig(xapConfigOptions);

        setUpIsDone = true;
    }

    @Test
    public void zippedConfigExistsTest() {
        assertThat(zippedConfig.toFile().exists(), is(true));
    }

    @Test
    public void zippedConfigEntriesCountTest() throws IOException {
        ZipFile zipFile = new ZipFile(zippedConfig.toString());
        long entriesCount = zipFile.size();

        assertThat(entriesCount, is(12L));
    }

    @Test
    public void zippedConfigEntriesNamesShellTest() throws IOException {
        ZipFile zipFile = new ZipFile(zippedConfig.toString());

        assertThat(zipFile.stream().map(ZipEntry::getName).collect(toList()), hasItems("config/", "lib/", "local/", "logs/", "work/", "deploy/", "grid/",
                setAppEnvScriptName + sh,
                webuiScriptName + sh,
                cliScriptName + sh,
                "grid/" + "start-" + getNamedZone().getZoneName() + "-services" + sh,
                "grid/" + startGridScriptName + sh));
    }
}

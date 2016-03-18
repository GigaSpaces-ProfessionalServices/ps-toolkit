package com.gigaspaces.gigapro.web.service.script.shell;

import com.gigaspaces.gigapro.web.Application;
import com.gigaspaces.gigapro.web.model.ZoneConfig;
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

import static com.gigaspaces.gigapro.web.XAPTestOptions.getNamedZone;
import static com.gigaspaces.gigapro.web.XAPTestOptions.getUnnamedZone;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.wrap;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@ActiveProfiles("test")
public class StartGridShellMustacheTemplateTest {

    @Resource(name = "startGridShellMustache")
    private Mustache startGridShellMustache;

    private static ZoneConfig unnamedZone = getUnnamedZone();
    private static ZoneConfig namedZone = getNamedZone();

    @Test
    public void unnamedZoneTest() throws IOException {
        String result;
        try (Writer writer = new StringWriter()) {
            startGridShellMustache.execute(writer, unnamedZone).flush();
            result = writer.toString();
        }

        assertThat(result, allOf(
                containsString("OTHER_GSC_OPTIONS=" + wrap(ofNullable(unnamedZone.getOtherOptions()).orElse(EMPTY), '"')),
                not(containsString("-Dcom.gs.zones=")),
                not(containsString("${GSA_JAVA_OPTIONS} ${ZONES}")),
                containsString("GSA_JAVA_OPTIONS=\"${GSA_JAVA_OPTIONS}\""),
                containsString("-Xmx" + unnamedZone.getXmx() + " -Xms" + unnamedZone.getXms() + " -Xmn" + unnamedZone.getXmn()),
                containsString("gsa.gsc " + unnamedZone.getGscNum() + " gsa.gsm " + unnamedZone.getGsmNum() + " gsa.lus " + unnamedZone.getLusNum())
        ));
    }

    @Test
    public void namedZoneTest() throws IOException {
        String result;
        try (Writer writer = new StringWriter()) {
            startGridShellMustache.execute(writer, namedZone).flush();
            result = writer.toString();
        }

        assertThat(result, allOf(
                containsString("OTHER_GSC_OPTIONS=" + wrap(ofNullable(namedZone.getOtherOptions()).orElse(EMPTY), '"')),
                containsString("-Dcom.gs.zones=" + namedZone.getZoneName()),
                containsString("${GSA_JAVA_OPTIONS} ${ZONES}"),
                containsString("GSA_JAVA_OPTIONS=\"${GSA_JAVA_OPTIONS} ${ZONES}\""),
                containsString("-Xmx" + namedZone.getXmx() + " -Xms" + namedZone.getXms() + " -Xmn" + namedZone.getXmn()),
                containsString("gsa.gsc " + namedZone.getGscNum() + " gsa.gsm " + namedZone.getGsmNum() + " gsa.lus " + namedZone.getLusNum())
        ));
    }
}

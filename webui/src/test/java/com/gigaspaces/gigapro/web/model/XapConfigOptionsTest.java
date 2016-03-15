package com.gigaspaces.gigapro.web.model;

import org.junit.Test;

import java.util.List;

import static com.gigaspaces.gigapro.web.XAPTestOptions.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class XapConfigOptionsTest {

    @Test
    public void getLookupLocatorsAsListNullTest() {
        XapConfigOptions options = new XapConfigOptions();

        assertThat(emptyList(), is(options.getLookupLocatorsAsList()));
    }

    @Test
    public void getLookupLocatorsAsListEmptyTest() {
        XapConfigOptions options = new XapConfigOptions();
        options.setLookupLocators(EMPTY);

        assertThat(options.getLookupLocatorsAsList(), is(emptyList()));
    }

    @Test
    public void getLookupLocatorsAsListOneItemTest() {
        XapConfigOptions options = new XapConfigOptions();
        options.setLookupLocators(LOOKUP_LOCATORS);

        assertThat(options.getLookupLocatorsAsList(), is(asList(LOOKUP_LOCATORS)));
    }

    @Test
    public void getLookupLocatorsAsListManyItemsTest() {
        XapConfigOptions options = new XapConfigOptions();
        options.setLookupLocators(LOOKUP_LOCATORS_MANY);

        List<String> lookupLocatorsList = asList(split(LOOKUP_LOCATORS_MANY, ",")).stream().map(String::trim).collect(toList());
        assertThat(options.getLookupLocatorsAsList(), is(lookupLocatorsList));
    }

    @Test
    public void getLookupLocatorsWithPortEmptyTest() {
        XapConfigOptions options = new XapConfigOptions();
        options.setLookupLocators(EMPTY);

        assertThat(options.getLookupLocatorsWithPort(), is(EMPTY));
    }

    @Test
    public void getLookupLocatorsWithPortOneItemTest() {
        XapConfigOptions options = new XapConfigOptions();
        options.setLookupLocators(LOOKUP_LOCATORS);
        options.setDiscoveryPort(DISCOVERY_PORT);

        assertThat(options.getLookupLocatorsWithPort(), is(LOOKUP_LOCATORS + ":" + DISCOVERY_PORT));
    }

    @Test
    public void getLookupLocatorsWithPortManyItemsTest() {
        XapConfigOptions options = new XapConfigOptions();
        options.setLookupLocators(LOOKUP_LOCATORS_MANY);
        options.setDiscoveryPort(DISCOVERY_PORT);

        String lookupLocatorsWithPort = join(asList(split(LOOKUP_LOCATORS_MANY, ",")).stream().map((s) -> s.trim() + ":" + DISCOVERY_PORT).collect(toList()), ",");
        assertThat(options.getLookupLocatorsWithPort(), is(lookupLocatorsWithPort));
    }
}

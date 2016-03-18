package com.gigaspaces.gigapro.web;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;
import com.gigaspaces.gigapro.web.model.ZoneConfig;

import static java.util.Arrays.asList;

@SuppressWarnings("Duplicates")
public class XAPTestOptions {
    public static final String JAVA_HOME = "c:\\Program Files\\Java\\jdk1.8.0_45\\";
    public static final String XAP_HOME = "c:\\xap\\xap-10.1";
    public static final int DISCOVERY_PORT = 1234;
    public static final String LOOKUP_GROUPS = "group";
    public static final int MAX_OPEN_FILE_DESCRIPTORS_NUMBER = 1000;
    public static final int MAX_PROCESSES_NUMBER = 20000;
    public static final String LOOKUP_LOCATORS = "10.9.1.20";
    public static final String LOOKUP_LOCATORS_MANY = "10.9.1.20, 10.9.1.21, 10.9.1.22";
    public static final String XMX = "1g";
    public static final String XMS = "1g";
    public static final String XMN = "330m";
    public static final int GSC_NUM = 1;
    public static final int GSM_NUM = 1;
    public static final int LUS_NUM = 1;
    public static final String ZONE_NAME = "zone";
    public static final String OTHER_OPTIONS = "-Doption=value";

    private static XapConfigOptions optionsUnicastTrue;
    private static XapConfigOptions optionsUnicastFalse;
    private static XapConfigOptions optionsManyLocators;
    private static XapConfigOptions namedZoneOptions;
    private static XapConfigOptions unnamedZoneOptions;
    private static XapConfigOptions manyZonesOptions;
    private static ZoneConfig namedZone;
    private static ZoneConfig unnamedZone;

    public static XapConfigOptions getOptionsUnicastTrue() {
        if (optionsUnicastTrue == null) {
            optionsUnicastTrue = new XapConfigOptions();
            optionsUnicastTrue.setIsUnicast(true);
            optionsUnicastTrue.setJavaHome(JAVA_HOME);
            optionsUnicastTrue.setXapHome(XAP_HOME);
            optionsUnicastTrue.setDiscoveryPort(DISCOVERY_PORT);
            optionsUnicastTrue.setLookupGroups(LOOKUP_GROUPS);
            optionsUnicastTrue.setMaxOpenFileDescriptorsNumber(MAX_OPEN_FILE_DESCRIPTORS_NUMBER);
            optionsUnicastTrue.setMaxProcessesNumber(MAX_PROCESSES_NUMBER);
            optionsUnicastTrue.setLookupLocators(LOOKUP_LOCATORS);
        }
        return optionsUnicastTrue;
    }

    public static XapConfigOptions getOptionsUnicastFalse() {
        if (optionsUnicastFalse == null) {
            optionsUnicastFalse = new XapConfigOptions();
            optionsUnicastFalse.setIsUnicast(false);
            optionsUnicastFalse.setJavaHome(JAVA_HOME);
            optionsUnicastFalse.setXapHome(XAP_HOME);
            optionsUnicastFalse.setDiscoveryPort(DISCOVERY_PORT);
            optionsUnicastFalse.setLookupGroups(LOOKUP_GROUPS);
            optionsUnicastFalse.setMaxOpenFileDescriptorsNumber(MAX_OPEN_FILE_DESCRIPTORS_NUMBER);
            optionsUnicastFalse.setMaxProcessesNumber(MAX_PROCESSES_NUMBER);
            optionsUnicastFalse.setLookupLocators(LOOKUP_LOCATORS);
        }
        return optionsUnicastFalse;
    }

    public static XapConfigOptions getOptionsManyLocators() {
        if (optionsManyLocators == null) {
            optionsManyLocators = new XapConfigOptions();
            optionsManyLocators.setIsUnicast(true);
            optionsManyLocators.setJavaHome(JAVA_HOME);
            optionsManyLocators.setXapHome(XAP_HOME);
            optionsManyLocators.setDiscoveryPort(DISCOVERY_PORT);
            optionsManyLocators.setLookupGroups(LOOKUP_GROUPS);
            optionsManyLocators.setMaxOpenFileDescriptorsNumber(MAX_OPEN_FILE_DESCRIPTORS_NUMBER);
            optionsManyLocators.setMaxProcessesNumber(MAX_PROCESSES_NUMBER);
            optionsManyLocators.setLookupLocators(LOOKUP_LOCATORS_MANY);
        }
        return optionsManyLocators;
    }

    public static ZoneConfig getUnnamedZone() {
        if (unnamedZone == null) {
            unnamedZone = new ZoneConfig();
            unnamedZone.setXmx(XMX);
            unnamedZone.setXms(XMS);
            unnamedZone.setXmn(XMN);
            unnamedZone.setGscNum(GSC_NUM);
            unnamedZone.setGsmNum(GSM_NUM);
            unnamedZone.setLusNum(LUS_NUM);
        }
        return unnamedZone;
    }

    public static ZoneConfig getNamedZone() {
        if (namedZone == null) {
            namedZone = new ZoneConfig();
            namedZone.setZoneName(ZONE_NAME);
            namedZone.setXmx(XMX);
            namedZone.setXms(XMS);
            namedZone.setXmn(XMN);
            namedZone.setGscNum(GSC_NUM);
            namedZone.setGsmNum(GSM_NUM);
            namedZone.setLusNum(LUS_NUM);
            namedZone.setOtherOptions(OTHER_OPTIONS);
        }
        return namedZone;
    }

    public static XapConfigOptions getUnnamedZoneOptions() {
        if (unnamedZoneOptions == null) {
            unnamedZoneOptions = getOptionsUnicastTrue();
            unnamedZoneOptions.setZoneOptions(asList(getUnnamedZone()));
        }
        return unnamedZoneOptions;
    }

    public static XapConfigOptions getNamedZoneOptions() {
        if (namedZoneOptions == null) {
            namedZoneOptions = getOptionsUnicastTrue();
            namedZoneOptions.setZoneOptions(asList(getNamedZone()));
        }
        return namedZoneOptions;
    }

    public static XapConfigOptions getManyZonesOptions() {
        if (manyZonesOptions == null) {
            manyZonesOptions = getOptionsUnicastTrue();
            manyZonesOptions.setZoneOptions(asList(getNamedZone(), getUnnamedZone()));
        }
        return manyZonesOptions;
    }
}

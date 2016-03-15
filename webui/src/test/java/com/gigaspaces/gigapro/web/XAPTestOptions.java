package com.gigaspaces.gigapro.web;

import com.gigaspaces.gigapro.web.model.XapConfigOptions;

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

    private static XapConfigOptions optionsUnicastTrue;
    private static XapConfigOptions optionsUnicastFalse;

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
        if (optionsUnicastFalse == null) {
            optionsUnicastFalse = new XapConfigOptions();
            optionsUnicastFalse.setIsUnicast(false);
            optionsUnicastFalse.setJavaHome(JAVA_HOME);
            optionsUnicastFalse.setXapHome(XAP_HOME);
            optionsUnicastFalse.setDiscoveryPort(DISCOVERY_PORT);
            optionsUnicastFalse.setLookupGroups(LOOKUP_GROUPS);
            optionsUnicastFalse.setMaxOpenFileDescriptorsNumber(MAX_OPEN_FILE_DESCRIPTORS_NUMBER);
            optionsUnicastFalse.setMaxProcessesNumber(MAX_PROCESSES_NUMBER);
            optionsUnicastFalse.setLookupLocators(LOOKUP_LOCATORS_MANY);
        }
        return optionsUnicastFalse;
    }
}

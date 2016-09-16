package com.gigaspaces.gigapro.model;

import com.gigaspaces.gigapro.convert.property.PropertyKey;

import java.util.*;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class GridInfo {
    @PropertyKey("host_addresses")
    private Set<String> ipAddresses = new HashSet<>();
    @PropertyKey("space_names")
    private Set<String> spaces = new HashSet<>();
    @PropertyKey("secured_spaces")
    private Set<String> securedSpaces = new HashSet<>();

    @PropertyKey("lookup_groups")
    private Set<String> lookupGroups = new HashSet<>();
    @PropertyKey("lookup_locators")
    private Set<String> lookupLocators = new HashSet<>();

    @PropertyKey("lus_host_addresses")
    private Map<String, Integer> lusAddresses = new HashMap<>();
    @PropertyKey("gsm_host_addresses")
    private Map<String, Integer> gsmAddresses = new HashMap<>();
    @PropertyKey("gsc_host_addresses")
    private Map<String, Integer> gscAddresses = new HashMap<>();

    public Set<String> getIpAddresses() {
        return ipAddresses;
    }

    public void setIpAddresses(Set<String> ipAddresses) {
        this.ipAddresses = ipAddresses;
    }

    public Set<String> getSpaces() {
        return spaces;
    }

    public void setSpaces(Set<String> spaces) {
        this.spaces = spaces;
    }

    public Set<String> getLookupGroups() {
        return lookupGroups;
    }

    public void setLookupGroups(Set<String> lookupGroups) {
        this.lookupGroups = lookupGroups;
    }

    public Map<String, Integer> getLusAddresses() {
        return lusAddresses;
    }

    public void setLusAddresses(Map<String, Integer> lusAddresses) {
        this.lusAddresses = lusAddresses;
    }

    public Map<String, Integer> getGsmAddresses() {
        return gsmAddresses;
    }

    public void setGsmAddresses(Map<String, Integer> gsmAddresses) {
        this.gsmAddresses = gsmAddresses;
    }

    public Map<String, Integer> getGscAddresses() {
        return gscAddresses;
    }

    public void setGscAddresses(Map<String, Integer> gscAddresses) {
        this.gscAddresses = gscAddresses;
    }

    public Set<String> getLookupLocators() {
        return lookupLocators;
    }

    public void setLookupLocators(Set<String> lookupLocators) {
        this.lookupLocators = lookupLocators;
    }

    public Set<String> getSecuredSpaces() {
        return securedSpaces;
    }

    public void setSecuredSpaces(Set<String> securedSpaces) {
        this.securedSpaces = securedSpaces;
    }
}

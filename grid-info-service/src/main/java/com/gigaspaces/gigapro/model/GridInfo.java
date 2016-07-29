package com.gigaspaces.gigapro.model;

import com.gigaspaces.gigapro.convert.PropertyKey;

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
    private Set<String> lusAddresses = new HashSet<>();
    @PropertyKey("gsm_host_addresses")
    private Set<String> gsmAddresses = new HashSet<>();
    @PropertyKey("gsc_host_addresses")
    private Map<String, Integer> gscAddresses = new HashMap<>();

    private transient Map<String, ClusterReplicationPolicy> replPolicyMap = new HashMap<>();

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

    public Set<String> getLusAddresses() {
        return lusAddresses;
    }

    public void setLusAddresses(Set<String> lusAddresses) {
        this.lusAddresses = lusAddresses;
    }

    public Set<String> getGsmAddresses() {
        return gsmAddresses;
    }

    public void setGsmAddresses(Set<String> gsmAddresses) {
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

    public Map<String, ClusterReplicationPolicy> getReplPolicyMap() {
        return replPolicyMap;
    }

    public void setReplPolicyMap(Map<String, ClusterReplicationPolicy> replPolicyMap) {
        this.replPolicyMap = replPolicyMap;
    }
}

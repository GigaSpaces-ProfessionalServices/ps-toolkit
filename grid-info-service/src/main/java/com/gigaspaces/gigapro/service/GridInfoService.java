package com.gigaspaces.gigapro.service;

import com.gigaspaces.gigapro.model.ClusterReplicationPolicy;
import com.gigaspaces.gigapro.model.HostInfo;
import com.gigaspaces.gigapro.model.GridInfo;

import java.util.Map;
import java.util.Set;

/**
 * @author Svitlana_Pogrebna
 *
 */
public interface GridInfoService extends AutoCloseable {

    GridInfo collectGridInfo();
    
    Map<String, ClusterReplicationPolicy> collectClusterReplicationPolicyInfo();
    
    Set<HostInfo> collectCpuUsageInfo();
}

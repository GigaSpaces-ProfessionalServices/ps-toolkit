package com.gigaspaces.gigapro;

import com.gigaspaces.gigapro.convert.property.PropertiesFormatConverter;
import com.gigaspaces.gigapro.model.ClusterReplicationPolicy;
import com.gigaspaces.gigapro.model.GridInfo;
import com.gigaspaces.gigapro.model.HostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Svitlana_Pogrebna
 *
 */
public final class LoggingHelper {

    private LoggingHelper() {
    }

    private static final char NEWLINE_SEPARATOR = '\n';
    private static final String GRID_SUMMARY        = " ----------- GRID SUMMARY -----------";
    private static final String REPLICATION_SUMMARY = " ------- REPLICATION SUMMARY --------";
    private static final String HOST_SUMMARY        = " ----------- HOST SUMMARY -----------";
    private static final String HOSTS_DELIMITER     = "-------------------------------------";
    
    private static final Logger OUTPUT_LOG = LoggerFactory.getLogger("output");
    
    public static void log(GridInfo gridInfo, Map<String, ClusterReplicationPolicy> clusterReplicationPolicyMap, Set<HostInfo> hostInfos) {
        StringBuilder builder = new StringBuilder();
        PropertiesFormatConverter converter = new PropertiesFormatConverter();

        builder.append(NEWLINE_SEPARATOR).append(GRID_SUMMARY).append(NEWLINE_SEPARATOR).append(converter.convert(gridInfo)).append(NEWLINE_SEPARATOR);

        if (!clusterReplicationPolicyMap.isEmpty()) {
            builder.append(REPLICATION_SUMMARY).append(NEWLINE_SEPARATOR);
        }
        for (Entry<String, ClusterReplicationPolicy> entry : clusterReplicationPolicyMap.entrySet()) {
            builder.append(converter.convert(entry.getKey(), entry.getValue())).append(NEWLINE_SEPARATOR);
        }
        builder.append(HOST_SUMMARY).append(NEWLINE_SEPARATOR);
        boolean first = true;
        for (HostInfo hostInfo : hostInfos) {
            if (!first) {
                builder.append(HOSTS_DELIMITER).append(NEWLINE_SEPARATOR);
            }
            builder.append(converter.convert(hostInfo));
            first = false;
        }
        String content = builder.toString();
        OUTPUT_LOG.info(content);
    }
}

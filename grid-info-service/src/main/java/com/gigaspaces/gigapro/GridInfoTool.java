package com.gigaspaces.gigapro;

import com.gigaspaces.gigapro.model.CpuUsageInfo;
import com.gigaspaces.gigapro.convert.Converter;
import com.gigaspaces.gigapro.convert.property.PropertiesFormatConverter;
import com.gigaspaces.gigapro.model.ClusterReplicationPolicy;
import com.gigaspaces.gigapro.model.GridInfo;
import com.gigaspaces.gigapro.parser.GridInfoOptionsParser;
import com.gigaspaces.gigapro.service.GridInfoService;
import com.gigaspaces.gigapro.service.GridInfoServiceImpl;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class GridInfoTool {
    
    private static final Logger OUTPUT_LOG = LoggerFactory.getLogger("output");
    private static final Logger LOG = LoggerFactory.getLogger(GridInfoTool.class);
    
    public static void main(String[] args) {
       try (GridInfoService gridInfoService = new GridInfoServiceImpl(GridInfoOptionsParser.parse(args))) {
            
            Converter converter = new PropertiesFormatConverter();
            GridInfo gridInfo = gridInfoService.collectGridInfo();
            
            StringBuilder builder = new StringBuilder();
            builder.append("\n ------- GRID SUMMARY --------\n").append(converter.convert(gridInfo)).append("\n");
            
            Map<String, ClusterReplicationPolicy> clusterReplicationPolicyMap = gridInfoService.collectClusterReplicationPolicyInfo();
            if (!clusterReplicationPolicyMap.isEmpty()) {
                builder.append(" ------- REPLICATION SUMMARY --------\n");
            }
            for (Entry<String, ClusterReplicationPolicy> entry : clusterReplicationPolicyMap.entrySet()) {
                builder.append(converter.convert(entry.getKey(), entry.getValue())).append("\n");
            }
            builder.append(" ------- CPU USAGE SUMMARY --------\n");
            for (CpuUsageInfo cpuUsageInfo : gridInfoService.collectCpuUsageInfo()) {
                builder.append(converter.convert(cpuUsageInfo)).append("\n");
            }
            
            OUTPUT_LOG.info(builder.toString());
        } catch (ParseException e) {
            LOG.error("Failed to parse arguments.", e);
        } catch (Exception e1) {
            LOG.error(e1.getMessage());
        }
    }
}

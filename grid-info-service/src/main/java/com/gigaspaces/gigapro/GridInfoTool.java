package com.gigaspaces.gigapro;

import com.gigaspaces.gigapro.convert.pdf.PdfGenerator;
import com.gigaspaces.gigapro.mail.EmailSender;
import com.gigaspaces.gigapro.model.ClusterReplicationPolicy;
import com.gigaspaces.gigapro.model.GridInfo;
import com.gigaspaces.gigapro.model.HostInfo;
import com.gigaspaces.gigapro.parser.GridInfoOptionsParser;
import com.gigaspaces.gigapro.parser.GridInfoOptionsParser.GridInfoOptions;
import com.gigaspaces.gigapro.service.GridInfoService;
import com.gigaspaces.gigapro.service.GridInfoServiceImpl;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class GridInfoTool {

    private static final Logger LOG = LoggerFactory.getLogger(GridInfoTool.class);

    public static void main(String[] args) {
        try {
            GridInfoOptions options = GridInfoOptionsParser.parse(args);
            try (GridInfoService gridInfoService = new GridInfoServiceImpl(options)) {

                GridInfo gridInfo = gridInfoService.collectGridInfo();
                Map<String, ClusterReplicationPolicy> clusterReplicationPolicyMap = gridInfoService.collectClusterReplicationPolicyInfo();
                Set<HostInfo> hostInfo = gridInfoService.collectCpuUsageInfo();

                PdfGenerator pdfGenerator = new PdfGenerator();
                pdfGenerator.generateReport(gridInfo, clusterReplicationPolicyMap, hostInfo);

                EmailSender.send(pdfGenerator.getFileLocation());

                LoggingHelper.log(gridInfo, clusterReplicationPolicyMap, hostInfo);
            } catch (Exception e) {
                LOG.error("Unexpected exception", e);
            }
        } catch (ParseException e) {
            LOG.error("Failed to parse arguments.", e);
        }
    }
}

package com.gigaspaces.gigapro.convert.pdf;

import be.quodlibet.boxable.*;
import com.gigaspaces.gigapro.model.ClusterReplicationPolicy;
import com.gigaspaces.gigapro.model.GridInfo;
import com.gigaspaces.gigapro.model.HostInfo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class PdfGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(PdfGenerator.class);

    private static final String FILENAME = "grid-info.pdf";
    private static final float MARGIN = 10;

    private static final float HOST_NAME_COL_WIDTH = 20f;
    private static final float HOST_ADDR_COL_WIDTH = 15f;
    private static final float OS_COL_WIDTH = 20f;
    private static final float CORES_COL_WIDTH = 5f;
    private static final float RAM_COL_WIDTH = 25f;
    private static final float LUS_COUNT_COL_WIDTH = 5f;
    private static final float GSM_COUNT_COL_WIDTH = 5f;
    private static final float GSC_COUNT_COL_WIDTH = 5f;
    
    private static final float TOTAL_KEY_COL_WIDTH = 30f;
    private static final float TOTAL_VALUE_COL_WIDTH = 20f;
    
    private static final float SPACE_KEY_COL_WIDTH = 30f;
    private static final float SPACE_VALUE_COL_WIDTH = 70f;

    private static final float HEADER_ROW_HEIGHT = 15f;
    private static final float ROW_HEIGHT = 10f;

    private static final String HOST_NAME_COL_NAME = "Host Name";
    private static final String HOST_ADDR_COL_NAME = "Host Address";
    private static final String OS_COL_NAME = "OS";
    private static final String CORES_COL_NAME = "CPUs";
    private static final String RAM_COL_NAME = "RAM[MB]";
    private static final String GSM_COUNT_COL_NAME = "GSMs";
    private static final String LUS_COUNT_COL_NAME = "LUSs";
    private static final String GSC_COUNT_COL_NAME = "GSCs";

    private static final String TOTAL_LOOKUPS_COL_NAME = "Total Lookup Services: ";
    private static final String TOTAL_CORES_COL_NAME = "Total Cores count: ";
    private static final String TOTAL_RAM_COL_NAME = "Total RAM[MB]: ";
    private static final String TOTAL_MACHINES_COL_NAME = "Total Machines: ";
    private static final String TOTAL_SPACES_COL_NAME = "Total Spaces: ";
    private static final String TOTAL_SEC_SPACES_COL_NAME = "Total Secured Spaces: ";

    private static final String SPACE_REPLICATION_MODE_COL_NAME = "Replication Mode: ";
    private static final String SPACE_POLICY_TYPE_COL_NAME = "Replication Policy Type: ";
    private static final String SPACE_REPL_FIND_TIMEOUT_COL_NAME = "Replication Find Timeout: ";
    private static final String SPACE_REPL_FULL_TAKE_COL_NAME = "Replication Full Take: ";
    private static final String SPACE_REPL_NOTIFY_TEMPLATE_COL_NAME = "Replication Notify Templates: ";
    private static final String SPACE_REPL_CHUNK_SIZE_COL_NAME = "Replication Chunk Size: ";
    private static final String SPACE_REPL_INTERVAL_MILLIS_COL_NAME = "Replication Interval Milliseconds: ";
    private static final String SPACE_ASYNC_CHANNEL_SHUTDOWN_TIMEOUT_COL_NAME = "Asynchronous Channel Shutdown Timeout: ";
    private static final String SPACE_ON_CONFLICTING_PACKETS_COL_NAME = "On Conflicting Packets: ";
    private static final String SPACE_THROTTLE_WHEN_INACTIVE_COL_NAME = "Throttle When Inactive: ";
    private static final String SPACE_MAX_THROTTLE_TP_WHEN_INACTIVE_COL_NAME = "Max Throttle TP When Inactive: ";
    private static final String SPACE_MIN_THROTTLE_TP_WHEN_ACTIVE_COL_NAME = "Min Throttle TP When Active: ";
    private static final String SPACE_MULTIPLE_OPERS_CHUNK_SIZE_COL_NAME = "Multiple Operations Chunk Size: ";
    private static final String SPACE_TARGET_CONSUME_TIMEOUT_COL_NAME = "Target Consume Timeout: ";
    private static final String SPACE_MIRROR_NAME_COL_NAME = "Mirror Name: ";
    
    private static final String DASH = "-";

    public void generateReport(GridInfo gridInfo, Map<String, ClusterReplicationPolicy> clusterReplicationPolicyMap, Set<HostInfo> hostInfoSet) {
        try (PDDocument doc = new PDDocument()) {
            PDRectangle rect = new PDRectangle(PDRectangle.A4.getHeight(), PDRectangle.A4.getWidth());
            PDPage page = new PDPage(rect);

            doc.addPage(page);

            float scale = 0.8f;
            PDImageXObject image = drawLogo(doc, page, scale);
            float imageHeight = image != null ? image.getHeight() * scale : 0;
            
            float yStartNewPage = rect.getHeight() - MARGIN;
            float yStart = yStartNewPage - imageHeight;
            
            drawCreationDate(doc, page, yStart);
            yStart -= 20;
                    
            float tableWidth = rect.getWidth() - 2 * MARGIN;
            BaseTable hostsTable = new BaseTable(yStart, yStartNewPage, MARGIN, tableWidth, MARGIN, doc, page, true, true);
            Totals totalsInfo = populateHostsTable(hostsTable, gridInfo, hostInfoSet);
            yStart = hostsTable.draw();

            float totalsTableWidth = rect.getWidth() / 2;
            BaseTable totalsTable = new BaseTable(yStart, yStartNewPage, MARGIN, totalsTableWidth, MARGIN, doc, hostsTable.getCurrentPage(), false, true);
            populateTotalsTable(totalsTable, gridInfo, totalsInfo);
            yStart = totalsTable.draw();
            
            BaseTable spacesTable = new BaseTable(yStart, yStartNewPage, MARGIN, tableWidth, MARGIN, doc, totalsTable.getCurrentPage(), false, true);
            populateSpacesTable(spacesTable, gridInfo, clusterReplicationPolicyMap);
            spacesTable.draw();

            doc.save(FILENAME);
        } catch (IOException e) {
            LOG.error("Failed to generate PDF document", e);
        }
    }

    private PDImageXObject drawLogo(PDDocument doc, PDPage page, float scale) throws IOException {
        PDImageXObject image = null;
        try (InputStream inStream = this.getClass().getResourceAsStream("/logo.png")) {
            BufferedImage bi = ImageIO.read(inStream);
            image = LosslessFactory.createFromImage(doc, bi);
            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page, AppendMode.APPEND, true)) {
                float imageHeight = image.getHeight() * scale;
                contentStream.drawImage(image, 0, page.getMediaBox().getHeight() - imageHeight, image.getWidth() * scale, imageHeight);
            }
        }
        return image;
    }

    private void drawCreationDate(PDDocument doc, PDPage page, float y) throws IOException {
        try (PDPageContentStream pdfContent = new PDPageContentStream(doc, page, AppendMode.APPEND, true)) {
            pdfContent.beginText();
            pdfContent.setFont(PDType1Font.HELVETICA, 9);
            pdfContent.newLineAtOffset(MARGIN, y);
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
            pdfContent.showText("Created on: " + df.format(new Date()));
            pdfContent.endText();
        }
    }

    private Totals populateHostsTable(BaseTable table, GridInfo gridInfo, Set<HostInfo> hostInfoSet) {
        Row<PDPage> headerRow = table.createRow(HEADER_ROW_HEIGHT);
        createHeaderCell(headerRow, HOST_NAME_COL_WIDTH, HOST_NAME_COL_NAME);
        createHeaderCell(headerRow, HOST_ADDR_COL_WIDTH, HOST_ADDR_COL_NAME);
        createHeaderCell(headerRow, OS_COL_WIDTH, OS_COL_NAME);
        createHeaderCell(headerRow, CORES_COL_WIDTH, CORES_COL_NAME);
        createHeaderCell(headerRow, RAM_COL_WIDTH, RAM_COL_NAME);
        createHeaderCell(headerRow, GSM_COUNT_COL_WIDTH, GSM_COUNT_COL_NAME);
        createHeaderCell(headerRow, LUS_COUNT_COL_WIDTH, LUS_COUNT_COL_NAME);
        createHeaderCell(headerRow, GSC_COUNT_COL_WIDTH, GSC_COUNT_COL_NAME);

        table.addHeaderRow(headerRow);

        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        Totals totals = new Totals();
        for (HostInfo hostInfo : hostInfoSet) {
            Row<PDPage> row = table.createRow(ROW_HEIGHT);
            createCell(row, hostInfo.getHostName());
            String hostAddr = hostInfo.getHostAddress();
            createCell(row, hostAddr);
            createCell(row, String.format("%s [v.%s, arch.%s]", hostInfo.getOsName(), hostInfo.getOsVersion(), hostInfo.getOsArch()));
            int cores = hostInfo.getAvailableProcessors();
            createCell(row, String.valueOf(cores));
            totals.coresNumber += cores;

            double ram = hostInfo.getPhysicalMemorySizeInMB();
            createCell(row, df.format(ram));
            totals.ram += ram;

            Integer gsmCount = gridInfo.getGsmAddresses().get(hostAddr);
            createCell(row, gsmCount == null ? DASH : String.valueOf(gsmCount));
            Integer lusCount = gridInfo.getLusAddresses().get(hostAddr);
            createCell(row, lusCount == null ? DASH : String.valueOf(lusCount));
            Integer gscCount = gridInfo.getGscAddresses().get(hostAddr);
            createCell(row, gscCount == null ? DASH : String.valueOf(gscCount));
        }
        return totals;
    }

    private static class Totals {
        private int coresNumber;
        private double ram;
    }

    private void populateTotalsTable(BaseTable table, GridInfo gridInfo, Totals totalsInfo) {
        Row<PDPage> row = table.createRow(ROW_HEIGHT);
        row.createCell(TOTAL_KEY_COL_WIDTH, TOTAL_LOOKUPS_COL_NAME);
        row.createCell(TOTAL_VALUE_COL_WIDTH, String.valueOf(gridInfo.getLusAddresses().size()));

        row = table.createRow(ROW_HEIGHT);
        row.createCell(TOTAL_KEY_COL_WIDTH, TOTAL_CORES_COL_NAME);
        row.createCell(TOTAL_VALUE_COL_WIDTH, String.valueOf(totalsInfo.coresNumber));

        row = table.createRow(ROW_HEIGHT);
        row.createCell(TOTAL_KEY_COL_WIDTH, TOTAL_RAM_COL_NAME);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        row.createCell(TOTAL_VALUE_COL_WIDTH, df.format(totalsInfo.ram));

        row = table.createRow(ROW_HEIGHT);
        row.createCell(TOTAL_KEY_COL_WIDTH, TOTAL_MACHINES_COL_NAME);
        row.createCell(TOTAL_VALUE_COL_WIDTH, String.valueOf(gridInfo.getIpAddresses().size()));
        
        row = table.createRow(ROW_HEIGHT);
        row.createCell(TOTAL_KEY_COL_WIDTH, TOTAL_SPACES_COL_NAME);
        row.createCell(TOTAL_VALUE_COL_WIDTH, String.valueOf(gridInfo.getSpaces().size()));
        
        row = table.createRow(ROW_HEIGHT);
        row.createCell(TOTAL_KEY_COL_WIDTH, TOTAL_SEC_SPACES_COL_NAME);
        row.createCell(TOTAL_VALUE_COL_WIDTH, String.valueOf(gridInfo.getSecuredSpaces().size()));
    }

    private void populateSpacesTable(BaseTable table, GridInfo gridInfo, Map<String, ClusterReplicationPolicy> replicationInfoMap) {
        for (Entry<String, ClusterReplicationPolicy> replicationInfo : replicationInfoMap.entrySet()) {
            Row<PDPage> row = table.createRow(ROW_HEIGHT);
            Cell<PDPage> headerCell = row.createCell(SPACE_KEY_COL_WIDTH, String.format("<%s> Summary: ", capitalize(replicationInfo.getKey())));
            headerCell.setFont(PDType1Font.HELVETICA_BOLD);
            headerCell.setFontSize(9f);
            row.createCell(SPACE_VALUE_COL_WIDTH, "");
            
            ClusterReplicationPolicy repPolicy = replicationInfo.getValue();
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_REPLICATION_MODE_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, repPolicy.getReplicationMode());
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_POLICY_TYPE_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, repPolicy.getPolicyType());
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_REPL_FIND_TIMEOUT_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, String.valueOf(repPolicy.getReplFindTimeout()));
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_REPL_FULL_TAKE_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, String.valueOf(repPolicy.getReplFullTake()));
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_REPL_NOTIFY_TEMPLATE_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, String.valueOf(repPolicy.isReplNotifyTemplate()));
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_REPL_CHUNK_SIZE_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, String.valueOf(repPolicy.getReplChunkSize()));
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_REPL_INTERVAL_MILLIS_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, String.valueOf(repPolicy.getReplIntervalMillis()));
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_ASYNC_CHANNEL_SHUTDOWN_TIMEOUT_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, String.valueOf(repPolicy.getAsyncChannelShutdownTimeout()));
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_ON_CONFLICTING_PACKETS_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, repPolicy.getOnConflictingPackates());
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_THROTTLE_WHEN_INACTIVE_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, String.valueOf(repPolicy.isThrottleWhenInactive()));
           
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_MAX_THROTTLE_TP_WHEN_INACTIVE_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, String.valueOf(repPolicy.getMaxThrottleTpWhenInactive()));
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_MIN_THROTTLE_TP_WHEN_ACTIVE_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, String.valueOf(repPolicy.getMinThrottleTpWhenActive()));
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_MULTIPLE_OPERS_CHUNK_SIZE_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, String.valueOf(repPolicy.getMultipleOpersChunkSize()));
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_TARGET_CONSUME_TIMEOUT_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, String.valueOf(repPolicy.getTargetConsumeTimeout()));
            
            row = table.createRow(ROW_HEIGHT);
            row.createCell(SPACE_KEY_COL_WIDTH, SPACE_MIRROR_NAME_COL_NAME);
            row.createCell(SPACE_VALUE_COL_WIDTH, repPolicy.getMirrorName());
        }
    }
    
    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
    
    private void createHeaderCell(Row<PDPage> headerRow, float width, String value) {
        Cell<PDPage> cell = headerRow.createCell(width, value, HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE);
        cell.setFont(PDType1Font.HELVETICA_BOLD);
        cell.setFontSize(9f);
    }

    private void createCell(Row<PDPage> row, String value) {
        row.createCell(value);
    }

    public String getFileLocation() {
        return FILENAME;
    }
}

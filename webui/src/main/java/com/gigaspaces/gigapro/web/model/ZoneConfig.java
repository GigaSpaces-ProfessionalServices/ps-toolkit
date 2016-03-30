package com.gigaspaces.gigapro.web.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "zoneName")
public class ZoneConfig {
    private String zoneName;
    private String xmx;
    private String xms;
    private String xmn;
    private String otherOptions;
    private Integer gscNum;
    private Integer gsmNum;
    private Integer lusNum;
}

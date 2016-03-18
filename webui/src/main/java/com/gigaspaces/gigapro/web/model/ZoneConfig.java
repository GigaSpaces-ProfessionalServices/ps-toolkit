package com.gigaspaces.gigapro.web.model;

import lombok.Data;

import javax.validation.ValidationException;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Data
public class ZoneConfig {
    private String zoneName;
    private String xmx;
    private String xms;
    private String xmn;
    private String otherOptions;
    private Integer gscNum;
    private Integer gsmNum;
    private Integer lusNum;

    public void validate() {
        String errorMessage = "";
        if (isBlank(xmx)) {
            errorMessage += "xmx cannot be null or empty!<br/>";
        }
        if (isBlank(xms)) {
            errorMessage += "xms cannot be null or empty!<br/>";
        }
        if (isBlank(xmn)) {
            errorMessage += "xmn cannot be null or empty!<br/>";
        }
        if (gscNum == null || gscNum < 0) {
            errorMessage += "gscNum cannot be null or less than 0!<br/>";
        }
        if (gsmNum == null || gsmNum < 0) {
            errorMessage += "gsmNum cannot be null or less than 0!<br/>";
        }
        if (lusNum == null || lusNum < 0) {
            errorMessage += "lusNum cannot be null or less than 0!<br/>";
        }
        if (isNotEmpty(errorMessage)) {
            throw new ValidationException(errorMessage);
        }
    }
}

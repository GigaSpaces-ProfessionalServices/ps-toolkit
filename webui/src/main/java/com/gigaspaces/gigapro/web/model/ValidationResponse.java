package com.gigaspaces.gigapro.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ValidationResponse {
    @JsonProperty("isValid")
    boolean isValid;
    String value;
}

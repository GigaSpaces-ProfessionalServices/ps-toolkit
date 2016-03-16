package com.gigaspaces.gigapro.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RestError {
    private Integer statusCode;
    private String message;
    private String detailedMessage;
}

package com.gigaspaces.gigapro.web.validation.service;

import com.gigaspaces.gigapro.web.validation.model.ValidationRequest;
import com.gigaspaces.gigapro.web.validation.model.ValidationResponse;

import java.io.IOException;

public interface ValidationService {

    ValidationResponse validate(ValidationRequest request) throws IOException;
}

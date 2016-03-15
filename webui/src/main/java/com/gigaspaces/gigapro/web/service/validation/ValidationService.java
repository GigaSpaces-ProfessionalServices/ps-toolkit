package com.gigaspaces.gigapro.web.service.validation;

import com.gigaspaces.gigapro.web.model.ValidationRequest;
import com.gigaspaces.gigapro.web.model.ValidationResponse;

import java.io.IOException;

public interface ValidationService {

    ValidationResponse validate(ValidationRequest request) throws IOException;
}

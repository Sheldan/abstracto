package dev.sheldan.abstracto.core.service;


import dev.sheldan.abstracto.core.models.JSONValidationResult;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface JSONValidationService {
    JSONValidationResult validateJSONSchema(InputStream schema, File jsonFile) throws IOException;
    JSONValidationResult validateJSONSchema(InputStream schema, InputStream jsonFile);
    JSONValidationResult validateJSONSchema(JSONObject schema, Object json) throws IOException;
    JSONValidationResult validateJSONSchema(String schema, String json);
    JSONValidationResult validateJSONSchema(File schema, File jsonFile) throws IOException;
    List<ValidationException> getDetailedException(List<ValidationException> exceptions);

    enum Result {
        SUCCESSFUL, ERRONEOUS
    }
}

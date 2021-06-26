package dev.sheldan.abstracto.core.service;

import dev.sheldan.abstracto.core.models.JSONValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JSONValidationServiceBean implements JSONValidationService {

    @Override
    public JSONValidationResult validateJSONSchema(InputStream schemaStream, File jsonFile) throws IOException {
        return validateJSONSchema(schemaStream, new FileInputStream(jsonFile));
    }

    @Override
    public JSONValidationResult validateJSONSchema(InputStream schema, InputStream jsonFile)  {
        JSONObject jsonSchemaStream = new JSONObject(new JSONTokener(schema));
        JSONTokener tokener = new JSONTokener(jsonFile);
        return validateJSONSchema(jsonSchemaStream, tokener.nextValue());
    }

    @Override
    public JSONValidationResult validateJSONSchema(JSONObject jsonSchema, Object json) {
        Schema schema = SchemaLoader.load(jsonSchema);
        JSONValidationResult jsonValidationResult = JSONValidationResult
                .builder()
                .result(Result.SUCCESSFUL)
                .build();
        try {
            schema.validate(json);
        } catch (ValidationException e) {
            jsonValidationResult.setResult(Result.ERRONEOUS);
            if(e.getCausingExceptions().isEmpty()) {
                jsonValidationResult.setExceptions(new ArrayList<>(Arrays.asList(e)));
            } else {
                jsonValidationResult.setExceptions(e.getCausingExceptions());
            }
        }
        return jsonValidationResult;
    }

    @Override
    public JSONValidationResult validateJSONSchema(String schema, String json) {
        return validateJSONSchema(new JSONObject(schema), new JSONTokener(json).nextValue());
    }

    @Override
    public JSONValidationResult validateJSONSchema(File schema, File jsonFile) throws IOException {
        try (InputStream schemaStream = new FileInputStream(schema); InputStream jsonInputStream = new FileInputStream(jsonFile)) {
            return validateJSONSchema(schemaStream, jsonInputStream);
        }
    }

    @Override
    public List<ValidationException> getDetailedException(List<ValidationException> exceptions) {
        return exceptions
                .stream()
                .map(this::getDeepestExceptions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private List<ValidationException> getDeepestExceptions(ValidationException validationException) {
        if(validationException.getCausingExceptions().isEmpty()) {
            return Arrays.asList(validationException);
        }

        return validationException
                .getCausingExceptions()
                .stream()
                .map(this::getDeepestExceptions)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}

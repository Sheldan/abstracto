package dev.sheldan.abstracto.webservices.urban.service;

import dev.sheldan.abstracto.webservices.urban.model.UrbanDefinition;

import java.io.IOException;

public interface UrbanService {
    UrbanDefinition getUrbanDefinition(String query) throws IOException;
}

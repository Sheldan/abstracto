package dev.sheldan.abstracto.webservices.wikipedia.service;

import dev.sheldan.abstracto.webservices.wikipedia.model.WikipediaArticleSummary;

import java.io.IOException;

public interface WikipediaService {
    WikipediaArticleSummary getSummary(String query, String language) throws IOException;
}

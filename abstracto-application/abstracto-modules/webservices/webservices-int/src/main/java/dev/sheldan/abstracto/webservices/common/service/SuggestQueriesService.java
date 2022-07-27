package dev.sheldan.abstracto.webservices.common.service;

import java.util.List;

public interface SuggestQueriesService {
    List<String> getSuggestionsForQuery(String query, String service);
    List<String> getYoutubeSuggestionsForQuery(String query);
}

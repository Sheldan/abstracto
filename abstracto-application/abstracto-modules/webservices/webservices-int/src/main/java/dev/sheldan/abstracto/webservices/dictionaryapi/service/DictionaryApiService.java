package dev.sheldan.abstracto.webservices.dictionaryapi.service;

import dev.sheldan.abstracto.webservices.dictionaryapi.model.WordMeaning;

import java.io.IOException;

public interface DictionaryApiService {
    WordMeaning getDefinitions(String query) throws IOException;
}

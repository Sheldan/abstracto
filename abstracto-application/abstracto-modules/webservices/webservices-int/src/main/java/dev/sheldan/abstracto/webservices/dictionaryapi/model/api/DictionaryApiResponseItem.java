package dev.sheldan.abstracto.webservices.dictionaryapi.model.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
@Setter
public class DictionaryApiResponseItem {
    private String word;
    private String origin;
    private List<DictionaryApiWordMeaning> meanings;
}

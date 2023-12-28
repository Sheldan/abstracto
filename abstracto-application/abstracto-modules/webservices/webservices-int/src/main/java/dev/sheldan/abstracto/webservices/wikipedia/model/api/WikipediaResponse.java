package dev.sheldan.abstracto.webservices.wikipedia.model.api;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WikipediaResponse {
    private WikipediaResponseQuery query;
}


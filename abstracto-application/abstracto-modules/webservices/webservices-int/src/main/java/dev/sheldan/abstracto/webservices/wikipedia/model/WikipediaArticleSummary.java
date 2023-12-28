package dev.sheldan.abstracto.webservices.wikipedia.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WikipediaArticleSummary {
    private String title;
    private String summary;
    private String fullURL;
}

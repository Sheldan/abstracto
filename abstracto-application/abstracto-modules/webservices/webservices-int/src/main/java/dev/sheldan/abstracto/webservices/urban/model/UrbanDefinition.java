package dev.sheldan.abstracto.webservices.urban.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class UrbanDefinition {
    private String definition;
    private String author;
    private String example;
    private Instant creationDate;
    private String url;
    private Long upvoteCount;
    private Long downVoteCount;
}

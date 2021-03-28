package dev.sheldan.abstracto.webservices.urban.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class UrbanResponseDefinition {
    private String definition;
    private String permalink;
    private Long thumbs_up;
    private Long thumbs_down;
    private String author;
    private String word;
    private Long defid;
    private Instant written_on;
    private String example;
}

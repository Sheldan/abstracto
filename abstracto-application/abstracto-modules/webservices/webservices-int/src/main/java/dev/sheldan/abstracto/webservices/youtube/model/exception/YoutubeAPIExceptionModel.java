package dev.sheldan.abstracto.webservices.youtube.model.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class YoutubeAPIExceptionModel implements Serializable {
    private Throwable exception;
}

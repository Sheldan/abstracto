package dev.sheldan.abstracto.webservices.common.model.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class SuggestQueriesExceptionModel implements Serializable {
    private Integer responseCode;
}

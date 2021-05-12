package dev.sheldan.abstracto.webservices.urban.model.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class UrbanDictionaryRequestExceptionModel implements Serializable {
    private Integer responseCode;
}

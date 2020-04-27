package dev.sheldan.abstracto.core;

import org.springframework.beans.factory.annotation.Value;

public class Constants {

    private Constants() {

    }

    @Value("${abstracto.parameter.lowerBound}")
    public static final int PARAMETER_LIMIT = 0;
}

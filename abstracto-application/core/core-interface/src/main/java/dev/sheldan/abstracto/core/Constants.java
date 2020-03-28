package dev.sheldan.abstracto.core;

import org.springframework.beans.factory.annotation.Value;

public class Constants {

    @Value("${abstracto.parameter.lowerBound}")
    public static int PARAMETER_LIMIT = 0;
}

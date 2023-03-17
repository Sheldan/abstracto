package dev.sheldan.abstracto.webservices.openweathermap.exception;

import dev.sheldan.abstracto.core.exception.AbstractoTemplatableException;

public class LocationNotFoundException extends AbstractoTemplatableException {
    @Override
    public String getTemplateName() {
        return "no_weather_location_found_exception";
    }

    @Override
    public Object getTemplateModel() {
        return new Object();
    }
}

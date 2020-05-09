package dev.sheldan.abstracto.templating.service;

import dev.sheldan.abstracto.templating.model.MessageToSend;

import java.util.HashMap;

public interface TemplateService {
    MessageToSend renderEmbedTemplate(String key, Object model);
    String renderTemplateWithMap(String key, HashMap<String, Object> parameters);
    String renderTemplate(String key, Object model);
}

package dev.sheldan.abstracto.templating.service;

import dev.sheldan.abstracto.templating.model.MessageToSend;
import dev.sheldan.abstracto.templating.model.database.Template;

import java.util.HashMap;

public interface TemplateService {
    String renderTemplate(Template template);
    MessageToSend renderEmbedTemplate(String key, Object model);
    String renderTemplate(String key, HashMap<String, Object> parameters);
    String renderTemplate(String key, Object model);
}

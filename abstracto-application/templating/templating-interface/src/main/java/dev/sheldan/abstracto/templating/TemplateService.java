package dev.sheldan.abstracto.templating;

import dev.sheldan.abstracto.core.models.ServerContext;
import dev.sheldan.abstracto.core.models.embed.MessageToSend;

import java.util.HashMap;

public interface TemplateService {
    TemplateDto getTemplateByKey(String key);
    boolean templateExists(String key);
    String renderTemplate(TemplateDto templateDto);
    MessageToSend renderEmbedTemplate(String key, Object model);
    String renderTemplate(String key, HashMap<String, Object> parameters);
    String renderTemplate(String key, Object model);
    String renderContextAwareTemplate(String key, ServerContext serverContext);
    void createTemplate(String key, String content);
}

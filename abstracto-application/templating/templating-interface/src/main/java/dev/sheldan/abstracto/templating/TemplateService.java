package dev.sheldan.abstracto.templating;

import dev.sheldan.abstracto.core.models.ServerContext;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.HashMap;

public interface TemplateService {
    TemplateDto getTemplateByKey(String key);
    boolean templateExists(String key);
    String renderTemplate(TemplateDto templateDto);
    MessageEmbed renderEmbedTemplate(String key, Object model);
    String renderTemplate(String key, HashMap<String, Object> parameters);
    String renderTemplate(String key, Object model);
    String renderContextAwareTemplate(String key, ServerContext serverContext);
    void createTemplate(String key, String content);
}

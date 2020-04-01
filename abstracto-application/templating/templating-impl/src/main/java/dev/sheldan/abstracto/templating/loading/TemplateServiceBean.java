package dev.sheldan.abstracto.templating.loading;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.models.ContextAware;
import dev.sheldan.abstracto.core.models.ServerContext;
import dev.sheldan.abstracto.core.models.embed.MessageToSend;
import dev.sheldan.abstracto.templating.TemplateDto;
import dev.sheldan.abstracto.templating.TemplateService;
import dev.sheldan.abstracto.templating.embed.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.util.HashMap;

@Slf4j
@Component
public class TemplateServiceBean implements TemplateService {

    @Autowired
    private TemplateRepository repository;

    @Autowired
    private Configuration configuration;

    @Autowired
    private Gson gson;

    @Override
    public TemplateDto getTemplateByKey(String key) {
        return repository.getOne(key);
    }

    @Override
    public boolean templateExists(String key) {
        return getTemplateByKey(key) != null;
    }

    @Override
    public String renderTemplate(TemplateDto templateDto) {
        return null;
    }

    @Override
    public MessageToSend renderEmbedTemplate(String key, Object model) {
        String embedConfig = this.renderTemplate(key + "_embed", model);
        EmbedBuilder builder = new EmbedBuilder();
        EmbedConfiguration configuration = gson.fromJson(embedConfig, EmbedConfiguration.class);
        String description = configuration.getDescription();
        if(description != null) {
            builder.setDescription(description);
        }
        EmbedAuthor author = configuration.getAuthor();
        if(author != null) {
            builder.setAuthor(author.getName(), author.getUrl(), author.getAvatar());
        }

        String thumbnail = configuration.getThumbnail();
        if(thumbnail != null) {
            builder.setThumbnail(thumbnail);
        }
        EmbedTitle title = configuration.getTitle();
        if(title != null) {
            builder.setTitle(title.getTitle(), title.getUrl());
        }
        EmbedFooter footer = configuration.getFooter();
        if(footer != null) {
            builder.setFooter(footer.getText(), footer.getIcon());
        }
        if(configuration.getFields() != null) {
            configuration.getFields().forEach(embedField -> {
                Boolean inline = embedField.getInline() != null ? embedField.getInline() : Boolean.FALSE;
                builder.addField(embedField.getName(), embedField.getValue(), inline);
            });
        }
        builder.setTimestamp(configuration.getTimeStamp());

        builder.setImage(configuration.getImageUrl());

        EmbedColor color = configuration.getColor();
        if(color != null) {
            builder.setColor(new Color(color.getR(), color.getG(), color.getB()).getRGB());
        }


        return MessageToSend.builder()
                .embed(builder.build())
                .message(configuration.getAdditionalMessage())
                .build();
    }

    @Override
    public String renderTemplate(String key, HashMap<String, Object> parameters) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(key), parameters);
        } catch (IOException | TemplateException e) {
            log.warn("Failed to render template: {}", e.getMessage());
        }
        return "";
    }

    @Override
    public String renderTemplate(String key, Object model) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(key), model);
        } catch (IOException | TemplateException e) {
            log.warn("Failed to render template: {}", e.getMessage());
            throw new RuntimeException("Failed to render template", e);
        }
    }

    @Override
    public String renderContextAwareTemplate(String key, ServerContext serverContext) {
        return renderTemplate(getTemplateKey(key, serverContext), serverContext);
    }

    private String getTemplateKey(String key, ContextAware contextAware) {
        if(!contextAware.getTemplateSuffix().equals("")) {
            return key + "_" + contextAware.getTemplateSuffix();
        }
        return key;
    }

    @Override
    public void createTemplate(String key, String content) {
        repository.save(TemplateDto.builder().key(key).content(content).lastModified(Instant.now()).build());
    }
}

package dev.sheldan.abstracto.templating.loading;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.models.context.ContextAware;
import dev.sheldan.abstracto.core.models.context.ServerContext;
import dev.sheldan.abstracto.core.models.MessageToSend;
import dev.sheldan.abstracto.templating.TemplateDto;
import dev.sheldan.abstracto.templating.TemplateService;
import dev.sheldan.abstracto.templating.embed.*;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

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

    private String getPageString(Integer count) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("count", count);
        return renderTemplate("embed_page_count", params);
    }

    @Override
    public MessageToSend renderEmbedTemplate(String key, Object model) {
        String embedConfig = this.renderTemplate(key + "_embed", model);
        List<EmbedBuilder> embedBuilders = new ArrayList<>();
        EmbedConfiguration configuration = gson.fromJson(embedConfig, EmbedConfiguration.class);
        String description = configuration.getDescription();
        if(description != null) {
            double neededIndices = Math.ceil(description.length() / (double) MessageEmbed.TEXT_MAX_LENGTH) - 1;
            extendIfNecessary(embedBuilders, neededIndices);
            for (int i = 0; i < neededIndices + 1; i++) {
                String descriptionText = description.substring(MessageEmbed.TEXT_MAX_LENGTH * i, Math.min(MessageEmbed.TEXT_MAX_LENGTH * (i + 1), description.length()));
                embedBuilders.get(i).setDescription(descriptionText);
            }
        }
        EmbedAuthor author = configuration.getAuthor();
        EmbedBuilder firstBuilder = embedBuilders.get(0);
        if(author != null) {
            firstBuilder.setAuthor(author.getName(), author.getUrl(), author.getAvatar());
        }

        String thumbnail = configuration.getThumbnail();
        if(thumbnail != null) {
            firstBuilder.setThumbnail(thumbnail);
        }
        EmbedTitle title = configuration.getTitle();
        if(title != null) {
            firstBuilder.setTitle(title.getTitle(), title.getUrl());
        }
        EmbedFooter footer = configuration.getFooter();
        if(footer != null) {
            firstBuilder.setFooter(footer.getText(), footer.getIcon());
        }
        if(configuration.getFields() != null) {
            double neededIndex = Math.ceil(configuration.getFields().size() / 25D) - 1;
            extendIfNecessary(embedBuilders, neededIndex);
            for (int i = 0; i < configuration.getFields().size(); i++) {
                double currentPart = Math.floor(i / 25D);
                EmbedField embedField = configuration.getFields().get(i);
                Boolean inline = embedField.getInline() != null ? embedField.getInline() : Boolean.FALSE;
                embedBuilders.get((int) currentPart).addField(embedField.getName(), embedField.getValue(), inline);
            }
        }
        firstBuilder.setTimestamp(configuration.getTimeStamp());

        firstBuilder.setImage(configuration.getImageUrl());

        EmbedColor color = configuration.getColor();
        if(color != null) {
            int colorToSet = new Color(color.getR(), color.getG(), color.getB()).getRGB();
            embedBuilders.forEach(embedBuilder -> embedBuilder.setColor(colorToSet));
        }

        List<MessageEmbed> embeds = embedBuilders.stream().map(EmbedBuilder::build).collect(Collectors.toList());

        return MessageToSend.builder()
                .embeds(embeds)
                .message(configuration.getAdditionalMessage())
                .build();
    }

    private void extendIfNecessary(List<EmbedBuilder> builders, double neededIndex) {
        if(neededIndex > builders.size() - 1) {
            for (int i = builders.size(); i < neededIndex + 1; i++) {
                EmbedBuilder e = new EmbedBuilder();
                e.setFooter(getPageString(i + 1));
                builders.add(e);
            }
        }
    }

    @Override
    public String renderTemplate(String key, HashMap<String, Object> parameters) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(key), parameters);
        } catch (IOException | TemplateException e) {
            log.warn("Failed to render template. ", e);
        }
        return "";
    }

    @Override
    public String renderTemplate(String key, Object model) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(key), model);
        } catch (IOException | TemplateException e) {
            log.warn("Failed to render template. ", e);
            return null;
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

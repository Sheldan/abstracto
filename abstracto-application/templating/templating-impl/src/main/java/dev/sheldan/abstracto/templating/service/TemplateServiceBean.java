package dev.sheldan.abstracto.templating.service;

import com.google.gson.Gson;
import dev.sheldan.abstracto.templating.Templatable;
import dev.sheldan.abstracto.templating.model.*;
import dev.sheldan.abstracto.templating.model.database.Template;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean used to render a template, identified by a key, with the passed model.
 */
@Slf4j
@Component
public class TemplateServiceBean implements TemplateService {

    @Autowired
    private Configuration configuration;

    @Autowired
    private Gson gson;

    /**
     * Formats the passed passed count with the embed used for formatting pages.
     * @param count The index of the page you want formated.
     * @return The rendered template as a string object
     */
    private String getPageString(Integer count) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("count", count);
        return renderTemplateWithMap("embed_page_count", params);
    }

    /**
     * Retrieves the key which gets suffixed with '_embed' and this retrives the embed configuration. This configuration is then rendered
     * and deserialized with GSON into a {@link EmbedConfiguration} object. This object is then rendered into a {@link MessageToSend} and returned.
     * If the individual element do not fit in an embed, for example, if the field count is to high, another embed will be created in the {@link MessageToSend} object.
     * If multiple embeds are necessary to provide what the {@link EmbedConfiguration} wanted, this method will automatically set the footer of the additional {@link MessageEmbed}
     *  with a formatted page count.
     * This method will to try its best to provided a message which can be handled by discord without rejecting it. Besides that, the content from the rendered template, will be passed
     * into the {@link EmbedBuilder} directly.
     * @param key The key of the embed template to be used for rendering.
     * @param model The model providing the properties to be used for rendering
     * @return The {@link MessageToSend} object which is properly split up in order to be send to discord.
     */
    @Override
    public MessageToSend renderEmbedTemplate(String key, Object model) {
        String embedConfig = this.renderTemplate(key + "_embed", model);
        List<EmbedBuilder> embedBuilders = new ArrayList<>();
        embedBuilders.add(new EmbedBuilder());
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
            for (int i = 0; i < configuration.getFields().size(); i++) {
                EmbedField field = configuration.getFields().get(i);
                if(field != null && field.getValue() != null) {
                    if(field.getValue().length() > MessageEmbed.VALUE_MAX_LENGTH) {
                        String substring = field.getValue().substring(MessageEmbed.VALUE_MAX_LENGTH);
                        field.setValue(field.getValue().substring(0, MessageEmbed.VALUE_MAX_LENGTH));
                        EmbedField secondPart = EmbedField.builder().inline(field.getInline()).name(field.getName() + " 2").value(substring).build();
                        configuration.getFields().add(i + 1, secondPart);
                    }
                } else {
                    log.warn("Field {} in template {} is null.", i, key);
                }
            }
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

        List<MessageEmbed> embeds = new ArrayList<>();
        if(embedBuilders.size() > 1 || !embedBuilders.get(0).isEmpty()) {
            embeds = embedBuilders.stream().map(EmbedBuilder::build).collect(Collectors.toList());
        }

        return MessageToSend.builder()
                .embeds(embeds)
                .message(configuration.getAdditionalMessage())
                .build();
    }

    /**
     * Enlarges the passed list of builders, if the passed index is not yet available within the list.
     * When a new builder is needed, this will automatically set the footer with a page indicator.
     * @param builders The current list of {@link EmbedBuilder} builders used.
     * @param neededIndex The desired index in the list which should be available for using.
     */
    private void extendIfNecessary(List<EmbedBuilder> builders, double neededIndex) {
        if(neededIndex > builders.size() - 1) {
            for (int i = builders.size(); i < neededIndex + 1; i++) {
                EmbedBuilder e = new EmbedBuilder();
                e.setFooter(getPageString(i + 1));
                builders.add(e);
            }
        }
    }

    /**
     * Renders the template identified by the key with the passed {@link HashMap}
     * @param key The key of the template to be rendered.
     * @param parameters The {@link HashMap} to be used as the parameters for the template
     * @return The rendered template as a string
     */
    @Override
    public String renderTemplateWithMap(String key, HashMap<String, Object> parameters) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(key), parameters);
        } catch (IOException | TemplateException e) {
            log.warn("Failed to render template. ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Renders the template identified by the key with the passed object as model
     * @param key The key of the template to be rendered
     * @param model The object containing the model to be used in the template
     * @return The rendered template as a string
     */
    @Override
    public String renderTemplate(String key, Object model) {
        try {
            return FreeMarkerTemplateUtils.processTemplateIntoString(configuration.getTemplate(key), model);
        } catch (IOException | TemplateException e) {
            log.warn("Failed to render template. ", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Renders a simple template identified by key without any model. This will cause exceptions in case there are references to a model in the provided template.
     * @param key The key of the template to be rendered
     * @return The rendered template as a string
     */
    @Override
    public String renderSimpleTemplate(String key) {
        return renderTemplate(key, new Object());
    }

    /**
     * Renders the {@link Templatable} object using the template key and the model and returns it as a string.
     * @param templatable The {@link Templatable} object to be rendered
     * @return The rendered {@link Templatable} as a string
     */
    @Override
    public String renderTemplatable(Templatable templatable) {
        return renderTemplate(templatable.getTemplateName(), templatable.getTemplateModel());
    }
}

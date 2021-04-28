package dev.sheldan.abstracto.core.templating.service;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureConfig;
import dev.sheldan.abstracto.core.config.ServerContext;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.templating.Templatable;
import dev.sheldan.abstracto.core.templating.exception.TemplatingException;
import dev.sheldan.abstracto.core.templating.model.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean used to render a template, identified by a key, with the passed model.
 */
@Slf4j
@Component
public class TemplateServiceBean implements TemplateService {

    public static final double MAX_FIELD_COUNT = 25D;
    @Autowired
    private Configuration configuration;

    @Autowired
    private Gson gson;

    @Autowired
    private ServerContext serverContext;

    @Autowired
    private ConfigService configService;

    /**
     * Formats the passed passed count with the embed used for formatting pages.
     *
     * @param count The index of the page you want formatted.
     * @return The rendered template as a string object
     */
    private String getPageString(Integer count) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("count", count);
        return renderTemplateWithMap("embed_page_count", params);
    }

    /**
     * Retrieves the key which gets suffixed with '_embed' and this retrieves the embed configuration. This configuration is then rendered
     * and de-serialized with GSON into a {@link EmbedConfiguration} object. This object is then rendered into a {@link MessageToSend} and returned.
     * If the individual element do not fit in an embed, for example, if the field count is to high, another embed will be created in the {@link MessageToSend} object.
     * If multiple embeds are necessary to provide what the {@link EmbedConfiguration} wanted, this method will automatically set the footer of the additional {@link MessageEmbed}
     * with a formatted page count.
     * This method will to try its best to provided a message which can be handled by discord without rejecting it. Besides that, the content from the rendered template, will be passed
     * into the {@link EmbedBuilder} directly.
     *
     * @param key   The key of the embed template to be used for rendering.
     * @param model The model providing the properties to be used for rendering
     * @return The {@link MessageToSend} object which is properly split up in order to be send to discord.
     */
    @Override
    public MessageToSend renderEmbedTemplate(String key, Object model) {
        String embedConfig = this.renderTemplate(key + "_embed", model);
        EmbedConfiguration embedConfiguration = gson.fromJson(embedConfig, EmbedConfiguration.class);
        List<EmbedBuilder> embedBuilders = new ArrayList<>();
        embedBuilders.add(new EmbedBuilder());
        String description = embedConfiguration.getDescription();
        if (description != null) {
            handleEmbedDescription(embedBuilders, description);
        }
        EmbedAuthor author = embedConfiguration.getAuthor();
        EmbedBuilder firstBuilder = embedBuilders.get(0);
        if (author != null) {
            firstBuilder.setAuthor(author.getName(), author.getUrl(), author.getAvatar());
        }

        String thumbnail = embedConfiguration.getThumbnail();
        if (thumbnail != null) {
            firstBuilder.setThumbnail(thumbnail);
        }
        EmbedTitle title = embedConfiguration.getTitle();
        if (title != null) {
            firstBuilder.setTitle(title.getTitle(), title.getUrl());
        }
        EmbedFooter footer = embedConfiguration.getFooter();
        if (footer != null) {
            firstBuilder.setFooter(footer.getText(), footer.getIcon());
        }
        if (embedConfiguration.getFields() != null) {
            createFieldsForEmbed(embedBuilders, embedConfiguration);
        }
        firstBuilder.setTimestamp(embedConfiguration.getTimeStamp());

        firstBuilder.setImage(embedConfiguration.getImageUrl());

        EmbedColor color = embedConfiguration.getColor();
        if (color != null) {
            int colorToSet = new Color(color.getR(), color.getG(), color.getB()).getRGB();
            embedBuilders.forEach(embedBuilder -> embedBuilder.setColor(colorToSet));
        }

        setPagingFooters(embedBuilders);

        List<MessageEmbed> embeds = new ArrayList<>();
        if ((embedBuilders.size() > 1 || !embedBuilders.get(0).isEmpty()) && !isEmptyEmbed(embedConfiguration)) {
            embeds = embedBuilders.stream().map(EmbedBuilder::build).collect(Collectors.toList());
        }

        List<String> messages = new ArrayList<>();
        String additionalMessage = embedConfiguration.getAdditionalMessage();
        if(additionalMessage != null) {
            Long segmentLimit = embedConfiguration.getMetaConfig() != null
                    && embedConfiguration.getMetaConfig().getAdditionalMessageLengthLimit() != null ?
                    embedConfiguration.getMetaConfig().getAdditionalMessageLengthLimit() :
                    Long.valueOf(Message.MAX_CONTENT_LENGTH);
            if(additionalMessage.length() > segmentLimit) {
                int segmentStart = 0;
                int segmentEnd = segmentLimit.intValue();
                while(segmentStart < additionalMessage.length()) {
                    int segmentLength = additionalMessage.length() - segmentStart;
                    if(segmentLength > segmentLimit) {
                        int lastSpace = additionalMessage.substring(segmentStart, segmentEnd).lastIndexOf(" ");
                        if(lastSpace != -1) {
                            segmentEnd = segmentStart + lastSpace;
                        }
                    } else {
                        segmentEnd = additionalMessage.length();
                    }
                    String messageText = additionalMessage.substring(segmentStart, segmentEnd);
                    messages.add(messageText);
                    segmentStart = segmentEnd;
                    segmentEnd += segmentLimit;
                }
            } else {
                messages.add(additionalMessage);
            }
        }
        Long messageLimit = 100L;
        if(serverContext.getServerId() != null) {
            messageLimit = Math.min(messageLimit, configService.getLongValueOrConfigDefault(CoreFeatureConfig.MAX_MESSAGES_KEY, serverContext.getServerId()));
        }
        if(embedConfiguration.getMetaConfig() != null && embedConfiguration.getMetaConfig().getMessageLimit() != null) {
            messageLimit = Math.min(messageLimit, embedConfiguration.getMetaConfig().getMessageLimit());
        }
        if(embeds.size() > messageLimit) {
            log.info("Limiting size of embeds. Max allowed: {}, currently: {}.", messageLimit, embeds.size());
            embeds.subList(messageLimit.intValue(), embeds.size()).clear();
        }
        if(messages.size() > messageLimit) {
            log.info("Limiting size of messages. Max allowed: {}, currently: {}.", messageLimit, messages.size());
            messages.subList(messageLimit.intValue(), messages.size()).clear();
        }
        Long referencedMessageId = embedConfiguration.getReferencedMessageId();

        return MessageToSend.builder()
                .embeds(embeds)
                .messageConfig(createMessageConfig(embedConfiguration.getMetaConfig()))
                .messages(messages)
                .referencedMessageId(referencedMessageId)
                .build();
    }

    private MessageConfig createMessageConfig(MetaEmbedConfiguration metaEmbedConfiguration) {
        if(metaEmbedConfiguration == null) {
            return null;
        }
        return MessageConfig
                .builder()
                .allowsEveryoneMention(metaEmbedConfiguration.isAllowsEveryoneMention())
                .allowsUserMention(metaEmbedConfiguration.isAllowsUserMention())
                .allowsRoleMention(metaEmbedConfiguration.isAllowsRoleMention())
                .mentionsReferencedMessage(metaEmbedConfiguration.isMentionsReferencedMessage())
                .build();
    }

    private void setPagingFooters(List<EmbedBuilder> embedBuilders) {
        // the first footer comes from the configuration
        for (int i = 1; i < embedBuilders.size(); i++) {
            embedBuilders.get(i).setFooter(getPageString(i + 1));
        }
    }

    private void handleEmbedDescription(List<EmbedBuilder> embedBuilders, String description) {
        int segmentCounter = 0;
        int segmentStart = 0;
        int segmentEnd = MessageEmbed.TEXT_MAX_LENGTH;
        int handledIndex = 0;
        while(handledIndex < description.length()) {
            int segmentLength = description.length() - segmentStart;
            if(segmentLength > MessageEmbed.TEXT_MAX_LENGTH) {
                int lastSpace = description.substring(segmentStart, segmentEnd).lastIndexOf(" ");
                if(lastSpace != -1) {
                    segmentEnd = segmentStart + lastSpace;
                }
            } else {
                segmentEnd = description.length();
            }
            String descriptionText = description.substring(segmentStart, segmentEnd);
            extendIfNecessary(embedBuilders, segmentCounter);
            embedBuilders.get(segmentCounter).setDescription(descriptionText);
            segmentCounter++;
            handledIndex = segmentEnd;
            segmentStart = segmentEnd;
            segmentEnd += MessageEmbed.TEXT_MAX_LENGTH;
        }
    }

    @Override
    public MessageToSend renderEmbedTemplate(String key, Object model, Long serverId) {
        try {
            serverContext.setServerId(serverId);
            return renderEmbedTemplate(key, model);
        } finally {
            serverContext.clear();
        }
    }

    private boolean isEmptyEmbed(EmbedConfiguration configuration) {
        if (configuration.getMetaConfig() != null && configuration.getMetaConfig().isPreventEmptyEmbed()) {
            return configuration.getFields() == null && configuration.getDescription() == null && configuration.getImageUrl() == null;
        }
        return false;
    }

    @Override
    public MessageToSend renderTemplateToMessageToSend(String key, Object model) {
        return MessageToSend.builder().messages(Arrays.asList(renderTemplate(key, model))).build();
    }

    @Override
    public MessageToSend renderTemplateToMessageToSend(String key, Object model, Long serverId) {
        try {
            serverContext.setServerId(serverId);
            return renderTemplateToMessageToSend(key, model);
        } finally {
            serverContext.clear();
        }
    }

    private void splitFieldsIntoAppropriateLengths(EmbedConfiguration configuration) {
        Comparator<AdditionalEmbedField> comparator = Comparator.comparing(o -> o.fieldIndex);
        // we need to reverse this, because the insertion need to be done from the back to the front, because we only insert according to field index
        // and we need to insert the "first" element last, so it actually gets the correct field index
        comparator = comparator.thenComparing(additionalEmbedField -> additionalEmbedField.innerFieldIndex).reversed();
        TreeSet<AdditionalEmbedField> toInsert = new TreeSet<>(comparator);
        for (int i = 0; i < configuration.getFields().size(); i++) {
            EmbedField field = configuration.getFields().get(i);
            if (field != null && field.getValue() != null && field.getValue().length() > MessageEmbed.VALUE_MAX_LENGTH) {
                int segmentCounter = 0;
                int segmentStart = 0;
                int segmentEnd = MessageEmbed.VALUE_MAX_LENGTH;
                int handledIndex = 0;
                String fullFieldValue = field.getValue();
                while(handledIndex < fullFieldValue.length()) {
                    int segmentLength = fullFieldValue.length() - segmentStart;
                    if(segmentLength > MessageEmbed.VALUE_MAX_LENGTH) {
                        int lastSpace = fullFieldValue.substring(segmentStart, segmentEnd).lastIndexOf(" ");
                        if(lastSpace != -1) {
                            segmentEnd = segmentStart + lastSpace;
                        }
                    } else {
                        segmentEnd = fullFieldValue.length();
                    }
                    String fieldValue = fullFieldValue.substring(segmentStart, segmentEnd);
                    if(segmentCounter == 0) {
                        field.setValue(fieldValue);
                    } else {
                        EmbedField newField = EmbedField
                                .builder()
                                .inline(field.getInline())
                                .name(field.getName() + " " + (segmentCounter + 1))
                                .value(fieldValue).build();
                        AdditionalEmbedField additionalField = new AdditionalEmbedField(i, segmentCounter, newField);
                        toInsert.add(additionalField);
                    }
                    segmentCounter++;
                    handledIndex = segmentEnd;
                    segmentStart = segmentEnd;
                    segmentEnd += MessageEmbed.VALUE_MAX_LENGTH;
                }
            }
        }
        for (AdditionalEmbedField field : toInsert) {
            configuration.getFields().add(field.getFieldIndex() + 1, field.getField());
        }
    }

    private void createFieldsForEmbed(List<EmbedBuilder> embedBuilders, EmbedConfiguration configuration) {
        splitFieldsIntoAppropriateLengths(configuration);
        int actualCurrentIndex = 0;
        int neededMessages = 0;
        for (int i = 0; i < configuration.getFields().size(); i++) {
            EmbedField field = configuration.getFields().get(i);
            boolean lastMessageInEmbed = ((actualCurrentIndex + 1) % MAX_FIELD_COUNT) == 0;
            boolean isStartOfNewMessage = (actualCurrentIndex % MAX_FIELD_COUNT) == 0;
            boolean newMessageForcedWithinEmbeds = Boolean.TRUE.equals(field.getForceNewMessage()) && !lastMessageInEmbed;
            boolean startOfNewMessage = actualCurrentIndex != 0 && isStartOfNewMessage;
            if (newMessageForcedWithinEmbeds || startOfNewMessage) {
                actualCurrentIndex = 0;
                neededMessages++;
            } else {
                actualCurrentIndex++;
            }
            extendIfNecessary(embedBuilders, neededMessages);
            EmbedField embedField = configuration.getFields().get(i);
            boolean inline = embedField.getInline() != null ? embedField.getInline() : Boolean.FALSE;
            embedBuilders.get(neededMessages).addField(embedField.getName(), embedField.getValue(), inline);
        }
        Comparator<AdditionalEmbed> comparator = Comparator.comparing(o -> o.embedIndex);
        // we need to reverse this, because the insertion need to be done from the back to the front, because we only insert according to field index
        // and we need to insert the "first" element last, so it actually gets the correct field index
        comparator = comparator.thenComparing(additionalEmbedField -> additionalEmbedField.innerEmbedIndex).reversed();
        TreeSet<AdditionalEmbed> toInsert = new TreeSet<>(comparator);

        for (int i = 0; i < embedBuilders.size(); i++) {
            EmbedBuilder embedBuilder = embedBuilders.get(i);
            if(!embedBuilder.isValidLength()) {
                int totalFieldLength = 0;
                // we need to calculate the length without fields this way, because the title property is not public
                for (MessageEmbed.Field field: embedBuilder.getFields()) {
                    totalFieldLength += getFieldLength(field);
                }
                int currentEmbedLength = embedBuilder.length() - totalFieldLength;
                List<MessageEmbed.Field> toRemove = new ArrayList<>();
                EmbedBuilder newlyAddedEmbed = new EmbedBuilder();
                boolean addToNew = false;
                boolean createNewEmbed;
                int additionalEmbedIndex = 0;
                for (int j = 0; j < embedBuilder.getFields().size(); j++) {
                    MessageEmbed.Field field = embedBuilder.getFields().get(j);
                    int fieldLength = getFieldLength(field);
                    if(currentEmbedLength + fieldLength > MessageEmbed.EMBED_MAX_LENGTH_BOT) {
                         createNewEmbed = true;
                         addToNew = true;
                         currentEmbedLength = 0;
                         additionalEmbedIndex++;
                    } else {
                        createNewEmbed = false;
                    }
                    if(createNewEmbed) {
                        // TODO this always creates a new embed, and does not re-use the existing ones
                        newlyAddedEmbed = new EmbedBuilder();
                        toInsert.add(new AdditionalEmbed(i, additionalEmbedIndex, newlyAddedEmbed));

                    }
                    if(addToNew) {
                        toRemove.add(field);
                        newlyAddedEmbed.addField(field);
                    }
                    currentEmbedLength += fieldLength;
                }
                embedBuilder.getFields().removeAll(toRemove);
            }
        }
        for (AdditionalEmbed embed : toInsert) {
            embedBuilders.add(embed.getEmbedIndex() + embed.getInnerEmbedIndex(), embed.getEmbedBuilder());
        }
    }

    private int getFieldLength(MessageEmbed.Field field) {
        int nameLength = 0;
        if(field.getName() != null) {
            nameLength = field.getName().length();
        }
        int valueLength = 0;
        if(field.getValue() != null) {
            valueLength = field.getValue().length();
        }
        return nameLength + valueLength;
    }

    /**
     * Enlarges the passed list of builders, if the passed index is not yet available within the list.
     * When a new builder is needed, this will automatically set the footer with a page indicator.
     *
     * @param builders    The current list of {@link EmbedBuilder} builders used.
     * @param neededIndex The desired index in the list which should be available for using.
     */
    private void extendIfNecessary(List<EmbedBuilder> builders, double neededIndex) {
        if (neededIndex > builders.size() - 1) {
            for (int i = builders.size(); i < neededIndex + 1; i++) {
                EmbedBuilder e = new EmbedBuilder();
                e.setFooter(getPageString(i + 1));
                builders.add(e);
            }
        }
    }

    /**
     * Renders the template identified by the key with the passed {@link HashMap}
     *
     * @param key        The key of the template to be rendered.
     * @param parameters The {@link HashMap} to be used as the parameters for the template
     * @return The rendered template as a string
     */
    @Override
    public String renderTemplateWithMap(String key, HashMap<String, Object> parameters) {
        try {
            return renderTemplateToString(key, parameters);
        } catch (IOException | TemplateException e) {
            log.warn("Failed to render template. ", e);
            throw new TemplatingException(e);
        }
    }

    @Override
    public String renderTemplateWithMap(String key, HashMap<String, Object> parameters, Long serverId) {
        try {
            serverContext.setServerId(serverId);
            return renderTemplateWithMap(key, parameters);
        } finally {
            serverContext.clear();
        }
    }

    /**
     * Renders the template identified by the key with the passed object as model
     *
     * @param key   The key of the template to be rendered
     * @param model The object containing the model to be used in the template
     * @return The rendered template as a string
     */
    @Override
    public String renderTemplate(String key, Object model) {
        try {
            return renderTemplateToString(key, model);
        } catch (IOException | TemplateException e) {
            log.warn("Failed to render template. ", e);
            throw new TemplatingException(e);
        }
    }

    @Override
    public String renderTemplate(String key, Object model, Long serverId) {
        try {
            serverContext.setServerId(serverId);
            return renderTemplate(key, model);
        } finally {
            serverContext.clear();
        }
    }

    /**
     * Loads the given key as a template, and renders it, returns the result as a String
     *
     * @param key   The key of the template to render
     * @param model The parameters which are given to the template
     * @return The rendered template in a String
     * @throws freemarker.template.TemplateNotFoundException In case the template could not be found
     * @throws TemplateException                             In case the rendering failed
     */
    private String renderTemplateToString(String key, Object model) throws IOException, TemplateException {
        StringWriter result = new StringWriter();
        Template template = configuration.getTemplate(key, null, serverContext.getServerId(), null, true, false);
        template.process(model, result);
        return result.toString();
    }

    /**
     * Renders a simple template identified by key without any model. This will cause exceptions in case there are references to a model in the provided template.
     *
     * @param key The key of the template to be rendered
     * @return The rendered template as a string
     */
    @Override
    public String renderSimpleTemplate(String key) {
        return renderTemplate(key, new Object());
    }

    @Override
    public String renderSimpleTemplate(String key, Long serverId) {
        try {
            serverContext.setServerId(serverId);
            return renderSimpleTemplate(key);
        } finally {
            serverContext.clear();
        }
    }

    /**
     * Renders the {@link Templatable} object using the template key and the model and returns it as a string.
     *
     * @param templatable The {@link Templatable} object to be rendered
     * @return The rendered {@link Templatable} as a string
     */
    @Override
    public String renderTemplatable(Templatable templatable) {
        return renderTemplate(templatable.getTemplateName(), templatable.getTemplateModel());
    }

    @Override
    public String renderTemplatable(Templatable templatable, Long serverId) {
        try {
            serverContext.setServerId(serverId);
            return renderTemplatable(templatable);
        } finally {
            serverContext.clear();
        }
    }

    @Override
    public void clearCache() {
        configuration.getCacheStorage().clear();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class AdditionalEmbedField {
        // this is the index of the field where this additional field came from
        private Integer fieldIndex;
        // this is the index within the field, which means, a field might have been split into three fields
        // and in this case this value defines which respective part of that field it is, this is required to
        // keep the order of the created fields
        private Integer innerFieldIndex;
        private EmbedField field;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class AdditionalEmbed {
        private Integer embedIndex;
        private Integer innerEmbedIndex;
        private EmbedBuilder embedBuilder;
    }
}

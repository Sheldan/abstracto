package dev.sheldan.abstracto.core.templating.service;

import com.google.gson.Gson;
import dev.sheldan.abstracto.core.command.config.features.CoreFeatureConfig;
import dev.sheldan.abstracto.core.config.ServerContext;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.models.database.ComponentType;
import dev.sheldan.abstracto.core.interaction.ComponentServiceBean;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.templating.Templatable;
import dev.sheldan.abstracto.core.templating.exception.TemplatingException;
import dev.sheldan.abstracto.core.templating.model.*;
import dev.sheldan.abstracto.core.utils.FileService;
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
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
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

    @Autowired
    private FileService fileService;

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
     * and de-serialized with GSON into a {@link MessageConfiguration} object. This object is then rendered into a {@link MessageToSend} and returned.
     * If the individual element do not fit in an embed, for example, if the field count is to high, another embed will be created in the {@link MessageToSend} object.
     * If multiple embeds are necessary to provide what the {@link MessageConfiguration} wanted, this method will automatically set the footer of the additional {@link MessageEmbed}
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
        MessageConfiguration messageConfiguration = gson.fromJson(embedConfig, MessageConfiguration.class);
        return convertEmbedConfigurationToMessageToSend(messageConfiguration);
    }

    public MessageToSend convertEmbedConfigurationToMessageToSend(MessageConfiguration messageConfiguration) {
        List<EmbedBuilder> embedBuilders = new ArrayList<>();
        if(messageConfiguration.getEmbeds() != null && !messageConfiguration.getEmbeds().isEmpty())  {
            convertEmbeds(messageConfiguration, embedBuilders);
        }

        List<ActionRow> actionRows = new ArrayList<>();
        Map<String, MessageToSend.ComponentConfig> componentPayloads = new HashMap<>();
        if(messageConfiguration.getButtons() != null || messageConfiguration.getSelectionMenus() != null) {
            // this basically preprocesses the buttons and select menus
            // by getting the positions of the items first
            // we only need this, because the current message config does not have them in the same item
            // they are two distinct lists, but map to the same concept in discord: components
            Set<Integer> positions = new HashSet<>();
            HashMap<Integer, ButtonConfig> buttonPositions = new HashMap<>();
            List<ButtonConfig> buttonsWithoutPosition = new ArrayList<>();
            HashMap<Integer, SelectionMenuConfig> selectionMenuPositions = new HashMap<>();
            List<SelectionMenuConfig> selectionMenusWithoutPosition = new ArrayList<>();
            // we do this by getting all positions which are part of the config
            // we also track which positions are buttons and which are select menus
            if(messageConfiguration.getButtons() != null) {
                messageConfiguration.getButtons().forEach(buttonConfig -> {
                    if(buttonConfig.getPosition() != null) {
                        positions.add(buttonConfig.getPosition());
                        buttonPositions.put(buttonConfig.getPosition(), buttonConfig);
                    } else {
                        buttonsWithoutPosition.add(buttonConfig);
                    }
                });
            }

            if(messageConfiguration.getSelectionMenus() != null) {
                messageConfiguration.getSelectionMenus().forEach(selectionMenuConfig -> {
                    if(selectionMenuConfig.getPosition() != null) {
                        positions.add(selectionMenuConfig.getPosition());
                        selectionMenuPositions.put(selectionMenuConfig.getPosition(), selectionMenuConfig);
                    } else {
                        selectionMenusWithoutPosition.add(selectionMenuConfig);
                    }
                });
            }
            List<Integer> positionsSorted = new ArrayList<>(positions);
            Collections.sort(positionsSorted);
            List<ButtonConfig> currentButtons = new ArrayList<>();
            // we go over all positions, and if its part of the buttons, we only add it to a list of buttons
            // this will then mean, that all buttons are processed as a group
            // this is necessary, because we can only add buttons as part of an action row
            // and in order to make it easier, we process the whole chunk of buttons at once, producing
            // at least one or more action rows
            for (Integer position : positionsSorted) {
                if (buttonPositions.containsKey(position)) {
                    currentButtons.add(buttonPositions.get(position));
                } else {
                    // if we get interrupted by a selection menu, we process the buttons we have so far
                    // because those should be handled as a group
                    // and then process the selection menu, the selection menu will always represent one full action row
                    // it is not possible to have a button and a menu in the same row
                    if(!currentButtons.isEmpty()) {
                        addButtons(actionRows, componentPayloads, currentButtons);
                        currentButtons.clear();
                    }
                    addSelectionMenu(actionRows, selectionMenuPositions.get(position));
                }
            }
            if(!currentButtons.isEmpty()) {
                addButtons(actionRows, componentPayloads, currentButtons);
                currentButtons.clear();
            }
            // all the rest without positions will be processed at the end (probably default case for most cases)
            addButtons(actionRows, componentPayloads, buttonsWithoutPosition);
            // selection menus are handled afterwards, that is just implied logic
            // to have a select menu before a button, one would need to set accordingly, or only
            // set the position for the selection menu, and not for the button
            selectionMenusWithoutPosition.forEach(selectionMenuConfig -> addSelectionMenu(actionRows, selectionMenuConfig));
        }

        setPagingFooters(embedBuilders);

        List<MessageEmbed> embeds = new ArrayList<>();
        if (!embedBuilders.isEmpty()) {
            embeds = embedBuilders
                    .stream()
                    .filter(embedBuilder -> !embedBuilder.isEmpty())
                    .map(EmbedBuilder::build)
                    .collect(Collectors.toList());
        }

        List<String> messages = new ArrayList<>();
        if(
                messageConfiguration.getMessageConfig() != null &&
                messageConfiguration.getMessageConfig().getAdditionalMessageLengthLimit() != null &&
                messageConfiguration.getAdditionalMessage().length() > messageConfiguration.getMessageConfig().getAdditionalMessageLengthLimit()
        ) {
            messageConfiguration.setAdditionalMessage(messageConfiguration.getAdditionalMessage().substring(0, messageConfiguration.getMessageConfig().getAdditionalMessageLengthLimit()));
        }

        boolean isEphemeral = false;
        if(messageConfiguration.getMessageConfig() != null) {
            isEphemeral = Boolean.TRUE.equals(messageConfiguration.getMessageConfig().isEphemeral());
        }

        List<AttachedFile> files = new ArrayList<>();
        if(messageConfiguration.getFiles() != null && !messageConfiguration.getFiles().isEmpty()) {
            messageConfiguration.getFiles().forEach(fileToAttach -> {
                String fileName = fileToAttach.getFileName() != null ? fileToAttach.getFileName() : RandomStringUtils.randomAlphabetic(5);
                AttachedFile attachedFile = AttachedFile
                        .builder()
                        .fileName(fileName)
                        .spoiler(fileToAttach.getSpoiler() != null && fileToAttach.getSpoiler())
                        .build();
                if(fileToAttach.getFileContent() != null) {
                    File tempFile = fileService.createTempFile(fileName);
                    try {
                        fileService.writeContentToFile(tempFile, fileToAttach.getFileContent());
                    } catch (IOException e) {
                        log.error("Failed to write local temporary file.", e);
                        throw new AbstractoRunTimeException(e);
                    }
                    attachedFile.setFile(tempFile);
                }
                files.add(attachedFile);
            });
        }

        String additionalMessage = messageConfiguration.getAdditionalMessage();
        if(additionalMessage != null) {
            Long segmentLimit = messageConfiguration.getMessageConfig() != null
                    && messageConfiguration.getMessageConfig().getAdditionalMessageSplitLength() != null ?
                    messageConfiguration.getMessageConfig().getAdditionalMessageSplitLength() :
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
        if(messageConfiguration.getMessageConfig() != null && messageConfiguration.getMessageConfig().getMessageLimit() != null) {
            messageLimit = Math.min(messageLimit, messageConfiguration.getMessageConfig().getMessageLimit());
        }
        if(messages.size() > messageLimit) {
            log.info("Limiting size of messages. Max allowed: {}, currently: {}.", messageLimit, messages.size());
            messages.subList(messageLimit.intValue(), messages.size()).clear();
        }
        Long referencedMessageId = messageConfiguration.getReferencedMessageId();

        return MessageToSend.builder()
                .embeds(embeds)
                .messageConfig(createMessageConfig(messageConfiguration.getMessageConfig()))
                .messages(messages)
                .ephemeral(isEphemeral)
                .attachedFiles(files)
                .actionRows(actionRows)
                .componentPayloads(componentPayloads)
                .referencedMessageId(referencedMessageId)
                .build();
    }

    private void addSelectionMenu(List<ActionRow> actionRows, SelectionMenuConfig selectionMenuConfig) {
        ItemComponent selectionMenu;
        if (selectionMenuConfig.getType() == SelectionMenuType.STRING) {
            List<SelectOption> selectOptions = selectionMenuConfig.getMenuEntries().stream().map(selectionMenuEntry -> {
                SelectOption option = SelectOption.of(selectionMenuEntry.getLabel(), selectionMenuEntry.getValue());
                if (StringUtils.isNotBlank(selectionMenuEntry.getDescription())) {
                    option = option.withDescription(selectionMenuEntry.getDescription());
                }
                if(Boolean.TRUE.equals(selectionMenuEntry.getIsDefault())) {
                    option = option.withDefault(true);
                }
                return option;
            }).collect(Collectors.toList());
            StringSelectMenu.Builder builder = StringSelectMenu
                    .create(selectionMenuConfig.getId())
                    .addOptions(selectOptions);
            List<SelectOption> defaultOptions = selectOptions
                    .stream()
                    .filter(SelectOption::isDefault)
                    .collect(Collectors.toList());
            builder.setDefaultOptions(defaultOptions);
            if (selectionMenuConfig.getMaxValues() != null) {
                builder.setMaxValues(selectionMenuConfig.getMaxValues());
            }
            if (selectionMenuConfig.getMinValues() != null) {
                builder.setMinValues(selectionMenuConfig.getMinValues());
            }
            if (selectionMenuConfig.getPlaceholder() != null) {
                builder.setPlaceholder(selectionMenuConfig.getPlaceholder());
            }
            selectionMenu = builder.build();
        } else {
            Set<EntitySelectMenu.SelectTarget> targets = new HashSet<>();
            if(selectionMenuConfig.getTargets() != null) {
                selectionMenuConfig.getTargets().forEach(selectionMenuTarget -> {
                    switch (selectionMenuTarget) {
                        case ROLE:
                            targets.add(EntitySelectMenu.SelectTarget.ROLE);
                            break;
                        case USER:
                            targets.add(EntitySelectMenu.SelectTarget.USER);
                            break;
                        case CHANNEL:
                            targets.add(EntitySelectMenu.SelectTarget.CHANNEL);
                            break;
                    }
                });
            }

            Set<ChannelType> channelTypes = new HashSet<>();
            if(selectionMenuConfig.getChannelTypes() != null) {
                selectionMenuConfig.getChannelTypes().forEach(channelType -> {
                    switch (channelType) {
                        case TEXT:
                            channelTypes.add(ChannelType.TEXT);
                            break;
                        case VOICE:
                            channelTypes.add(ChannelType.VOICE);
                            break;
                    }
                });
            }
            selectionMenu = EntitySelectMenu.create(selectionMenuConfig.getId(), targets)
                    .setChannelTypes(channelTypes)
                    .build();
        }
        actionRows.add(ActionRow.of(selectionMenu));
    }

    private void addButtons(List<ActionRow> actionRows, Map<String, MessageToSend.ComponentConfig> componentPayloads, List<ButtonConfig> buttonConfigs) {
        ActionRow currentRow = null;
        for (ButtonConfig buttonConfig : buttonConfigs) {
            ButtonMetaConfig metaConfig = buttonConfig.getMetaConfig() != null ? buttonConfig.getMetaConfig() : null;
            String id = metaConfig != null && Boolean.TRUE.equals(metaConfig.getGenerateRandomUUID()) ?
                    UUID.randomUUID().toString() : buttonConfig.getId();
            String componentOrigin = metaConfig != null ? metaConfig.getButtonOrigin() : null;
            MessageToSend.ComponentConfig componentConfig = null;
            try {
                componentConfig = MessageToSend.ComponentConfig
                        .builder()
                        .componentOrigin(componentOrigin)
                        .componentType(ComponentType.BUTTON)
                        .persistCallback(metaConfig != null && Boolean.TRUE.equals(metaConfig.getPersistCallback()))
                        .payload(buttonConfig.getButtonPayload())
                        .payloadType(buttonConfig.getPayloadType() != null ? Class.forName(buttonConfig.getPayloadType()) : null)
                        .build();
            } catch (ClassNotFoundException e) {
                throw new AbstractoRunTimeException("Referenced class in button config could not be found: " + buttonConfig.getPayloadType(), e);
            }
            componentPayloads.put(id, componentConfig);
            String idOrUl = buttonConfig.getUrl() == null ? buttonConfig.getId() : buttonConfig.getUrl();
            Button createdButton = Button.of(ButtonStyleConfig.getStyle(buttonConfig.getButtonStyle()), idOrUl, buttonConfig.getLabel());
            if (buttonConfig.getDisabled() != null) {
                createdButton = createdButton.withDisabled(buttonConfig.getDisabled());
            }
            if (buttonConfig.getEmoteMarkdown() != null) {
                createdButton = createdButton.withEmoji(Emoji.fromFormatted(buttonConfig.getEmoteMarkdown()));
            }
            if(currentRow == null) {
                currentRow = ActionRow.of(createdButton);
            } else if (
                    (
                            metaConfig != null &&
                                    Boolean.TRUE.equals(metaConfig.getForceNewRow())
                    )
                            || currentRow.getComponents().size() == ComponentServiceBean.MAX_BUTTONS_PER_ROW) {
                actionRows.add(currentRow);
                currentRow = ActionRow.of(createdButton);
            } else {
                currentRow.getComponents().add(createdButton);
            }
        }
        if(currentRow != null) {
            actionRows.add(currentRow);
        }
    }

    private void convertEmbeds(MessageConfiguration messageConfiguration, List<EmbedBuilder> embedBuilders) {
        int currentEffectiveEmbed;
        for (int embedIndex = 0; embedIndex < messageConfiguration.getEmbeds().size(); embedIndex++) {
            currentEffectiveEmbed = embedBuilders.size();
            EmbedConfiguration embedConfiguration = messageConfiguration.getEmbeds().get(embedIndex);
            if(isEmptyEmbed(embedConfiguration)) {
                continue;
            }
            EmbedBuilder mainEmbedBuilder = new EmbedBuilder();
            if(embedConfiguration.getMetaConfig() != null &&
                    embedConfiguration.getDescription() != null &&
                    embedConfiguration.getMetaConfig().getDescriptionMessageLengthLimit() != null &&
                    embedConfiguration.getDescription().length() > embedConfiguration.getMetaConfig().getDescriptionMessageLengthLimit())
            {
                embedConfiguration.setDescription(embedConfiguration.getDescription().substring(0,
                        embedConfiguration.getMetaConfig().getDescriptionMessageLengthLimit()));
            }
            EmbedAuthor author = embedConfiguration.getAuthor();

            if (author != null) {
                mainEmbedBuilder.setAuthor(author.getName(), author.getUrl(), author.getAvatar());
            }
            String thumbnail = embedConfiguration.getThumbnail();
            if (thumbnail != null) {
                mainEmbedBuilder.setThumbnail(thumbnail);
            }
            EmbedTitle title = embedConfiguration.getTitle();
            if (title != null) {
                mainEmbedBuilder.setTitle(title.getTitle(), title.getUrl());
            }
            EmbedFooter footer = embedConfiguration.getFooter();
            if (footer != null) {
                mainEmbedBuilder.setFooter(footer.getText(), footer.getIcon());
            }
            mainEmbedBuilder.setTimestamp(embedConfiguration.getTimeStamp());

            mainEmbedBuilder.setImage(embedConfiguration.getImageUrl());

            embedBuilders.add(mainEmbedBuilder);

            String description = embedConfiguration.getDescription();
            if (description != null) {
                handleEmbedDescription(embedBuilders, description);
            }
            if (embedConfiguration.getFields() != null) {
                createFieldsForEmbed(embedBuilders, embedConfiguration);
            }

            EmbedColor color = embedConfiguration.getColor();
            if (color != null) {
                int colorToSet = new Color(color.getR(), color.getG(), color.getB()).getRGB();
                for (int i = currentEffectiveEmbed; i < embedBuilders.size(); i++) {
                    EmbedBuilder embedBuilder = embedBuilders.get(i);
                    embedBuilder.setColor(colorToSet);
                }
            }
        }
    }

    private MessageConfig createMessageConfig(MetaMessageConfiguration messageconfiguration) {
        if(messageconfiguration == null) {
            return null;
        }
        return MessageConfig
                .builder()
                .allowsEveryoneMention(messageconfiguration.isAllowsEveryoneMention())
                .allowsUserMention(messageconfiguration.isAllowsUserMention())
                .allowsRoleMention(messageconfiguration.isAllowsRoleMention())
                .mentionsReferencedMessage(messageconfiguration.isMentionsReferencedMessage())
                .build();
    }

    private void setPagingFooters(List<EmbedBuilder> embedBuilders) {
        // the first footer comes from the configuration
        for (int i = 1; i < embedBuilders.size(); i++) {
            embedBuilders.get(i).setFooter(getPageString(i + 1));
        }
    }

    private void handleEmbedDescription(List<EmbedBuilder> embedBuilders, String description) {
        // we need to start with the "current" one, and can then extend onto it
        int segmentCounter = Math.max(embedBuilders.size() - 1, 0);
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
            initLocaleFromServer();
            return renderEmbedTemplate(key, model);
        } finally {
            serverContext.clear();
        }
    }

    // FIXME not thread safe
    private void initLocaleFromServer() {
        if(serverContext.getServerId() != null) {
            String localeConfig = configService.getStringValueOrConfigDefault(CoreFeatureConfig.LOCALE_CONFIG_KEY, serverContext.getServerId());
            serverContext.setLocale(localeConfig);
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
            initLocaleFromServer();
            return renderTemplateToMessageToSend(key, model);
        } finally {
            serverContext.clear();
        }
    }

    /**
     * This method limits the fields: if there are limits configured.
     * It also splits the fields into multiple fields, if the length is over the limit. The newly created fields, get a title with a suffixed 2.
     */
    private void splitFieldsIntoAppropriateLengths(EmbedConfiguration configuration) {
        Comparator<AdditionalEmbedField> comparator = Comparator.comparing(o -> o.fieldIndex);
        // we need to reverse this, because the insertion need to be done from the back to the front, because we only insert according to field index
        // and we need to insert the "first" element last, so it actually gets the correct field index
        comparator = comparator.thenComparing(additionalEmbedField -> additionalEmbedField.innerFieldIndex).reversed();
        TreeSet<AdditionalEmbedField> toInsert = new TreeSet<>(comparator);

        configuration.getFields().forEach(embedField -> {
            if(embedField.getValueLengthLimit() != null && embedField.getValueLengthLimit() < embedField.getValue().length()) {
                embedField.setValue(embedField.getValue().substring(0, embedField.getValueLengthLimit()));
            }
            if(embedField.getNameLengthLimit() != null && embedField.getNameLengthLimit() < embedField.getName().length()) {
                embedField.setName(embedField.getName().substring(0, embedField.getNameLengthLimit()));
            }
        });
        for (int i = 0; i < configuration.getFields().size(); i++) {
            EmbedField field = configuration.getFields().get(i);
            int maxSplitLength = field != null && field.getValueSplitLength() != null ? Math.min(field.getValueSplitLength(), MessageEmbed.VALUE_MAX_LENGTH) : MessageEmbed.VALUE_MAX_LENGTH;
            if (field != null && field.getValue() != null && field.getValue().length() > maxSplitLength) {
                int segmentCounter = 0;
                int segmentStart = 0;
                int segmentEnd = maxSplitLength;
                int handledIndex = 0;
                String fullFieldValue = field.getValue();
                while(handledIndex < fullFieldValue.length()) {
                    // lets determine how much text we can handle, if we iterated multiple times over this, the segement
                    // start has a value, so some things are cut off
                    int segmentLength = fullFieldValue.length() - segmentStart;
                    // if its over the hard limit for a field
                    if(segmentLength > maxSplitLength) {
                        // find the last space in the text, as a natural "splitting point"
                        int lastSpace = fullFieldValue.substring(segmentStart, segmentEnd).lastIndexOf(" ");
                        if(lastSpace != -1) {
                            // and use this as the new end
                            segmentEnd = segmentStart + lastSpace;
                        }
                    } else {
                        // just use the full length
                        segmentEnd = fullFieldValue.length();
                    }
                    // cut the field value to be appropriate
                    String fieldValue = fullFieldValue.substring(segmentStart, segmentEnd);
                    if(segmentCounter == 0) {
                        field.setValue(fieldValue);
                    } else {
                        // if we are in an additional segment, we have to create a new embed field, to hold our values
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
                    segmentEnd += maxSplitLength;
                }
            }
        }
        // insert additional fields
        for (AdditionalEmbedField field : toInsert) {
            configuration.getFields().add(field.getFieldIndex() + 1, field.getField());
        }
    }

    private void createFieldsForEmbed(List<EmbedBuilder> embedBuilders, EmbedConfiguration configuration) {
        splitFieldsIntoAppropriateLengths(configuration);
        int embedLocalFieldIndex = 0;
        int neededEmbeds = embedBuilders.size() - 1;
        for (int i = 0; i < configuration.getFields().size(); i++) {
            EmbedField field = configuration.getFields().get(i);
            // determine if we are the last field allowed in this embed
            boolean lastMessageInEmbed = ((embedLocalFieldIndex + 1) % MAX_FIELD_COUNT) == 0;
            // determine if we are at the beginning of a new message, that means we have the embed index 0
            boolean isStartOfNewEmbed = (embedLocalFieldIndex % MAX_FIELD_COUNT) == 0;
            // whether or not we have to force a new embed, but only if we are not the last message, then its fine anyway
            boolean newMessageForcedWithinEmbeds = Boolean.TRUE.equals(field.getForceNewEmbed()) && !lastMessageInEmbed;
            // if we are at the start of an _additional_ embed, this is false at the first embed
            boolean startOfNewMessage = embedLocalFieldIndex != 0 && isStartOfNewEmbed;
            int fieldLength = field.getName().length() + field.getValue().length();
            boolean currentEmbedOverTotalLimit = (embedBuilders.get(neededEmbeds).length() + fieldLength) > MessageEmbed.EMBED_MAX_LENGTH_BOT;
            if (newMessageForcedWithinEmbeds || startOfNewMessage || currentEmbedOverTotalLimit) {
                embedLocalFieldIndex = 0;
                neededEmbeds++;
            } else {
                embedLocalFieldIndex++;
            }
            extendIfNecessary(embedBuilders, neededEmbeds);
            EmbedField embedField = configuration.getFields().get(i);
            boolean inline = embedField.getInline() != null ? embedField.getInline() : Boolean.FALSE;
            embedBuilders.get(neededEmbeds).addField(embedField.getName(), embedField.getValue(), inline);
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
            initLocaleFromServer();
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
            initLocaleFromServer();
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
        Locale locale = null;
        if(serverContext.getLocale() != null) {
            locale = Locale.forLanguageTag(serverContext.getLocale());
        }
        Template template = configuration.getTemplate(key, locale, serverContext.getServerId(), null, true, false);
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
            initLocaleFromServer();
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
            initLocaleFromServer();
            return renderTemplatable(templatable);
        } finally {
            serverContext.clear();
        }
    }

    @Override
    public String renderDuration(Duration duration, Long serverId) {
        try {
            serverContext.setServerId(serverId);
            initLocaleFromServer();
            return renderDuration(duration);
        } finally {
            serverContext.clear();
        }
    }

    @Override
    public String renderDuration(Duration duration) {
        HashMap<String, Object> parameters = new HashMap<>();
        long days = duration.toDays();
        parameters.put("days", days);
        long hours = duration.toHours() % 24;
        parameters.put("hours", hours);
        long minutes = duration.toMinutes() % 60;
        parameters.put("minutes", minutes);

        long seconds = duration.get(ChronoUnit.SECONDS) % 60;
        parameters.put("seconds", seconds);

        return renderTemplateWithMap("duration_formatting", parameters);
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

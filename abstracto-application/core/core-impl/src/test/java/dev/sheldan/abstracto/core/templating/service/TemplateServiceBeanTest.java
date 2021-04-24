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
import freemarker.template.TemplateNotFoundException;
import io.micrometer.core.instrument.util.IOUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TemplateServiceBeanTest {

    public static final String EXAMPLE_URL = "https://example.com";
    public static final String FIELD_VALUE = "value";
    public static final String EMBED_PAGE_COUNT_TEMPLATE = "embed_page_count";
    public static final String FIELD_TEMPLATE = "{\n" +
            "\"name\": \"name\",\n" +
            "\"value\": \"value\",\n" +
            "\"inline\": \"true\"\n" +
            "}";

    public static final String FIELD_TEMPLATE_WITH_VALUE = "{\n" +
            "\"name\": \"name\",\n" +
            "\"value\": \"%s\",\n" +
            "\"inline\": \"true\"\n" +
            "}";
    @InjectMocks
    private TemplateServiceBean templateServiceBean;

    @Mock
    private Configuration configuration;

    @Mock
    private ConfigService configService;

    // requires the org.mockito.plugins.MockMaker file
    @Mock
    private Gson gson;

    @Mock
    private ServerContext serverContext;

    private static final String SIMPLE_TEMPLATE_SOURCE = "source";
    private static final String TEMPLATE_KEY = "template";
    private static final Long SERVER_ID = 1L;

    @Test
    public void testSimpleTemplate() throws IOException, TemplateException {
        setupServerAware();
        when(configuration.getTemplate(TEMPLATE_KEY, null, SERVER_ID, null, true, false)).thenReturn(getSimpleTemplate());
        String rendered = templateServiceBean.renderSimpleTemplate(TEMPLATE_KEY);
        Assert.assertEquals(SIMPLE_TEMPLATE_SOURCE, rendered);
    }

    private void setupServerAware() {
        when(serverContext.getServerId()).thenReturn(SERVER_ID);
        when(configService.getLongValueOrConfigDefault(CoreFeatureConfig.MAX_MESSAGES_KEY, SERVER_ID)).thenReturn(5L);
    }

    @Test
    public void renderTemplatable() throws IOException, TemplateException {
        setupServerAware();
        when(configuration.getTemplate(TEMPLATE_KEY, null, SERVER_ID, null, true, false)).thenReturn(getSimpleTemplate());
        Templatable templatable = getTemplatableWithSimpleTemplate();
        String rendered = templateServiceBean.renderTemplatable(templatable);
        Assert.assertEquals(SIMPLE_TEMPLATE_SOURCE, rendered);
    }

    @Test
    public void testRenderTooLongAdditionalMessage() throws IOException, TemplateException {
        when(serverContext.getServerId()).thenReturn(SERVER_ID);
        String additionalMessage = RandomStringUtils.randomAlphabetic(3500);
        when(configService.getLongValueOrConfigDefault(CoreFeatureConfig.MAX_MESSAGES_KEY, SERVER_ID)).thenReturn(5L);
        String templateContent = String.format("{ \"additionalMessage\": \"%s\"}", additionalMessage);
        EmbedConfiguration config = Mockito.mock(EmbedConfiguration.class);
        MetaEmbedConfiguration metaConfig = Mockito.mock(MetaEmbedConfiguration.class);
        when(config.getMetaConfig()).thenReturn(metaConfig);
        when(metaConfig.getAdditionalMessageLengthLimit()).thenReturn(2000L);
        when(config.getAdditionalMessage()).thenReturn(additionalMessage);
        when(metaConfig.getMessageLimit()).thenReturn(5L);
        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenReturn(new Template(getEmbedTemplateKey(), templateContent, getNonMockedConfiguration()));
        when(gson.fromJson(templateContent, EmbedConfiguration.class)).thenReturn(config);
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new Object());
        Assert.assertEquals(2, messageToSend.getMessages().size());
        Assert.assertEquals(additionalMessage.substring(0, 2000), messageToSend.getMessages().get(0));
        Assert.assertEquals(additionalMessage.substring(2000, 3500), messageToSend.getMessages().get(1));
    }

    @Test
    public void testRenderEmbedWithMessageLimit() throws IOException, TemplateException {
        when(serverContext.getServerId()).thenReturn(SERVER_ID);
        String additionalMessage = RandomStringUtils.randomAlphabetic(3500);
        when(configService.getLongValueOrConfigDefault(CoreFeatureConfig.MAX_MESSAGES_KEY, SERVER_ID)).thenReturn(5L);
        String templateContent = String.format("{ \"additionalMessage\": \"%s\", \"messageLimit\": 1}", additionalMessage);
        EmbedConfiguration config = Mockito.mock(EmbedConfiguration.class);
        MetaEmbedConfiguration metaConfig = Mockito.mock(MetaEmbedConfiguration.class);
        when(config.getMetaConfig()).thenReturn(metaConfig);
        when(metaConfig.getAdditionalMessageLengthLimit()).thenReturn(2000L);
        when(config.getAdditionalMessage()).thenReturn(additionalMessage);
        when(metaConfig.getMessageLimit()).thenReturn(1L);
        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenReturn(new Template(getEmbedTemplateKey(), templateContent, getNonMockedConfiguration()));
        when(gson.fromJson(templateContent, EmbedConfiguration.class)).thenReturn(config);
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new Object());
        Assert.assertEquals(1, messageToSend.getMessages().size());
        Assert.assertEquals(additionalMessage.substring(0, 2000), messageToSend.getMessages().get(0));
    }

    @Test
    public void testRenderTooLongMultipleAdditionalMessages() throws IOException, TemplateException {
        when(serverContext.getServerId()).thenReturn(SERVER_ID);
        String additionalMessage = RandomStringUtils.randomAlphabetic(3500);
        when(configService.getLongValueOrConfigDefault(CoreFeatureConfig.MAX_MESSAGES_KEY, SERVER_ID)).thenReturn(5L);
        String templateContent = String.format("{ \"additionalMessage\": \"%s\"}", additionalMessage);
        EmbedConfiguration config = Mockito.mock(EmbedConfiguration.class);
        MetaEmbedConfiguration metaConfig = Mockito.mock(MetaEmbedConfiguration.class);
        when(config.getMetaConfig()).thenReturn(metaConfig);
        when(metaConfig.getAdditionalMessageLengthLimit()).thenReturn(500L);
        when(metaConfig.getMessageLimit()).thenReturn(5L);
        when(config.getAdditionalMessage()).thenReturn(additionalMessage);
        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenReturn(new Template(getEmbedTemplateKey(), templateContent, getNonMockedConfiguration()));
        when(gson.fromJson(templateContent, EmbedConfiguration.class)).thenReturn(config);
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new Object());
        Assert.assertEquals(5, messageToSend.getMessages().size());
        Assert.assertEquals(additionalMessage.substring(0, 500), messageToSend.getMessages().get(0));
        Assert.assertEquals(additionalMessage.substring(500, 1000), messageToSend.getMessages().get(1));
        Assert.assertEquals(additionalMessage.substring(1000, 1500), messageToSend.getMessages().get(2));
        Assert.assertEquals(additionalMessage.substring(1500, 2000), messageToSend.getMessages().get(3));
        Assert.assertEquals(additionalMessage.substring(2000, 2500), messageToSend.getMessages().get(4));
    }

    @Test
    public void testTemplateWithMapParameter() throws IOException, TemplateException {
        setupServerAware();
        when(configuration.getTemplate(TEMPLATE_KEY, null, SERVER_ID, null, true, false)).thenReturn(getSimpleTemplate());
        String rendered = templateServiceBean.renderTemplateWithMap(TEMPLATE_KEY, new HashMap<>());
        Assert.assertEquals(SIMPLE_TEMPLATE_SOURCE, rendered);
    }

    @Test
    public void testEmbedWithDescription() throws IOException, TemplateException {
        setupServerAware();
        String descriptionText = "test";
        String fullEmbedTemplateKey = getEmbedTemplateKey();
        when(configuration.getTemplate(fullEmbedTemplateKey, null, SERVER_ID, null, true, false)).thenReturn(getEmbedTemplateWithDescription(descriptionText));
        when(gson.fromJson(embedTemplateWithDescription(descriptionText), EmbedConfiguration.class)).thenReturn(embedConfigWithDescription(descriptionText));
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        Assert.assertEquals(descriptionText, messageToSend.getEmbeds().get(0).getDescription());
    }

    @Test
    public void testEmbedWithAllUsableAttributes() throws IOException, TemplateException {
        setupServerAware();
        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenReturn(getEmbedTemplateWithFallFieldsUsedOnce());
        when(gson.fromJson(getFullEmbedConfigString(), EmbedConfiguration.class)).thenReturn(getFullEmbedConfiguration());
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        Assert.assertEquals("additionalMessage", messageToSend.getMessages().get(0));
        MessageEmbed onlyEmbed = messageToSend.getEmbeds().get(0);
        Assert.assertEquals(EXAMPLE_URL, onlyEmbed.getAuthor().getIconUrl());
        Assert.assertEquals("name", onlyEmbed.getAuthor().getName());
        Assert.assertEquals("description", onlyEmbed.getDescription());
        MessageEmbed.Field onlyField = onlyEmbed.getFields().get(0);
        Assert.assertEquals("name", onlyField.getName());
        Assert.assertEquals(FIELD_VALUE, onlyField.getValue());
        Assert.assertTrue(onlyField.isInline());
        Color color = onlyEmbed.getColor();
        Assert.assertEquals(255, color.getBlue());
        Assert.assertEquals(255, color.getRed());
        Assert.assertEquals(255, color.getGreen());
        Assert.assertEquals(EXAMPLE_URL, onlyEmbed.getUrl());
        Assert.assertEquals("text", onlyEmbed.getFooter().getText());
        Assert.assertEquals(EXAMPLE_URL, onlyEmbed.getFooter().getIconUrl());
        Assert.assertEquals(EXAMPLE_URL, onlyEmbed.getImage().getUrl());
        Assert.assertEquals(EXAMPLE_URL, onlyEmbed.getThumbnail().getUrl());
        Assert.assertEquals(1, messageToSend.getEmbeds().size());
    }

    @Test
    public void testEmbedWithTooLongDescriptionNoSpace() throws IOException, TemplateException {
        setupServerAware();
        int tooMuchCharacterCount = 1024;
        String descriptionText = RandomStringUtils.randomAlphabetic(MessageEmbed.TEXT_MAX_LENGTH + tooMuchCharacterCount);
        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenReturn(getEmbedTemplateWithDescription(descriptionText));
        when(configuration.getTemplate(EMBED_PAGE_COUNT_TEMPLATE, null, SERVER_ID, null, true, false)).thenReturn(getPageCountTemplate(1));
        when(gson.fromJson(embedTemplateWithDescription(descriptionText), EmbedConfiguration.class)).thenReturn(embedConfigWithDescription(descriptionText));
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        MessageEmbed firstEmbed = messageToSend.getEmbeds().get(0);
        Assert.assertEquals(MessageEmbed.TEXT_MAX_LENGTH, firstEmbed.getDescription().length());
        Assert.assertEquals(descriptionText.substring(0, MessageEmbed.TEXT_MAX_LENGTH), firstEmbed.getDescription());
        MessageEmbed secondEmbed = messageToSend.getEmbeds().get(1);
        Assert.assertEquals(tooMuchCharacterCount, secondEmbed.getDescription().length());
        Assert.assertEquals(descriptionText.substring(MessageEmbed.TEXT_MAX_LENGTH, MessageEmbed.TEXT_MAX_LENGTH + tooMuchCharacterCount), secondEmbed.getDescription());
    }

    @Test
    public void testEmbedWithTooManyFields() throws IOException, TemplateException {
        setupServerAware();
        int totalFieldCount = 30;
        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenReturn(getEmbedTemplateWithFieldCount(totalFieldCount));
        when(configuration.getTemplate(EMBED_PAGE_COUNT_TEMPLATE, null, SERVER_ID, null, true, false)).thenReturn(getPageCountTemplate(1));
        when(gson.fromJson(getFieldsEmbedConfigAsString(totalFieldCount), EmbedConfiguration.class)).thenReturn(getTooManyFieldsEmbedConfiguration());
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        MessageEmbed firstEmbed = messageToSend.getEmbeds().get(0);
        Assert.assertEquals(25, firstEmbed.getFields().size());
        MessageEmbed secondEmbed = messageToSend.getEmbeds().get(1);
        Assert.assertEquals(totalFieldCount % 25, secondEmbed.getFields().size());
    }

    @Test
    public void testEmbedWithTooLongFieldNoSpace() throws IOException, TemplateException {
        when(serverContext.getServerId()).thenReturn(SERVER_ID);
        String fieldValue = RandomStringUtils.randomAlphabetic(1500);
        when(configService.getLongValueOrConfigDefault(CoreFeatureConfig.MAX_MESSAGES_KEY, SERVER_ID)).thenReturn(5L);
        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenReturn(getEmbedTemplateWithTooLongField(fieldValue));
        when(gson.fromJson(getSingleFieldWithValue(fieldValue), EmbedConfiguration.class)).thenReturn(getEmbedWithSingleFieldOfValue(fieldValue));
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        MessageEmbed firstEmbed = messageToSend.getEmbeds().get(0);
        Assert.assertEquals(2, firstEmbed.getFields().size());
        Assert.assertEquals(fieldValue.substring(0, MessageEmbed.VALUE_MAX_LENGTH), firstEmbed.getFields().get(0).getValue());
        Assert.assertEquals(fieldValue.substring(MessageEmbed.VALUE_MAX_LENGTH), firstEmbed.getFields().get(1).getValue());
    }

    @Test
    public void testEmbedWithTooLongFieldWithSpace() throws IOException, TemplateException {
        setupServerAware();
        int partsLength = 750;
        String firstPart = RandomStringUtils.randomAlphabetic(partsLength);
        String secondPart = RandomStringUtils.randomAlphabetic(partsLength);
        String fieldValue = firstPart + " " + secondPart;
        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenReturn(getEmbedTemplateWithTooLongField(fieldValue));
        when(gson.fromJson(getSingleFieldWithValue(fieldValue), EmbedConfiguration.class)).thenReturn(getEmbedWithSingleFieldOfValue(fieldValue));
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        MessageEmbed firstEmbed = messageToSend.getEmbeds().get(0);
        Assert.assertEquals(2, firstEmbed.getFields().size());
        Assert.assertEquals(firstPart, firstEmbed.getFields().get(0).getValue());
        Assert.assertEquals(secondPart, firstEmbed.getFields().get(1).getValue());
    }

    @Test
    public void testDescriptionWithOneSpace() throws IOException, TemplateException {
        setupServerAware();
        int partLengths = 1024;
        String firstPart = RandomStringUtils.randomAlphabetic(partLengths);
        String secondPart = RandomStringUtils.randomAlphabetic(partLengths);
        String descriptionText = firstPart + " " + secondPart;
        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenReturn(getEmbedTemplateWithDescription(descriptionText));
        when(configuration.getTemplate(EMBED_PAGE_COUNT_TEMPLATE, null, SERVER_ID, null, true, false)).thenReturn(getPageCountTemplate(1));
        when(gson.fromJson(embedTemplateWithDescription(descriptionText), EmbedConfiguration.class)).thenReturn(embedConfigWithDescription(descriptionText));
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        Assert.assertEquals(2, messageToSend.getEmbeds().size());
        MessageEmbed firstEmbed = messageToSend.getEmbeds().get(0);
        Assert.assertEquals(partLengths, firstEmbed.getDescription().length());
        Assert.assertEquals(firstPart, firstEmbed.getDescription());
        MessageEmbed secondEmbed = messageToSend.getEmbeds().get(1);
        Assert.assertEquals(partLengths + 1, secondEmbed.getDescription().length());
        Assert.assertEquals(" " + secondPart, secondEmbed.getDescription());
    }

    @Test
    public void testDescriptionWithTwoSpacesAndLongChunks() throws IOException, TemplateException {
        setupServerAware();
        int partLengths = 1024;
        String firstPart = RandomStringUtils.randomAlphabetic(partLengths);
        String secondPart = RandomStringUtils.randomAlphabetic(partLengths);
        String thirdPart = RandomStringUtils.randomAlphabetic(partLengths);
        String descriptionText = firstPart + " " + secondPart + " " + thirdPart;
        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenReturn(getEmbedTemplateWithDescription(descriptionText));
        when(configuration.getTemplate(EMBED_PAGE_COUNT_TEMPLATE, null, SERVER_ID, null, true, false)).thenReturn(getPageCountTemplate(1));
        when(gson.fromJson(embedTemplateWithDescription(descriptionText), EmbedConfiguration.class)).thenReturn(embedConfigWithDescription(descriptionText));
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        Assert.assertEquals(3, messageToSend.getEmbeds().size());
        MessageEmbed firstEmbed = messageToSend.getEmbeds().get(0);
        Assert.assertEquals(partLengths, firstEmbed.getDescription().length());
        Assert.assertEquals(firstPart, firstEmbed.getDescription());
        MessageEmbed secondEmbed = messageToSend.getEmbeds().get(1);
        Assert.assertEquals(partLengths + 1, secondEmbed.getDescription().length());
        Assert.assertEquals(" " + secondPart, secondEmbed.getDescription());
        MessageEmbed thirdEmbed = messageToSend.getEmbeds().get(2);
        Assert.assertEquals(partLengths + 1, thirdEmbed.getDescription().length());
        Assert.assertEquals(" " + thirdPart, thirdEmbed.getDescription());
    }

    @Test
    public void testDescriptionWithMultipleSpacesSplitIntoTwo() throws IOException, TemplateException {
        setupServerAware();
        int partLengths = 750;
        String firstPart = RandomStringUtils.randomAlphabetic(partLengths);
        String secondPart = RandomStringUtils.randomAlphabetic(partLengths);
        String thirdPart = RandomStringUtils.randomAlphabetic(partLengths);
        String descriptionText = firstPart + " " + secondPart + " " + thirdPart;
        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenReturn(getEmbedTemplateWithDescription(descriptionText));
        when(configuration.getTemplate(EMBED_PAGE_COUNT_TEMPLATE, null, SERVER_ID, null, true, false)).thenReturn(getPageCountTemplate(1));
        when(gson.fromJson(embedTemplateWithDescription(descriptionText), EmbedConfiguration.class)).thenReturn(embedConfigWithDescription(descriptionText));
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        Assert.assertEquals(2, messageToSend.getEmbeds().size());
        MessageEmbed firstEmbed = messageToSend.getEmbeds().get(0);
        Assert.assertEquals(partLengths + partLengths + 1, firstEmbed.getDescription().length());
        Assert.assertEquals(firstPart + " " + secondPart, firstEmbed.getDescription());
        MessageEmbed secondEmbed = messageToSend.getEmbeds().get(1);
        Assert.assertEquals(1 + partLengths, secondEmbed.getDescription().length());
        Assert.assertEquals(" " + thirdPart, secondEmbed.getDescription());
    }

    @Test
    public void testFieldLengthTooLongForEmbed()  throws IOException, TemplateException {
        setupServerAware();
        int partLengths = 1000;
        String fieldValue = RandomStringUtils.randomAlphabetic(partLengths);
        String firstField = fieldValue + "a";
        String secondField = fieldValue + "b";
        String thirdField = fieldValue + "c";
        String fourthField = fieldValue + "d";
        String fifthField = fieldValue + "e";
        String sixthField = fieldValue + "f";
        List<String> fieldValues = Arrays.asList(firstField, secondField, thirdField, fourthField, fifthField, sixthField);

        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenReturn(getEmbedTemplateWithFieldValues(fieldValues));
        when(configuration.getTemplate(EMBED_PAGE_COUNT_TEMPLATE, null, SERVER_ID, null, true, false)).thenReturn(getPageCountTemplate(1));
        when(gson.fromJson(getFields(fieldValues), EmbedConfiguration.class)).thenReturn(getEmbedWithFields(fieldValues));
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        Assert.assertEquals(2, messageToSend.getEmbeds().size());
        MessageEmbed firstEmbed = messageToSend.getEmbeds().get(0);
        List<MessageEmbed.Field> firstFields = firstEmbed.getFields();
        Assert.assertEquals(5, firstFields.size());
        Assert.assertEquals(firstField, firstFields.get(0).getValue());
        Assert.assertEquals(secondField, firstFields.get(1).getValue());
        Assert.assertEquals(thirdField, firstFields.get(2).getValue());
        Assert.assertEquals(fourthField, firstFields.get(3).getValue());
        Assert.assertEquals(fifthField, firstFields.get(4).getValue());
        MessageEmbed secondEmbed = messageToSend.getEmbeds().get(1);
        List<MessageEmbed.Field> secondFields = secondEmbed.getFields();
        Assert.assertEquals(1, secondFields.size());
        Assert.assertEquals(sixthField, secondFields.get(0).getValue());
    }

    @Test(expected = TemplatingException.class)
    public void tryToRenderMissingTemplate() throws IOException {
        setupServerAware();
        when(configuration.getTemplate(TEMPLATE_KEY, null, SERVER_ID, null, true, false)).thenThrow(new TemplateNotFoundException(TEMPLATE_KEY, new Object(), ""));
        templateServiceBean.renderSimpleTemplate(TEMPLATE_KEY);
    }

    @Test(expected = TemplatingException.class)
    public void tryToRenderMissingEmbedTemplate() throws IOException {
        setupServerAware();
        when(configuration.getTemplate(getEmbedTemplateKey(), null, SERVER_ID, null, true, false)).thenThrow(new TemplateNotFoundException(TEMPLATE_KEY, new Object(), ""));
        templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new Object());
    }

    @Test(expected = TemplatingException.class)
    public void tryToRenderMissingTemplateWithMap() throws IOException {
        setupServerAware();
        when(configuration.getTemplate(TEMPLATE_KEY, null, SERVER_ID, null, true, false)).thenThrow(new TemplateNotFoundException(TEMPLATE_KEY, new Object(), ""));
        templateServiceBean.renderTemplateWithMap(TEMPLATE_KEY, new HashMap<>());
    }

    private EmbedConfiguration embedConfigWithDescription(String descriptionText) {
        return EmbedConfiguration.builder().description(descriptionText).build();
    }

    private EmbedConfiguration getEmbedWithSingleFieldOfValue(String value) {
        List<EmbedField> fields = new ArrayList<>();
        fields.add(EmbedField.builder().name("name").value(value).build());
        return EmbedConfiguration.builder().fields(fields).build();
    }

    private String getEmbedTemplateKey() {
        return TEMPLATE_KEY + "_embed";
    }

    private EmbedConfiguration getEmbedWithFields(List<String> fieldValues) {
        List<EmbedField> fields = new ArrayList<>();
        fieldValues.forEach(s -> {
            fields.add(EmbedField.builder().name("name").value(s).build());
        });
        return EmbedConfiguration.builder().fields(fields).build();
    }

    private EmbedConfiguration getTooManyFieldsEmbedConfiguration() {
        List<EmbedField> fields = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            EmbedField field = EmbedField
                    .builder()
                    .value(FIELD_VALUE)
                    .inline(true)
                    .name("name")
                    .build();
            fields.add(field);
        }
        return EmbedConfiguration.builder().fields(fields).build();
    }

    private Templatable getTemplatableWithSimpleTemplate() {
        return new Templatable() {
            @Override
            public String getTemplateName() {
                return TEMPLATE_KEY;
            }

            @Override
            public Object getTemplateModel() {
                return new Object();
            }
        };
    }

    private EmbedConfiguration getFullEmbedConfiguration() {

        EmbedAuthor author = EmbedAuthor
                .builder()
                .avatar(EXAMPLE_URL)
                .name("name")
                .url(EXAMPLE_URL)
                .build();

        EmbedColor color = EmbedColor
                .builder()
                .r(255)
                .g(255)
                .b(255)
                .build();

        EmbedField field = EmbedField
                .builder()
                .value(FIELD_VALUE)
                .inline(true)
                .name("name")
                .build();

        EmbedFooter footer = EmbedFooter
                .builder()
                .icon(EXAMPLE_URL)
                .text("text")
                .build();

        EmbedTitle title = EmbedTitle
                .builder()
                .title("title")
                .url(EXAMPLE_URL)
                .build();
        return EmbedConfiguration
                .builder()
                .fields(Arrays.asList(field))
                .footer(footer)
                .author(author)
                .title(title)
                .color(color)
                .description("description")
                .additionalMessage("additionalMessage")
                .imageUrl(EXAMPLE_URL)
                .thumbnail(EXAMPLE_URL)
                .build();
    }

    private Template getSimpleTemplate() throws IOException, TemplateException {
        return new Template(TEMPLATE_KEY, SIMPLE_TEMPLATE_SOURCE, getNonMockedConfiguration());
    }

    private Template getEmbedTemplateWithDescription(String description) throws IOException, TemplateException {
        return new Template(getEmbedTemplateKey(), embedTemplateWithDescription(description), getNonMockedConfiguration());
    }

    private Template getEmbedTemplateWithFieldValues(List<String> fieldValues) throws IOException, TemplateException {
        return new Template(getEmbedTemplateKey(), getFields(fieldValues), getNonMockedConfiguration());
    }

    private Template getPageCountTemplate(Integer page) throws IOException, TemplateException {
        return new Template(EMBED_PAGE_COUNT_TEMPLATE, getEmbedPageCount(page), getNonMockedConfiguration());
    }

    private Template getEmbedTemplateWithFallFieldsUsedOnce() throws IOException, TemplateException {
        return new Template(getEmbedTemplateKey(), getFullEmbedConfigString(), getNonMockedConfiguration());
    }

    private Template getEmbedTemplateWithFieldCount(Integer count) throws IOException, TemplateException {
        return new Template(getEmbedTemplateKey(), getFieldsEmbedConfigAsString(count), getNonMockedConfiguration());
    }

    private Template getEmbedTemplateWithTooLongField(String value) throws IOException, TemplateException {
        return new Template(getEmbedTemplateKey(), getSingleFieldWithValue(value), getNonMockedConfiguration());
    }

    private String getFullEmbedConfigString() {
        return IOUtils.toString(this.getClass().getResourceAsStream("/src/test/resources/full_embed.json"), StandardCharsets.UTF_8);
    }

    private String getFieldsEmbedConfigAsString(Integer count) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"fields\": [");
        for (int i = 0; i < count - 1; i++) {
            sb.append(FIELD_TEMPLATE + ",");
        }
        sb.append(FIELD_TEMPLATE +
                "]\n" +
                "}");
        return sb.toString();
    }

    private String getFields(List<String> fieldValues) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"fields\": [");
        for (String fieldValue: fieldValues) {
            sb.append(getSingleFieldWithValue(fieldValue));
        }
        sb.append("]\n" +
                "}");
        return sb.toString();
    }

    private String getSingleFieldWithValue(String value) {
        return String.format("{\"fields\": [" + FIELD_TEMPLATE_WITH_VALUE + "]\n}",value);
    }

    private String embedTemplateWithDescription(String description) {
        return String.format("{ \"description\": \"%s\"}", description);
    }

    private String getEmbedPageCount(Integer page) {
        return String.format("Page %d", page);
    }

    private Configuration getNonMockedConfiguration() throws IOException, TemplateException {
        return new FreeMarkerConfigurationFactory().createConfiguration();
    }

}

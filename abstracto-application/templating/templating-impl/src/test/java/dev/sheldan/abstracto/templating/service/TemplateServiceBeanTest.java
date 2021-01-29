package dev.sheldan.abstracto.templating.service;

import com.google.gson.Gson;
import dev.sheldan.abstracto.templating.Templatable;
import dev.sheldan.abstracto.templating.exception.TemplatingException;
import dev.sheldan.abstracto.templating.model.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
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
    private Gson gson;

    @Autowired
    private Gson actualGsonInstance;

    private static final String SIMPLE_TEMPLATE_SOURCE = "source";
    private static final String TEMPLATE_KEY = "template";

    @Test
    public void testSimpleTemplate() throws IOException, TemplateException {
        when(configuration.getTemplate(TEMPLATE_KEY)).thenReturn(getSimpleTemplate());
        String rendered = templateServiceBean.renderSimpleTemplate(TEMPLATE_KEY);
        Assert.assertEquals(SIMPLE_TEMPLATE_SOURCE, rendered);
    }

    @Test
    public void renderTemplatable() throws IOException, TemplateException {
        when(configuration.getTemplate(TEMPLATE_KEY)).thenReturn(getSimpleTemplate());
        Templatable templatable = getTemplatableWithSimpleTemplate();
        String rendered = templateServiceBean.renderTemplatable(templatable);
        Assert.assertEquals(SIMPLE_TEMPLATE_SOURCE, rendered);
    }

    @Test
    public void testTemplateWithMapParameter() throws IOException, TemplateException {
        when(configuration.getTemplate(TEMPLATE_KEY)).thenReturn(getSimpleTemplate());
        String rendered = templateServiceBean.renderTemplateWithMap(TEMPLATE_KEY, new HashMap<>());
        Assert.assertEquals(SIMPLE_TEMPLATE_SOURCE, rendered);
    }

    @Test
    public void testEmbedWithDescription() throws IOException, TemplateException {
        String descriptionText = "test";
        String fullEmbedTemplateKey = getEmbedTemplateKey();
        when(configuration.getTemplate(fullEmbedTemplateKey)).thenReturn(getEmbedTemplateWithDescription(descriptionText));
        when(gson.fromJson(embedTemplateWithDescription(descriptionText), EmbedConfiguration.class)).thenReturn(embedConfigWithDescription(descriptionText));
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        Assert.assertEquals(descriptionText, messageToSend.getEmbeds().get(0).getDescription());
    }

    @Test
    public void testEmbedWithAllUsableAttributes() throws IOException, TemplateException {
        when(configuration.getTemplate(getEmbedTemplateKey())).thenReturn(getEmbedTemplateWithFallFieldsUsedOnce());
        when(gson.fromJson(getFullEmbedConfigString(), EmbedConfiguration.class)).thenReturn(getFullEmbedConfiguration());
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        Assert.assertEquals(messageToSend.getMessage(), "additionalMessage");
        MessageEmbed onlyEmbed = messageToSend.getEmbeds().get(0);
        Assert.assertEquals(onlyEmbed.getAuthor().getIconUrl(), EXAMPLE_URL);
        Assert.assertEquals(onlyEmbed.getAuthor().getName(), "name");
        Assert.assertEquals(onlyEmbed.getDescription(), "description");
        MessageEmbed.Field onlyField = onlyEmbed.getFields().get(0);
        Assert.assertEquals(onlyField.getName(), "name");
        Assert.assertEquals(onlyField.getValue(), FIELD_VALUE);
        Assert.assertTrue(onlyField.isInline());
        Color color = onlyEmbed.getColor();
        Assert.assertEquals(color.getBlue(), 255);
        Assert.assertEquals(color.getRed(), 255);
        Assert.assertEquals(color.getGreen(), 255);
        Assert.assertEquals(onlyEmbed.getUrl(), EXAMPLE_URL);
        Assert.assertEquals(onlyEmbed.getFooter().getText(), "text");
        Assert.assertEquals(onlyEmbed.getFooter().getIconUrl(), EXAMPLE_URL);
        Assert.assertEquals(onlyEmbed.getImage().getUrl(), EXAMPLE_URL);
        Assert.assertEquals(onlyEmbed.getThumbnail().getUrl(), EXAMPLE_URL);
        Assert.assertEquals(1, messageToSend.getEmbeds().size());
    }

    @Test
    public void testEmbedWithTooLongDescription() throws IOException, TemplateException {
        int tooMuchCharacterCount = 1024;
        String descriptionText = RandomStringUtils.randomAlphabetic(MessageEmbed.TEXT_MAX_LENGTH + tooMuchCharacterCount);
        when(configuration.getTemplate(getEmbedTemplateKey())).thenReturn(getEmbedTemplateWithDescription(descriptionText));
        when(configuration.getTemplate(EMBED_PAGE_COUNT_TEMPLATE)).thenReturn(getPageCountTemplate(1));
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
        int totalFieldCount = 30;
        when(configuration.getTemplate(getEmbedTemplateKey())).thenReturn(getEmbedTemplateWithFieldCount(totalFieldCount));
        when(configuration.getTemplate(EMBED_PAGE_COUNT_TEMPLATE)).thenReturn(getPageCountTemplate(1));
        when(gson.fromJson(getFieldsEmbedConfigAsString(totalFieldCount), EmbedConfiguration.class)).thenReturn(getTooManyFieldsEmbedConfiguration());
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        MessageEmbed firstEmbed = messageToSend.getEmbeds().get(0);
        Assert.assertEquals(25, firstEmbed.getFields().size());
        MessageEmbed secondEmbed = messageToSend.getEmbeds().get(1);
        Assert.assertEquals(totalFieldCount % 25, secondEmbed.getFields().size());
    }

    @Test
    public void testEmbedWithTooLongField() throws IOException, TemplateException {
        String fieldValue = RandomStringUtils.randomAlphabetic(1500);
        when(configuration.getTemplate(getEmbedTemplateKey())).thenReturn(getEmbedTemplateWithTooLongField(fieldValue));
        when(gson.fromJson(getSingleFieldWithValue(fieldValue), EmbedConfiguration.class)).thenReturn(getEmbedWithSingleFieldOfValue(fieldValue));
        MessageToSend messageToSend = templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new HashMap<>());
        MessageEmbed firstEmbed = messageToSend.getEmbeds().get(0);
        Assert.assertEquals(2, firstEmbed.getFields().size());
        Assert.assertEquals(fieldValue.substring(0, MessageEmbed.VALUE_MAX_LENGTH), firstEmbed.getFields().get(0).getValue());
        Assert.assertEquals(fieldValue.substring(MessageEmbed.VALUE_MAX_LENGTH), firstEmbed.getFields().get(1).getValue());
    }

    @Test(expected = TemplatingException.class)
    public void tryToRenderMissingTemplate() throws IOException {
        when(configuration.getTemplate(TEMPLATE_KEY)).thenThrow(new TemplateNotFoundException(TEMPLATE_KEY, new Object(), ""));
        templateServiceBean.renderSimpleTemplate(TEMPLATE_KEY);
    }

    @Test(expected = TemplatingException.class)
    public void tryToRenderMissingEmbedTemplate() throws IOException {
        when(configuration.getTemplate(getEmbedTemplateKey())).thenThrow(new TemplateNotFoundException(TEMPLATE_KEY, new Object(), ""));
        templateServiceBean.renderEmbedTemplate(TEMPLATE_KEY, new Object());
    }

    @NotNull
    private String getEmbedTemplateKey() {
        return TEMPLATE_KEY + "_embed";
    }

    @Test(expected = TemplatingException.class)
    public void tryToRenderMissingTemplateWithMap() throws IOException {
        when(configuration.getTemplate(TEMPLATE_KEY)).thenThrow(new TemplateNotFoundException(TEMPLATE_KEY, new Object(), ""));
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

    private String getFullEmbedConfigString() throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream("/full_embed.json"), StandardCharsets.UTF_8);
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

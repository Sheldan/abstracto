package dev.sheldan.abstracto.templating.methods;

import dev.sheldan.abstracto.templating.service.TemplateService;
import freemarker.template.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SafeFieldIterationsTest {

    public static final String TEMPLATE_KEY = "template";
    public static final String FIELD_NAME_TEMPLATE = "fieldName";
    public static final String FIELD_NAME_VALUE = "fieldName";
    public static final String EXPECTED_START_PART = "{ \"name\": \"" + FIELD_NAME_VALUE + "\", \"inline\": \"true\", \"value\": \"";
    public static final String INLINE_VALUE = "true";
    public static final String FIRST_LIST_ENTRY = "text";
    public static final String SIX_HUNDRED_CHARACTERS = RandomStringUtils.randomAlphabetic(600);

    @InjectMocks
    private SafeFieldIterations safeFieldIterations;

    @Mock
    private TemplateService templateService;

    @Captor
    private ArgumentCaptor<String> templateKeyCaptor;


    @Before
    public void setup() {
        when(templateService.renderTemplateWithMap(eq(FIELD_NAME_TEMPLATE), any())).thenReturn(FIELD_NAME_VALUE);
    }

    @Test
    public void testEmptyList() throws TemplateModelException {
        String resultingValue = (String) safeFieldIterations.exec(getSimpleParameters());
        verify(templateService, times(1)).renderTemplateWithMap(templateKeyCaptor.capture(), any());
        List<String> usedTemplateKeys = templateKeyCaptor.getAllValues();
        assertEquals(FIELD_NAME_TEMPLATE, usedTemplateKeys.get(0));
        assertEquals(EXPECTED_START_PART + "\"}", resultingValue);
    }

    @Test
    public void testOneElement() throws TemplateModelException {
        when(templateService.renderTemplateWithMap(eq(TEMPLATE_KEY), any())).thenReturn(FIRST_LIST_ENTRY);
        String resultingValue = (String) safeFieldIterations.exec(oneListEntryParameter());
        assertEquals(EXPECTED_START_PART + FIRST_LIST_ENTRY + "\"}", resultingValue);
    }

    @Test
    public void testTwoElements() throws TemplateModelException {
        when(templateService.renderTemplateWithMap(eq(TEMPLATE_KEY), any())).thenReturn(FIRST_LIST_ENTRY);
        String resultingValue = (String) safeFieldIterations.exec(twoListEntryParameter());
        assertEquals(EXPECTED_START_PART  + FIRST_LIST_ENTRY + FIRST_LIST_ENTRY + "\"}", resultingValue);
    }

    @Test
    public void testElementsStaySolo() throws TemplateModelException {
        when(templateService.renderTemplateWithMap(eq(TEMPLATE_KEY), any())).thenReturn(SIX_HUNDRED_CHARACTERS);
        String resultingValue = (String) safeFieldIterations.exec(twoListEntryParameter());
        assertEquals(EXPECTED_START_PART  + SIX_HUNDRED_CHARACTERS + "\"}," + EXPECTED_START_PART  + SIX_HUNDRED_CHARACTERS + "\"}", resultingValue);
    }

    @Test(expected = TemplateModelException.class)
    public void testTooLittleParameters() throws TemplateModelException {
        safeFieldIterations.exec(Arrays.asList(""));
    }

    @Test(expected = TemplateModelException.class)
    public void testWrongListAdapterType() throws TemplateModelException {
        safeFieldIterations.exec(wrongListAdapter());
    }

    @Test(expected = TemplateModelException.class)
    public void testWrongTemplateKeyParameterType() throws TemplateModelException {
        safeFieldIterations.exec(wrongTemplateKeyParameterType());
    }

    @Test(expected = TemplateModelException.class)
    public void testWrongFieldNameTemplateKeyParameterType() throws TemplateModelException {
        safeFieldIterations.exec(wrongFieldNameTemplateKeyParameterType());
    }

    @Test(expected = TemplateModelException.class)
    public void testWrongInlineParameterType() throws TemplateModelException {
        safeFieldIterations.exec(wrongInLineValueParameterType());
    }


    public List<Object> wrongListAdapter() {
        return Arrays.asList(new Object(), new Object(), new SimpleScalar(FIELD_NAME_TEMPLATE), new SimpleScalar(INLINE_VALUE));
    }

    public List<Object> wrongTemplateKeyParameterType() {
        return Arrays.asList(validEmptyList(), new Object(), new SimpleScalar(FIELD_NAME_TEMPLATE), new SimpleScalar(INLINE_VALUE));
    }

    public List<Object> wrongFieldNameTemplateKeyParameterType() {
        return Arrays.asList(validEmptyList(), new SimpleScalar(TEMPLATE_KEY), new Object(), new SimpleScalar(INLINE_VALUE));
    }

    public List<Object> wrongInLineValueParameterType() {
        return Arrays.asList(validEmptyList(), new SimpleScalar(TEMPLATE_KEY), new SimpleScalar(FIELD_NAME_TEMPLATE), new Object());
    }

    public List<Object> getSimpleParameters() {
        return Arrays.asList(validEmptyList(), new SimpleScalar(TEMPLATE_KEY), new SimpleScalar(FIELD_NAME_TEMPLATE), new SimpleScalar(INLINE_VALUE));
    }

    private DefaultListAdapter validEmptyList() {
        return DefaultListAdapter.adapt(new ArrayList<Object>(), getWrapper());
    }

    public List<Object> oneListEntryParameter() {
        return Arrays.asList(DefaultListAdapter.adapt(Arrays.asList("testing"), getWrapper()), new SimpleScalar(TEMPLATE_KEY), new SimpleScalar(FIELD_NAME_TEMPLATE), new SimpleScalar(INLINE_VALUE));
    }

    public List<Object> twoListEntryParameter() {
        return Arrays.asList(DefaultListAdapter.adapt(Arrays.asList("testing", "otherText"), getWrapper()), new SimpleScalar(TEMPLATE_KEY), new SimpleScalar(FIELD_NAME_TEMPLATE), new SimpleScalar(INLINE_VALUE));
    }

    private DefaultObjectWrapper getWrapper() {
        return new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_0).build();
    }
}

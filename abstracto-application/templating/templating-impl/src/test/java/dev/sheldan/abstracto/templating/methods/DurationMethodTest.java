package dev.sheldan.abstracto.templating.methods;

import dev.sheldan.abstracto.templating.service.TemplateService;
import freemarker.ext.beans.StringModel;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.TemplateModelException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DurationMethodTest {
    public static final long SECOND_AMOUNT = 10L;
    public static final long MINUTES_AMOUNT = 2L;
    public static final long HOURS_AMOUNT = 3L;
    public static final long DAYS_AMOUNT = 5L;
    public static final String DURATION_TEMPLATE = "duration_formatting";
    public static final String SECONDS = "seconds";
    public static final String HOURS = "hours";
    public static final String MINUTES = "minutes";
    public static final String DAYS = "days";

    @InjectMocks
    private DurationMethod durationMethod;

    @Mock
    private TemplateService templateService;


    @Test
    public void testSeconds() throws TemplateModelException {
        durationMethod.exec(getSecondParameters());
        verify(templateService, times(1)).renderTemplateWithMap(DURATION_TEMPLATE, getHashMap(0, 0, 0, SECOND_AMOUNT));
    }

    @Test
    public void testMinutes() throws TemplateModelException {
        durationMethod.exec(getMinuteParameter());
        verify(templateService, times(1)).renderTemplateWithMap(DURATION_TEMPLATE, getHashMap(0, 0, MINUTES_AMOUNT, 0));
    }

    @Test
    public void testHours() throws TemplateModelException {
        durationMethod.exec(getHourParameter());
        verify(templateService, times(1)).renderTemplateWithMap(DURATION_TEMPLATE, getHashMap(0, HOURS_AMOUNT, 0, 0));
    }


    @Test
    public void testDays() throws TemplateModelException {
        durationMethod.exec(getDayParameter());
        verify(templateService, times(1)).renderTemplateWithMap(DURATION_TEMPLATE, getHashMap(DAYS_AMOUNT, 0, 0, 0));
    }

    @Test
    public void testAllTime() throws TemplateModelException {
        durationMethod.exec(getMixedParameter());
        verify(templateService, times(1)).renderTemplateWithMap(DURATION_TEMPLATE, getHashMap(DAYS_AMOUNT, HOURS_AMOUNT, MINUTES_AMOUNT, SECOND_AMOUNT));
    }

    @Test(expected = TemplateModelException.class)
    public void testNoParamGiven() throws TemplateModelException {
        durationMethod.exec(Collections.emptyList());
    }

    @Test(expected = TemplateModelException.class)
    public void testNoDurationObject() throws TemplateModelException {
        durationMethod.exec(Arrays.asList(new StringModel("", getWrapper())));
    }

    @Test(expected = TemplateModelException.class)
    public void testNoStringModelObject() throws TemplateModelException {
        durationMethod.exec(Arrays.asList(""));
    }


    private List<Object> getSecondParameters() {
        return Arrays.asList(new StringModel(Duration.ofSeconds(SECOND_AMOUNT), getWrapper()));
    }

    private List<Object> getMinuteParameter() {
        return Arrays.asList(new StringModel(Duration.ofMinutes(MINUTES_AMOUNT), getWrapper()));
    }

    private List<Object> getHourParameter() {
        return Arrays.asList(new StringModel(Duration.ofHours(HOURS_AMOUNT), getWrapper()));
    }

    private List<Object> getDayParameter() {
        return Arrays.asList(new StringModel(Duration.ofDays(DAYS_AMOUNT), getWrapper()));
    }

    private List<Object> getMixedParameter() {
        return Arrays.asList(new StringModel(Duration.ofSeconds(SECOND_AMOUNT)
                .plus(Duration.ofMinutes(MINUTES_AMOUNT))
                .plus(Duration.ofHours(HOURS_AMOUNT))
                .plus(Duration.ofDays(DAYS_AMOUNT)), getWrapper()));
    }

    private DefaultObjectWrapper getWrapper() {
        return new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_0).build();
    }

    private HashMap<String, Object> getHashMap(long days, long hours, long minutes, long seconds) {
        HashMap<String, Object> map = new HashMap<>();
        map.put(DAYS, days);
        map.put(HOURS, hours);
        map.put(MINUTES, minutes);
        map.put(SECONDS, seconds);
        return map;
    }

}

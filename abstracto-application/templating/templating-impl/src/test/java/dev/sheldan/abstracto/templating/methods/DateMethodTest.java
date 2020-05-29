package dev.sheldan.abstracto.templating.methods;

import freemarker.ext.beans.StringModel;
import freemarker.template.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class DateMethodTest {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final Instant DATE = Instant.ofEpochSecond(1590615937);

    @InjectMocks
    private DateMethod dateMethod;

    @Test
    public void testInstantFormat() throws TemplateModelException {
        String exec = (String) dateMethod.exec(getCorrectParametersInstant());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)
                .withZone(ZoneId.systemDefault());
        Assert.assertEquals(exec, formatter.format(DATE));
    }

    @Test
    public void testOffsetDateTimeObject() throws TemplateModelException {
        String exec = (String) dateMethod.exec(getCorrectParametersOffsetDateTime());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)
                .withZone(ZoneId.systemDefault());
        Assert.assertEquals(exec, formatter.format(DATE));
    }

    @Test(expected = TemplateModelException.class)
    public void incorrectParameterCount() throws TemplateModelException {
        dateMethod.exec(new ArrayList<Object>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectDateFormat() throws TemplateModelException {
        dateMethod.exec(getIncorrectDateFormat());
    }

    @Test(expected = TemplateModelException.class)
    public void incorrectPassedObject() throws TemplateModelException {
        dateMethod.exec(getIncorrectTimeParameter());
    }

    private List<Object> getIncorrectDateFormat() {
        List<Object> params = new ArrayList<>();
        params.add(getInstantDateObject());
        params.add(incorrectDateFormat());
        return params;
    }

    private List<Object> getIncorrectTimeParameter() {
        List<Object> params = new ArrayList<>();
        params.add(getNotCompatibleObject());
        params.add(incorrectDateFormat());
        return params;
    }


    private List<Object> getCorrectParametersInstant() {
        List<Object> params = new ArrayList<>();
        params.add(getInstantDateObject());
        params.add(simpleDateFormat());
        return params;
    }

    private List<Object> getCorrectParametersOffsetDateTime() {
        List<Object> params = new ArrayList<>();
        params.add(getOffsetDateTimeObject());
        params.add(simpleDateFormat());
        return params;
    }

    private SimpleScalar simpleDateFormat() {
        return new SimpleScalar(DATE_TIME_FORMAT);
    }

    private SimpleScalar incorrectDateFormat() {
        return new SimpleScalar("INCORRECT");
    }

    private StringModel getInstantDateObject() {
        DefaultObjectWrapper wrapper = getWrapper();
        return new StringModel(DATE, wrapper);
    }

    private DefaultObjectWrapper getWrapper() {
        return new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_0).build();
    }

    private StringModel getOffsetDateTimeObject() {
        DefaultObjectWrapper wrapper = getWrapper();
        return new StringModel(OffsetDateTime.ofInstant(DATE, ZoneId.systemDefault()), wrapper);
    }

    private StringModel getNotCompatibleObject() {
        DefaultObjectWrapper wrapper = getWrapper();
        return new StringModel("", wrapper);
    }
}

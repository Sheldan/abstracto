package dev.sheldan.abstracto.templating.loading;

import dev.sheldan.abstracto.templating.model.database.Template;
import dev.sheldan.abstracto.templating.service.management.TemplateManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TemplateLoaderTest {

    public static final String TEST_CONTENT = "test";
    public static final String TEMPLATE_KEY = "key";

    @InjectMocks
    private DatabaseTemplateLoader loader;

    @Mock
    private TemplateManagementService templateManagementService;


    @Test
    public void testProperLoading() throws IOException {
        Template mocked = Template.builder().key(TEMPLATE_KEY).content(TEST_CONTENT).build();
        when(templateManagementService.getTemplateByKey(TEMPLATE_KEY)).thenReturn(Optional.of(mocked));
        Template templateSource =  (Template) loader.findTemplateSource(TEMPLATE_KEY);
        assertThat(TEST_CONTENT, equalTo(templateSource.getContent()));
    }

    @Test(expected = IOException.class)
    public void testMissingTemplate() throws IOException {
        loader.findTemplateSource(TEMPLATE_KEY);
    }

    @Test
    public void testReader() throws IOException {
        Template mocked = Template.builder().key(TEMPLATE_KEY).content(TEST_CONTENT).build();
        Reader reader = loader.getReader(mocked, null);
        char[] chars = new char[4];
        reader.read(chars, 0, 4);
        assertThat(TEST_CONTENT, equalTo(new String(chars)));
    }
}

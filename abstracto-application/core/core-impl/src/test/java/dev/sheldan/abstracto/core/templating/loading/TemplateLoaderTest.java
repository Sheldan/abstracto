package dev.sheldan.abstracto.core.templating.loading;

import dev.sheldan.abstracto.core.config.ServerContext;
import dev.sheldan.abstracto.core.templating.model.EffectiveTemplate;
import dev.sheldan.abstracto.core.templating.service.management.EffectiveTemplateManagementService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private EffectiveTemplateManagementService templateManagementService;

    @Mock
    private ServerContext serverContext;

    @Test
    public void testProperLoading() throws IOException {
        EffectiveTemplate mocked = Mockito.mock(EffectiveTemplate.class);
        when(mocked.getContent()).thenReturn(TEST_CONTENT);
        when(templateManagementService.getTemplateByKey(TEMPLATE_KEY)).thenReturn(Optional.of(mocked));
        EffectiveTemplate templateSource =  (EffectiveTemplate) loader.findTemplateSource(TEMPLATE_KEY);
        assertThat(TEST_CONTENT, equalTo(templateSource.getContent()));
    }

    @Test(expected = IOException.class)
    public void testMissingTemplate() throws IOException {
        loader.findTemplateSource(TEMPLATE_KEY);
    }

    @Test
    public void testReader() throws IOException {
        EffectiveTemplate template = Mockito.mock(EffectiveTemplate.class);
        when(template.getContent()).thenReturn(TEST_CONTENT);
        Reader reader = loader.getReader(template, null);
        char[] chars = new char[4];
        reader.read(chars, 0, 4);
        assertThat(TEST_CONTENT, equalTo(new String(chars)));
    }
}

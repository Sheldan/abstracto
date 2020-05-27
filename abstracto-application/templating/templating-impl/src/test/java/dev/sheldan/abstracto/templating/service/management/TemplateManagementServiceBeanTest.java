package dev.sheldan.abstracto.templating.service.management;

import dev.sheldan.abstracto.templating.model.database.Template;
import dev.sheldan.abstracto.templating.repository.TemplateRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TemplateManagementServiceBeanTest {

    @InjectMocks
    private TemplateManagementServiceBean templateManagementServiceBean;

    @Mock
    private TemplateRepository repository;

    private static final String TEMPLATE_KEY = "templateKey";
    private static final String TEMPLATE_SOURCE = "source";

    @Test
    public void testFindByKey() {
        when(repository.findById(TEMPLATE_KEY)).thenReturn(Optional.of(getTemplate()));
        Optional<Template> templateByKey = templateManagementServiceBean.getTemplateByKey(TEMPLATE_KEY);
        templateByKey.ifPresent(template -> {
            Assert.assertEquals(TEMPLATE_KEY, template.getKey());
            Assert.assertEquals(TEMPLATE_SOURCE, template.getContent());
        });
        Assert.assertTrue(templateByKey.isPresent());
    }

    @Test
    public void testNotFindTemplate() {
        when(repository.findById(TEMPLATE_KEY)).thenReturn(Optional.empty());
        Optional<Template> templateByKey = templateManagementServiceBean.getTemplateByKey(TEMPLATE_KEY);
        Assert.assertFalse(templateByKey.isPresent());
    }

    @Test
    public void testTemplateExists() {
        when(repository.existsById(TEMPLATE_KEY)).thenReturn(true);
        Assert.assertTrue(templateManagementServiceBean.templateExists(TEMPLATE_KEY));
    }

    @Test
    public void testCreateTemplate() {
        Template template = templateManagementServiceBean.createTemplate(TEMPLATE_KEY, TEMPLATE_SOURCE);
        Assert.assertEquals(template.getContent(), TEMPLATE_SOURCE);
        Assert.assertEquals(template.getKey(), TEMPLATE_KEY);
        verify(repository, times(1)).save(any(Template.class));
        Assert.assertTrue(Duration.between(template.getLastModified(), Instant.now()).getSeconds() < 1);
    }

    private Template getTemplate() {
        return Template.builder().content(TEMPLATE_SOURCE).key(TEMPLATE_KEY).build();
    }

}

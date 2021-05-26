package dev.sheldan.abstracto.core.templating.config;

import dev.sheldan.abstracto.core.templating.loading.DatabaseTemplateLoader;
import dev.sheldan.abstracto.core.templating.method.DateMethod;
import dev.sheldan.abstracto.core.templating.method.DurationMethod;
import dev.sheldan.abstracto.core.templating.model.database.AutoLoadMacro;
import dev.sheldan.abstracto.core.templating.service.management.AutoLoadMacroManagementService;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Configuration bean used to provide the {@link Configuration} bean to the spring context.
 */
@org.springframework.context.annotation.Configuration
public class FreemarkerConfiguration {

    @Autowired
    private DatabaseTemplateLoader templateLoader;

    @Autowired
    private DurationMethod durationMethod;

    @Autowired
    private DateMethod instantMethod;

    @Autowired
    private AutoLoadMacroManagementService macroManagementService;

    /**
     * Creates a {@link Configuration} bean with the appropriate configuration which includes:
     * The correct compatibility version and the provided formatter methods to be used in the templates.
     * The encoding of the templates is set to UTF-8.
     * @return A configured {@link Configuration} bean according to the configuration
     * @throws IOException when the configuration fails to be created
     * @throws TemplateException when the configuration fails to be created
     */
    @Bean
    public Configuration freeMarkerConfiguration() throws IOException, TemplateException {
        FreeMarkerConfigurationFactory factory = new FreeMarkerConfigurationFactory();
        factory.setPreTemplateLoaders(templateLoader);
        Configuration configuration = factory.createConfiguration();
        configuration.setSharedVariable("fmtDuration", durationMethod);
        configuration.setSharedVariable("formatDate", instantMethod);
        // 10 minutes template cache
        configuration.setTemplateUpdateDelayMilliseconds(600000);
        List<String> macrosToLoad = macroManagementService.loadAllMacros().stream()
                .map(AutoLoadMacro::getKey).collect(Collectors.toList());
        configuration.setAutoIncludes(macrosToLoad);
        configuration.setTemplateLookupStrategy(new AbstractoTemplateLookupStrategy());
        configuration.setEncoding(Locale.getDefault(), "utf-8");
        // needed to support default methods in interfaces
        configuration.setIncompatibleImprovements(Configuration.VERSION_2_3_29);
        return configuration;
    }
}

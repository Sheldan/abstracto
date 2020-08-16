package dev.sheldan.abstracto.templating.config;

import dev.sheldan.abstracto.templating.loading.DatabaseTemplateLoader;
import dev.sheldan.abstracto.templating.methods.DurationMethod;
import dev.sheldan.abstracto.templating.methods.DateMethod;
import dev.sheldan.abstracto.templating.methods.SafeFieldIterations;
import dev.sheldan.abstracto.templating.model.database.AutoLoadMacro;
import dev.sheldan.abstracto.templating.service.management.AutoLoadMacroManagementService;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;

import java.io.IOException;
import java.util.Arrays;
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
    private SafeFieldIterations safeFieldIterations;

    @Autowired
    private AutoLoadMacroManagementService macroManagementService;

    /**
     * Creates a {@link Configuration} bean with the appropriate configuration which includes:
     * The correct compatibility version and the provided formatter methods to be used in the templates.
     * The encoding of the templates is set to UTF-8.
     * @return A configured {@link Configuration} bean according to the configuration
     */
    @Bean
    public Configuration freeMarkerConfiguration() throws IOException, TemplateException {
        FreeMarkerConfigurationFactory factory = new FreeMarkerConfigurationFactory();
        factory.setPreTemplateLoaders(templateLoader);
        Configuration configuration = factory.createConfiguration();
        configuration.setSharedVariable("fmtDuration", durationMethod);
        configuration.setSharedVariable("formatDate", instantMethod);
        configuration.setSharedVariable("safeFieldLength", safeFieldIterations);
        List<String> macrosToLoad = macroManagementService.loadAllMacros().stream()
                .map(AutoLoadMacro::getKey).collect(Collectors.toList());
        configuration.setAutoIncludes(macrosToLoad);
        configuration.setEncoding(Locale.getDefault(), "utf-8");
        // needed to support default methods in interfaces
        configuration.setIncompatibleImprovements(Configuration.VERSION_2_3_29);
        return configuration;
    }
}

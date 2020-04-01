package dev.sheldan.abstracto.templating.config;

import dev.sheldan.abstracto.templating.loading.DatabaseTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactory;

import java.io.IOException;
import java.util.Locale;

@org.springframework.context.annotation.Configuration
public class FreemarkerConfiguration {

    @Autowired
    private DatabaseTemplateLoader templateLoader;

    @Bean
    public Configuration freeMarkerConfiguration() throws IOException, TemplateException {
        FreeMarkerConfigurationFactory factory = new FreeMarkerConfigurationFactory();
        factory.setPreTemplateLoaders(templateLoader);
        Configuration configuration = factory.createConfiguration();
        configuration.setEncoding(Locale.getDefault(), "utf-8");
        // needed to support default methods in interfaces
        configuration.setIncompatibleImprovements(Configuration.VERSION_2_3_29);
        return configuration;
    }
}

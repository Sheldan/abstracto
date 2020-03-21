package dev.sheldan.abstracto;

import dev.sheldan.abstracto.core.service.Startup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { FreeMarkerAutoConfiguration.class })
@ComponentScan(basePackages = {"dev.sheldan.abstracto"})
@EnableCaching
@EnableAsync
@EnableTransactionManagement
public class Application implements CommandLineRunner {

    @Autowired
    private Startup startup;


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        startup.startBot();
    }

}

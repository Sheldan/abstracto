package dev.sheldan.abstracto;

import dev.sheldan.abstracto.service.StartupManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "dev.sheldan.abstracto")
@EnableCaching
public class Application implements CommandLineRunner {

    @Autowired
    private StartupManager startup;


    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        startup.startBot();
    }

}

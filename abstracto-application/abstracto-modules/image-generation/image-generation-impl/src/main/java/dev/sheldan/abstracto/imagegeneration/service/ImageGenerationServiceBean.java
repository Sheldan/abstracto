package dev.sheldan.abstracto.imagegeneration.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.service.HttpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;


@Component
public class ImageGenerationServiceBean implements ImageGenerationService {

    @Value("${abstracto.feature.imagegeneration.triggered.url}")
    private String triggeredUrl;

    @Value("${abstracto.feature.imagegeneration.pat.url}")
    private String patUrl;

    @Autowired
    private HttpService httpService;

    @Override
    public File getTriggeredGif(String imageUrl) {
        try {
            return httpService.downloadFileToTempFile(triggeredUrl.replace("{1}", imageUrl));
        } catch (IOException e) {
            throw new AbstractoRunTimeException(String.format("Failed to download triggered gif for url %s with error %s", imageUrl, e.getMessage()));
        }
    }

    @Override
    public File getPatGif(String imageUrl) {
        try {
            return httpService.downloadFileToTempFile(patUrl.replace("{1}", imageUrl));
        } catch (IOException e) {
            throw new AbstractoRunTimeException(String.format("Failed to download pat gif for url %s with error %s", imageUrl, e.getMessage()));
        }
    }

}

package dev.sheldan.abstracto.imagegeneration.service;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.core.exception.RequestException;
import dev.sheldan.abstracto.core.service.HttpService;
import dev.sheldan.abstracto.imagegeneration.exception.AmongusTextRequestException;
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

    @Value("${abstracto.feature.imagegeneration.bonk.url}")
    private String bonkUrl;

    @Value("${abstracto.feature.imagegeneration.amongusText.url}")
    private String amongusTextUrl;

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

    @Override
    public File getBonkGif(String imageUrl) {
        try {
            return httpService.downloadFileToTempFile(bonkUrl.replace("{1}", imageUrl));
        } catch (IOException e) {
            throw new AbstractoRunTimeException(String.format("Failed to download bonk gif for url %s with error %s", imageUrl, e.getMessage()));
        }
    }

    @Override
    public File getAmongusTextImage(String text) {
        try {
            return httpService.downloadFileToTempFile(amongusTextUrl.replace("{1}", text));
        } catch (IOException e) {
            throw new AbstractoRunTimeException(String.format("Failed to download amongus text with error %s", text, e.getMessage()));
        } catch (RequestException exception) {
            throw new AmongusTextRequestException(text, exception.getErrorMessage());
        }
    }

}

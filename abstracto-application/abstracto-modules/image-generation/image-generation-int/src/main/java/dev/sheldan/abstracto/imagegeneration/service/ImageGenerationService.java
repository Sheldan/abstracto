package dev.sheldan.abstracto.imagegeneration.service;

import java.io.File;

public interface ImageGenerationService {
    File getTriggeredGif(String imageUrl);
    File getPatGif(String imageUrl);
    File getBonkGif(String imageUrl);
    File getAmongusTextImage(String text);
}

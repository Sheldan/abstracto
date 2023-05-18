package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FileConfig {
    private String fileName;
    private Boolean spoiler;
    // only used for plaintext files
    private String fileContent;
}

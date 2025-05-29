package dev.sheldan.abstracto.core.templating.model.messagecomponents;

import java.io.File;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.dv8tion.jda.api.utils.FileUpload;

@SuperBuilder
@Getter
public class FileConfig {
    private String fileName;
    // only used for plaintext files
    private String fileContent;
    @Builder.Default
    private Boolean spoiler = false;

    public FileUpload convertToFileUpload(File file) {
        FileUpload fileUpload = FileUpload.fromData(file, fileName);
        if(spoiler != null && spoiler) {
            fileUpload = fileUpload.asSpoiler();
        }
        return fileUpload;
    }
}

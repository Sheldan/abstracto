package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;

@Getter
@Setter
@Builder
public class AttachedFile {
    private File file;
    private String fileName;
    private boolean spoiler;

    public FileUpload convertToFileUpload() {
        FileUpload fileUpload = FileUpload.fromData(file, fileName);
        if(spoiler) {
            fileUpload = fileUpload.asSpoiler();
        }
        return fileUpload;
    }
}

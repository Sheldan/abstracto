package dev.sheldan.abstracto.core.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.utils.AttachmentOption;

import java.io.File;
import java.util.List;

@Getter
@Setter
@Builder
public class AttachedFile {
    private File file;
    private String fileName;
    private List<AttachmentOption> options;
}

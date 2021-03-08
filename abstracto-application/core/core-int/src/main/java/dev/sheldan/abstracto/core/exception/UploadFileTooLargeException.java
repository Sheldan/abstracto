package dev.sheldan.abstracto.core.exception;

import dev.sheldan.abstracto.core.models.exception.UploadFileTooLargeExceptionModel;
import dev.sheldan.abstracto.core.templating.Templatable;

public class UploadFileTooLargeException extends AbstractoRunTimeException implements Templatable {

    private final UploadFileTooLargeExceptionModel model;

    public UploadFileTooLargeException(Long fileSize, Long fileSizeLimit) {
        super("File too large for uploading it into the server.");
        this.model = UploadFileTooLargeExceptionModel
                .builder()
                .fileSize(fileSize)
                .fileSizeLimit(fileSizeLimit)
                .build();
    }

    @Override
    public String getTemplateName() {
        return "upload_file_too_large_exception";
    }

    @Override
    public Object getTemplateModel() {
        return model;
    }
}

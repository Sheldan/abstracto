package dev.sheldan.abstracto.core.models.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UploadFileTooLargeExceptionModel {
    private Long fileSize;
    private Long fileSizeLimit;
}

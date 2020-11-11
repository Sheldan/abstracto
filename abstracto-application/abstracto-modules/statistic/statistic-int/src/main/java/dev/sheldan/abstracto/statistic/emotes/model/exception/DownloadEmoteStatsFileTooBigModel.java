package dev.sheldan.abstracto.statistic.emotes.model.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class DownloadEmoteStatsFileTooBigModel implements Serializable {
    private Long fileSize;
    private Long fileSizeLimit;
}

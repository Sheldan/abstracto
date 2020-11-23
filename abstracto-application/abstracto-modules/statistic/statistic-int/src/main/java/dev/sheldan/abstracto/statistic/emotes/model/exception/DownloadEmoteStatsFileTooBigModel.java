package dev.sheldan.abstracto.statistic.emotes.model.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Model used for {@link dev.sheldan.abstracto.statistic.emotes.exception.DownloadEmoteStatsFileTooBigException} which contains
 * the file size of the file which was created and the max file size allowed on the server.
 */
@Getter
@Setter
@Builder
public class DownloadEmoteStatsFileTooBigModel implements Serializable {
    /**
     * File size in bytes of the file being created for emote stats
     */
    private Long fileSize;
    /**
     * The file size limit of the server in bytes which was lower than the files size of the file
     */
    private Long fileSizeLimit;
}

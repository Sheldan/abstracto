package dev.sheldan.abstracto.webservices.threadreader.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ThreadReaderCommandResponseModel {
    private Long tweetId;
}

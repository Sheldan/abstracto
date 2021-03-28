package dev.sheldan.abstracto.webservices.youtube.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;

@Getter
@Setter
@Builder
public class YoutubeVideo {
    private String url;
    private BigInteger views;
    private Instant publishedAt;
    private Duration duration;
    private String channelTitle;
    private BigInteger likes;
    private BigInteger dislikes;
    private BigInteger commentCount;
}

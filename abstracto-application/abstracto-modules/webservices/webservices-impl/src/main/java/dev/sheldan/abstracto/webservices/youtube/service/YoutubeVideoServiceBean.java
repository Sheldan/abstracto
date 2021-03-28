package dev.sheldan.abstracto.webservices.youtube.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import dev.sheldan.abstracto.webservices.youtube.exception.YoutubeAPIException;
import dev.sheldan.abstracto.webservices.youtube.exception.YoutubeVideoNotFoundException;
import dev.sheldan.abstracto.webservices.youtube.model.YoutubeVideo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class YoutubeVideoServiceBean implements YoutubeVideoService {

    @Autowired
    private YouTube youTube;

    @Override
    public YoutubeVideo getVideoInfo(String id) {
        try {
            VideoListResponse videoListResponse = youTube
                    .videos()
                    .list("statistics,contentDetails,snippet")
                    .setId(id)
                    .execute();
            List<Video> items = videoListResponse.getItems();
            if(items.isEmpty()) {
                throw new YoutubeVideoNotFoundException();
            }
            Video videoInfo = items.get(0);
            VideoStatistics statistics = videoInfo.getStatistics();
            VideoSnippet snipped = videoInfo.getSnippet();
            VideoContentDetails contentDetails = videoInfo.getContentDetails();
            return YoutubeVideo
                    .builder()
                    .channelTitle(snipped.getChannelTitle())
                    .commentCount(statistics.getCommentCount())
                    .dislikes(statistics.getDislikeCount())
                    .likes(statistics.getLikeCount())
                    .views(statistics.getViewCount())
                    .duration(Duration.parse(contentDetails.getDuration()))
                    .publishedAt(Instant.ofEpochMilli(snipped.getPublishedAt().getValue()))
                    .url(String.format("https://youtu.be/%s", videoInfo.getId()))
                    .build();
        } catch (IOException e) {
            throw new YoutubeAPIException(e);
        }
    }
}

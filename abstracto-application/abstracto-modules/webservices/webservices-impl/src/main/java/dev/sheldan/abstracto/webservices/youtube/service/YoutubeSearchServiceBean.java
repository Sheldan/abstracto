package dev.sheldan.abstracto.webservices.youtube.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import dev.sheldan.abstracto.webservices.youtube.exception.YoutubeAPIException;
import dev.sheldan.abstracto.webservices.youtube.exception.YoutubeVideoNotFoundException;
import dev.sheldan.abstracto.webservices.youtube.model.YoutubeVideo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class YoutubeSearchServiceBean implements YoutubeSearchService {

    @Autowired
    private YouTube youTube;

    @Autowired
    private YoutubeVideoService youtubeVideoService;

    @Override
    public YoutubeVideo searchOneVideoForQuery(String query) {
        try {
            YouTube.Search.List search = youTube.search().list("id");
            search.setQ(query);
            search.setType("video");
            search.setMaxResults(1L);
            SearchListResponse execute = search.execute();
            List<SearchResult> items = execute.getItems();
            if(items.isEmpty()) {
                throw new YoutubeVideoNotFoundException();
            }
            if(items.stream().noneMatch(searchResult -> searchResult.getId().get("kind").equals("youtube#video"))) {
                throw new YoutubeVideoNotFoundException();
            }
            return youtubeVideoService.getVideoInfo(items.get(0).getId().getVideoId());
        } catch (IOException e) {
            throw new YoutubeAPIException(e);
        }
    }
}

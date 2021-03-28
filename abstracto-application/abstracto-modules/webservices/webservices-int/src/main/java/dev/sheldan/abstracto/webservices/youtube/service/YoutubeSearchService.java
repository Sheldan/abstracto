package dev.sheldan.abstracto.webservices.youtube.service;

import dev.sheldan.abstracto.webservices.youtube.model.YoutubeVideo;

public interface YoutubeSearchService {
    YoutubeVideo searchOneVideoForQuery(String query);
}

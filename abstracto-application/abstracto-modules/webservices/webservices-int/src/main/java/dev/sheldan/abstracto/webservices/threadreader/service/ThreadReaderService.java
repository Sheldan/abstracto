package dev.sheldan.abstracto.webservices.threadreader.service;

import java.util.Optional;

public interface ThreadReaderService {
    boolean containsTwitterLink(String text);
    Optional<Long> extractTweetId(String text);
}

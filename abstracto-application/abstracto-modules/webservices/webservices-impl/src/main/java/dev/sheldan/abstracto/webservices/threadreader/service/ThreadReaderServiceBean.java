package dev.sheldan.abstracto.webservices.threadreader.service;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ThreadReaderServiceBean implements ThreadReaderService {
    private static final Pattern TWITTER_STATUS_REGEX = Pattern.compile(".*?https?://twitter\\.com/(?:#!/)?(\\w+)/status(es)?/(?<tweetId>\\d+).*", Pattern.DOTALL);

    @Override
    public boolean containsTwitterLink(String text) {
        return TWITTER_STATUS_REGEX.matcher(text).matches();
    }

    @Override
    public Optional<Long> extractTweetId(String text) {
        Matcher matcher = TWITTER_STATUS_REGEX.matcher(text);
        return matcher.matches() ? Optional.of(Long.parseLong(matcher.group("tweetId"))) : Optional.empty();
    }
}

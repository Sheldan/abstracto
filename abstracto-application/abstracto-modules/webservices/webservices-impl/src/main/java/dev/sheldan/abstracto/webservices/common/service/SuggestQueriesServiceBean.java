package dev.sheldan.abstracto.webservices.common.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import dev.sheldan.abstracto.webservices.common.exception.SuggestQueriesException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@CacheConfig(cacheNames = "general-use-cache")
public class SuggestQueriesServiceBean implements SuggestQueriesService {

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${abstracto.feature.webservices.common.suggestionsURL}")
    private String suggestionUrl;

    @Autowired
    private Gson gson;

    @Autowired
    private SuggestQueriesServiceBean self;

    private List<String> getSuggestionsFromResponse(String response) {
        JsonElement rootJson = JsonParser.parseString(response);
        if(!rootJson.isJsonArray()) {
            return new ArrayList<>();
        }
        JsonArray mainArray = rootJson.getAsJsonArray();
        if(mainArray.size() < 2 || !mainArray.get(1).isJsonArray() || mainArray.get(1).getAsJsonArray().size() == 0) {
            return new ArrayList<>();
        }
        JsonArray suggestionArray = mainArray.get(1).getAsJsonArray();
        return Arrays.asList(gson.fromJson(suggestionArray, String[].class));
    }

    @Override
    @Cacheable(key = "{#query, #service}")
    public List<String> getSuggestionsForQuery(String query, String service) {
        Request request = new Request.Builder()
                .url(String.format(suggestionUrl, service, query))
                .get()
                .build();
        Response response;
        try {
            response = okHttpClient.newCall(request).execute();
            if(!response.isSuccessful()) {
                if(log.isDebugEnabled()) {
                    log.error("Failed to retrieve suggestions. Response had code {} with body {}.",
                            response.code(), response.body());
                }
                throw new SuggestQueriesException(response.code());
            }
            return getSuggestionsFromResponse(response.body().string());
        } catch (IOException e) {
            throw new AbstractoRunTimeException(e);
        }
    }

    @Override
    public List<String> getYoutubeSuggestionsForQuery(String query) {
        if(query == null || "".equals(query)) {
            return new ArrayList<>();
        }
        return self.getSuggestionsForQuery(query, "yt");
    }
}

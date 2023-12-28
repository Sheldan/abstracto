package dev.sheldan.abstracto.webservices.wikipedia.service;

import com.google.gson.Gson;
import dev.sheldan.abstracto.webservices.wikipedia.exception.NoWikipediaArticleFoundException;
import dev.sheldan.abstracto.webservices.wikipedia.exception.WikipediaRequestException;
import dev.sheldan.abstracto.webservices.wikipedia.model.WikipediaArticleSummary;
import dev.sheldan.abstracto.webservices.wikipedia.model.api.WikipediaResponse;
import dev.sheldan.abstracto.webservices.wikipedia.model.api.WikipediaResponsePage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class WikipediaServiceBean implements WikipediaService {

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${abstracto.feature.webservices.wikipedia.summaryURL}")
    private String articleSummaryUrl;

    @Autowired
    private Gson gson;

    @Override
    public WikipediaArticleSummary getSummary(String query, String language) throws IOException {
        String formattedUrl = articleSummaryUrl.replace("{1}", language).replace("{2}", query);
        Request request = new Request.Builder()
                .url(formattedUrl)
                .get()
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if(!response.isSuccessful()) {
            if(log.isDebugEnabled()) {
                log.error("Failed to retrieve wikipedia summary. Response had code {} with body {}.",
                        response.code(), response.body());
            }
            throw new WikipediaRequestException(response.code());
        }
        WikipediaResponse wikipediaResponse = gson.fromJson(response.body().string(), WikipediaResponse.class);
        if(wikipediaResponse.getQuery() == null
                || wikipediaResponse.getQuery().getPages() == null
                || (wikipediaResponse.getQuery().getPages().stream().anyMatch(wikipediaResponsePageModel -> wikipediaResponsePageModel.getPageId().equals(-1L))
                && wikipediaResponse.getQuery().getPages().size() == 1)
                ) {
            throw new NoWikipediaArticleFoundException();
        } else {
            WikipediaResponsePage page = wikipediaResponse.getQuery().getPages().get(0);
            return WikipediaArticleSummary
                    .builder()
                    .title(page.getTitle())
                    .summary(page.getExtract())
                    .fullURL(page.getFullUrl())
                    .build();
        }
    }
}

package dev.sheldan.abstracto.webservices.urban.service;

import com.google.gson.Gson;
import dev.sheldan.abstracto.webservices.urban.exception.NoUrbanDefinitionFoundException;
import dev.sheldan.abstracto.webservices.urban.model.UrbanDefinition;
import dev.sheldan.abstracto.webservices.urban.model.UrbanResponse;
import dev.sheldan.abstracto.webservices.urban.model.UrbanResponseDefinition;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UrbanServiceBean implements UrbanService {

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${abstracto.feature.webservices.urban.requestURL}")
    private String requestUrl;

    @Autowired
    private Gson gson;

    @Override
    public UrbanDefinition getUrbanDefinition(String query) throws IOException {
        Request request = new Request.Builder().url(String.format(requestUrl, query)).get().build();
        Response response = okHttpClient.newCall(request).execute();
        UrbanResponse urbanResponse = gson.fromJson(response.body().string(), UrbanResponse.class);
        if(urbanResponse.getList().isEmpty()) {
            throw new NoUrbanDefinitionFoundException();
        } else {
            UrbanResponseDefinition definition = urbanResponse.getList().get(0);
            return UrbanDefinition
                    .builder()
                    .definition(definition.getDefinition())
                    .author(definition.getAuthor())
                    .url(definition.getPermalink())
                    .creationDate(definition.getWritten_on())
                    .downVoteCount(definition.getThumbs_down())
                    .upvoteCount(definition.getThumbs_up())
                    .example(definition.getExample())
                    .build();
        }
    }
}

package dev.sheldan.abstracto.webservices.dictionaryapi.service;

import com.google.api.client.http.HttpStatusCodes;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.sheldan.abstracto.webservices.dictionaryapi.exception.DictionaryApiRequestException;
import dev.sheldan.abstracto.webservices.dictionaryapi.exception.NoDictionaryApiDefinitionFoundException;
import dev.sheldan.abstracto.webservices.dictionaryapi.model.WordDefinition;
import dev.sheldan.abstracto.webservices.dictionaryapi.model.WordMeaning;
import dev.sheldan.abstracto.webservices.dictionaryapi.model.api.DictionaryApiResponseItem;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class DictionaryApiServiceBean implements DictionaryApiService {

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${abstracto.feature.webservices.dictionaryapi.definitionURL}")
    private String dictionaryDefinitionURL;

    @Autowired
    private Gson gson;

    @Override
    public WordMeaning getDefinitions(String query) throws IOException {
        String formattedUrl = dictionaryDefinitionURL.replace("{1}", query);
        Request request = new Request.Builder()
                .url(formattedUrl)
                .get()
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if(response.code() == HttpStatusCodes.STATUS_CODE_NOT_FOUND) {
            throw new NoDictionaryApiDefinitionFoundException();
        }
        if(!response.isSuccessful()) {
            if(log.isDebugEnabled()) {
                log.error("Failed to retrieve dictionary api definition. Response had code {} with body {}.",
                        response.code(), response.body());
            }
            throw new DictionaryApiRequestException(response.code());
        }
        List<DictionaryApiResponseItem> dictionaryapiResponse = gson.fromJson(response.body().string(), new TypeToken<List<DictionaryApiResponseItem>>(){}.getType());
        DictionaryApiResponseItem selectedWord = dictionaryapiResponse.get(0);
        if(dictionaryapiResponse.size() > 1) {
            log.warn("Dictionary had multiple words. One example {}.", selectedWord.getWord());
        }
        List<WordDefinition> wordDefinitions = selectedWord
                        .getMeanings()
                        .stream()
                        .flatMap(dictionaryApiWordMeaning ->
                                dictionaryApiWordMeaning
                                        .getDefinitions()
                                        .stream()
                                        .map(WordDefinition::fromResponseDefinition))
                        .toList();
        return WordMeaning
                .builder()
                .word(selectedWord.getWord())
                .definitions(wordDefinitions)
                .build();
    }
}

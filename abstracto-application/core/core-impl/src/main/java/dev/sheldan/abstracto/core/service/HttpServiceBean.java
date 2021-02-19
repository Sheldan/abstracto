package dev.sheldan.abstracto.core.service;


import dev.sheldan.abstracto.core.utils.FileService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class HttpServiceBean implements HttpService {

    @Autowired
    private OkHttpClient client;

    @Autowired
    private FileService fileService;

    @Override
    public File downloadFileToTempFile(String url) throws IOException {
        Request request = new Request.Builder().url(url).get().build();
        File tempFile = fileService.createTempFile(Math.random() + "");
        Response execute = client.newCall(request).execute();
        fileService.writeBytesToFile(tempFile, execute.body().bytes());
        return tempFile;
    }
}

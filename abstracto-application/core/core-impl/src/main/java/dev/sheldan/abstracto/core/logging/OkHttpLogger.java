package dev.sheldan.abstracto.core.logging;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OkHttpLogger implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        log.debug("Executing request towards towards {}.", request.url().toString());
        Response response = chain.proceed(request);
        long startTime = response.sentRequestAtMillis();
        long endTime = response.receivedResponseAtMillis();
        log.debug("Response from {} with status {} received in {}ms.", request.url().toString(), response.code(), (endTime - startTime));
        return response;
    }
}

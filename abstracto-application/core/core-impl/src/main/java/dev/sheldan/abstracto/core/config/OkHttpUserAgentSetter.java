package dev.sheldan.abstracto.core.config;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OkHttpUserAgentSetter implements Interceptor {

    @Value("${abstracto.okhttp.useragent}")
    private String userAgent;

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request requestWithUserAgent = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .build();
        return chain.proceed(requestWithUserAgent);
    }
}

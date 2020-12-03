package dev.sheldan.abstracto.core.service;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class HashServiceBean implements HashService {
    @Override
    public String sha256HashFileContent(File file) throws IOException {
        return Files.asByteSource(file).hash(Hashing.sha256()).toString();
    }

    @Override
    public String sha256HashString(String text) {
        return Hashing.sha256().hashString(text, StandardCharsets.UTF_8).toString();
    }
}

package dev.sheldan.abstracto.core.service;

import java.io.File;
import java.io.IOException;

public interface HashService {
    String sha256HashFileContent(File file) throws IOException;
    String sha256HashString(String text);
}

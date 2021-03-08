package dev.sheldan.abstracto.core.service;

import java.io.File;
import java.io.IOException;

public interface HttpService {
    File downloadFileToTempFile(String url) throws IOException;
}

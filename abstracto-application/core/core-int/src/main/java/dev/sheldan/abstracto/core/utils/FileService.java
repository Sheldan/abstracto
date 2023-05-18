package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.exception.AbstractoRunTimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

@Component
@Slf4j
public class FileService {
    public void writeContentToFile(File file, String content) throws IOException {
        try(FileWriter fw = new FileWriter(file)) {
            fw.write(content);
            fw.flush();
        }
    }

    public void writeBytesToFile(File file, byte[] content) throws IOException {
        FileUtils.writeByteArrayToFile(file, content);
    }

    public File createTempFile(String fileName)  {
        try {
            return new File(Files.createTempDirectory("").toFile(), fileName);
        } catch (IOException e) {
            log.error("Failed to create temporary file.");
            throw new AbstractoRunTimeException(e);
        }
    }

    public void safeDelete(File file) throws IOException {
        java.nio.file.Files.delete(file.toPath());
    }

    public void safeDeleteIgnoreException(File file) {
        try {
            Files.delete(file.toPath());
        } catch (IOException e) {
            log.warn("Failed to delete file - ignoring.", e);
        }
    }
}

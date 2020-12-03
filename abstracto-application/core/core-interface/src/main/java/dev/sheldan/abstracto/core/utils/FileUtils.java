package dev.sheldan.abstracto.core.utils;

import com.google.common.io.Files;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Component
public class FileUtils {
    public void writeContentToFile(File file, String content) throws IOException {
        try(FileWriter fw = new FileWriter(file)) {
            fw.write(content);
            fw.flush();
        }
    }

    public void writeBytesToFile(File file, byte[] content) throws IOException {
        Files.write(content, file);
    }

    public File createTempFile(String fileName) {
        return new File(Files.createTempDir(), fileName);
    }

    public void safeDelete(File file) throws IOException {
        java.nio.file.Files.delete(file.toPath());
    }
}

package dev.sheldan.abstracto.core.utils;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Component
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

    public File createTempFile(String fileName) {
        return new File(Files.createTempDir(), fileName);
    }

    public void safeDelete(File file) throws IOException {
        java.nio.file.Files.delete(file.toPath());
    }
}

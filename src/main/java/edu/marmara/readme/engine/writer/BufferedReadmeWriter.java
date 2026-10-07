package edu.marmara.readme.engine.writer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Python buffered_writer.py'nin Java portu — bellekte biriktirip tek seferde dosyaya yazar. */
public class BufferedReadmeWriter {

    private final StringBuilder buffer = new StringBuilder();

    public BufferedReadmeWriter write(String content) {
        buffer.append(content);
        return this;
    }

    public BufferedReadmeWriter writeline(String content) {
        buffer.append(content).append("\n");
        return this;
    }

    public BufferedReadmeWriter writeline() {
        return writeline("");
    }

    public void save(Path path) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, buffer.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("README yazılamadı: " + path, e);
        }
    }

    public void appendToFile(Path path) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, buffer.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("README'ye eklenemedi: " + path, e);
        }
    }

    public String getContent() {
        return buffer.toString();
    }

    public void clear() {
        buffer.setLength(0);
    }

    public int length() {
        return buffer.length();
    }
}

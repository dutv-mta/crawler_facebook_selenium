package com.dutv.utils;

import com.google.common.io.Files;
import org.apache.commons.io.Charsets;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class FileAppend {
    public synchronized static void append(String content, String path) throws IOException {
        File file = new File(path);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        Files.append(content, file, Charsets.UTF_8);
    }

    public synchronized static void append(String content, Path path) throws IOException {
        append(content, path.toAbsolutePath().toString());
    }
}

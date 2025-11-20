package com.luisfelipe.simplecarapi.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Component
public class FileUtils {
    @Autowired
    private ResourceLoader resourceLoader;

    public String readResourceFile(String fileName) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:%s".formatted(fileName));

        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }
}

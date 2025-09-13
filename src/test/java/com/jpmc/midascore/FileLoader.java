package com.jpmc.midascore;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class FileLoader {
    public String[] loadStrings(String fileName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("File not found! " + fileName);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String result = reader.lines().collect(Collectors.joining("\n"));
                return result.split("\n");
            }
        } catch (Exception e) {
            // It's better to let the test fail with a clear exception than to return null
            throw new RuntimeException("Could not read file: " + fileName, e);
        }
    }
}


package com.jpmc.midascore;

import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class FileLoader {
    public String[] loadStrings(String fileName) {
        // This logic handles incorrect leading slashes in the file path
        String correctedFileName = fileName.startsWith("/") ? fileName.substring(1) : fileName;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(correctedFileName)) {
            if (is == null) {
                throw new IllegalArgumentException("File not found! " + correctedFileName);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return reader.lines().toArray(String[]::new);
            }
        } catch (Exception e) {
            System.err.println("Could not read file: " + correctedFileName);
            e.printStackTrace();
            return null; // Return null to indicate failure
        }
    }
}


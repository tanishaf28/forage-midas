package com.jpmc.midascore;//This is a utility class that loads files from the resources folder and returns them as an array of strings. It uses the Apache Commons IO library to read the file contents and split them into lines. The @Component annotation allows it to be injected into other classes using Spring's dependency injection.

import org.springframework.stereotype.Component;
import org.testcontainers.shaded.org.apache.commons.io.IOUtils;

import java.io.InputStream;

@Component
public class FileLoader {
    public String[] loadStrings(String path) {
        try {
            InputStream inputStream = this.getClass().getResourceAsStream(path);
            String fileText = IOUtils.toString(inputStream, "UTF-8");
            return fileText.split(System.lineSeparator());
        } catch (Exception e) {
            return null;
        }
    }
}

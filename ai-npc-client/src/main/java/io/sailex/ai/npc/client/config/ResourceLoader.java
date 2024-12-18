package io.sailex.ai.npc.client.config;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ResourceLoader {

    private static final Logger LOGGER = LogManager.getLogger(ResourceLoader.class);

    private ResourceLoader() {}

    public static String getResourcePath(String resource) {
        URL url = ResourceLoader.class.getClassLoader().getResource(resource);
        if (url != null) {
            return url.getPath();
        } else {
            return null;
        }
    }

    public static String getResourceJsonContent(String resourcePath) {
        try {
            return FileUtils.readFileToString(new File(resourcePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.error("Error reading resource file: {}", e.getMessage());
            return null;
        }
    }

}

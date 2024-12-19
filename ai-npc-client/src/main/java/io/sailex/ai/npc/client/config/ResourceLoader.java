package io.sailex.ai.npc.client.config;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ResourceLoader {

    private static final Logger LOGGER = LogManager.getLogger(ResourceLoader.class);

    private ResourceLoader() {}

    public static Map<String, String> getAllResourcesContent(String resourcePath) {
        Map<String, String> contentMap = new HashMap<>();
        try {
            Enumeration<URL> resources = ResourceLoader.class.getClassLoader().getResources(resourcePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                File folder = new File(resource.toURI());
                Optional.ofNullable(folder.listFiles())
                        .ifPresent(files -> Arrays.stream(files)
                                .forEach(file -> {
                                    String content = getResourceContent(resourcePath + "/" + file.getName());
                                    if (content != null) {
                                        contentMap.put(FilenameUtils.getBaseName(file.getName()), content);
                                    }
                                }));
            }
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("Error reading resources: {}", e.getMessage());
        }
        return contentMap;
    }

    public static String getResourceContent(String resourcePath) {
        try (InputStream in = ResourceLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            return reader.lines().collect(Collectors.joining());
        } catch (IOException e) {
            LOGGER.error("Error reading resource file: {}", e.getMessage());
            return null;
        }
    }

}

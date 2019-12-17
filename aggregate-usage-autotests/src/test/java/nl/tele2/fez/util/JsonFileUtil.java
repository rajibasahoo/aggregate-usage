package nl.tele2.fez.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonFileUtil {

    public static String fileToString(URI uri) {
        try {
            return new String(Files.readAllBytes(Paths.get(JsonFileUtil.class.getClassLoader().getResource(uri.toString()).toURI())));
        } catch (IOException |URISyntaxException e) {
            return "";
        }
    }
}

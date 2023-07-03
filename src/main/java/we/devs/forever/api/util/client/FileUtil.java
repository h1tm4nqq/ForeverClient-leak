package we.devs.forever.api.util.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

public
class FileUtil {

    public static void appendTextFile(String data, String file) {
        try {
            final Path path = Paths.get(file);
            Files.write(path, Collections.singletonList(data), StandardCharsets.UTF_8, Files.exists(path) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
        } catch (final IOException e) {
            System.out.println("WARNING: Unable to write file: " + file);
        }
    }

    public static List<String> readTextFileAllLines(String file) {
        try {
            final Path path = Paths.get(file);
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            System.out.println("WARNING: Unable to read file, creating new file: " + file);
            appendTextFile("", file);
        }
        return Collections.emptyList();
    }

}

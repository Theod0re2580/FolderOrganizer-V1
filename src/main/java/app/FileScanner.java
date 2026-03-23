package app;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.DosFileAttributes;
import java.util.*;
import java.util.stream.Stream;

public class FileScanner {

    private static final Set<String> DEFAULT_EXCLUDED_NAMES = Set.of(
            "desktop.ini",
            "thumbs.db"
    );

    public List<Path> scan(Path folderPath) throws IOException {
        List<Path> results = new ArrayList<>();

        try (Stream<Path> stream = Files.list(folderPath)) {
            stream.forEach(path -> {
                try {
                    if (shouldInclude(path)) {
                        results.add(path);
                    }
                } catch (IOException e) {
                    System.out.println("읽기 실패: " + path + " / " + e.getMessage());
                }
            });
        }

        results.sort(Comparator.comparing(path -> path.getFileName().toString().toLowerCase()));
        return results;
    }

    private boolean shouldInclude(Path path) throws IOException {
        String name = path.getFileName().toString();

        if (DEFAULT_EXCLUDED_NAMES.contains(name.toLowerCase())) {
            return false;
        }

        if (!Files.isReadable(path)) {
            return false;
        }

        if (Files.isHidden(path)) {
            return false;
        }

        if (isSystemFile(path)) {
            return false;
        }

        return true;
    }

    private boolean isSystemFile(Path path) {
        try {
            DosFileAttributes attrs = Files.readAttributes(path, DosFileAttributes.class);
            return attrs.isSystem();
        } catch (Exception e) {
            return false;
        }
    }
}
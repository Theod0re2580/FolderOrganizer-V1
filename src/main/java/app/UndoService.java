package app;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class UndoService {

    public UndoResult undo(Path logFilePath) throws IOException {
        if (logFilePath == null || !Files.exists(logFilePath)) {
            throw new IOException("되돌리기 로그 파일이 없습니다.");
        }

        List<String> lines = Files.readAllLines(logFilePath);
        Collections.reverse(lines);

        int restoredCount = 0;
        List<String> errors = new ArrayList<>();

        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }

            try {
                String[] parts = line.split("\\|", 2);
                if (parts.length != 2) {
                    errors.add("잘못된 로그 형식: " + line);
                    continue;
                }

                Path originalPath = Paths.get(parts[0]);
                Path movedPath = Paths.get(parts[1]);

                if (!Files.exists(movedPath)) {
                    errors.add("이동된 파일 없음: " + movedPath.getFileName());
                    continue;
                }

                if (originalPath.getParent() != null) {
                    Files.createDirectories(originalPath.getParent());
                }

                Path restoreTarget = resolveDuplicate(originalPath);
                Files.move(movedPath, restoreTarget, StandardCopyOption.REPLACE_EXISTING);
                restoredCount++;

            } catch (AccessDeniedException e) {
                errors.add("권한 없음: " + line);
            } catch (FileSystemException e) {
                errors.add("파일 시스템 오류: " + line + " / " + e.getMessage());
            } catch (Exception e) {
                errors.add("되돌리기 실패: " + line + " / " + e.getMessage());
            }
        }

        Path organizedRoot = logFilePath.getParent();
        cleanupEmptyFolders(organizedRoot, logFilePath);

        return new UndoResult(restoredCount, errors);
    }

    private void cleanupEmptyFolders(Path organizedRoot, Path logFilePath) {
        if (organizedRoot == null || !Files.exists(organizedRoot)) {
            return;
        }

        try (Stream<Path> stream = Files.walk(organizedRoot)) {
            stream.sorted((a, b) -> Integer.compare(b.getNameCount(), a.getNameCount()))
                    .forEach(path -> {
                        try {
                            if (Files.isDirectory(path) && isDirectoryEmpty(path)) {
                                Files.deleteIfExists(path);
                            }
                        } catch (Exception ignored) {
                        }
                    });
        } catch (Exception ignored) {
        }

        try {
            if (Files.exists(logFilePath) && isOnlyLogLeft(organizedRoot, logFilePath)) {
                Files.deleteIfExists(logFilePath);
            }
        } catch (Exception ignored) {
        }

        try {
            if (Files.exists(organizedRoot) && isDirectoryEmpty(organizedRoot)) {
                Files.deleteIfExists(organizedRoot);
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isOnlyLogLeft(Path organizedRoot, Path logFilePath) throws IOException {
        try (Stream<Path> stream = Files.list(organizedRoot)) {
            List<Path> children = stream.toList();
            return children.size() == 1 && children.get(0).equals(logFilePath);
        }
    }

    private boolean isDirectoryEmpty(Path path) throws IOException {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)) {
            return !directoryStream.iterator().hasNext();
        }
    }

    private Path resolveDuplicate(Path targetPath) {
        if (!Files.exists(targetPath)) {
            return targetPath;
        }

        String fileName = targetPath.getFileName().toString();
        String name = fileName;
        String extension = "";

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            name = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex);
        }

        int count = 1;
        Path parent = targetPath.getParent();

        while (true) {
            Path newPath = parent.resolve(name + " (" + count + ")" + extension);
            if (!Files.exists(newPath)) {
                return newPath;
            }
            count++;
        }
    }

    public static class UndoResult {
        private final int restoredCount;
        private final List<String> errors;

        public UndoResult(int restoredCount, List<String> errors) {
            this.restoredCount = restoredCount;
            this.errors = errors;
        }

        public int getRestoredCount() {
            return restoredCount;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
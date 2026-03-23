package app;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrganizerService {

    public OrganizeResult organize(Path sourceFolder, List<FileItem> items) throws IOException {
        String sourceFolderName = sourceFolder.getFileName().toString();
        Path parentFolder = sourceFolder.getParent();

        if (parentFolder == null) {
            throw new IOException("상위 폴더를 찾을 수 없습니다.");
        }

        Path organizedRoot = parentFolder.resolve(sourceFolderName + "_정리결과");
        Files.createDirectories(organizedRoot);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path logFilePath = organizedRoot.resolve(sourceFolderName + "_" + timestamp + ".txt");

        int movedCount = 0;
        int skippedCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedWriter writer = Files.newBufferedWriter(
                logFilePath,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        )) {
            for (FileItem item : items) {
                try {
                    if (item.isExcluded()) {
                        skippedCount++;
                        continue;
                    }

                    String targetFolderName = item.getTargetFolder();

                    if (targetFolderName == null || targetFolderName.isBlank() || "정리 제외".equals(targetFolderName)) {
                        skippedCount++;
                        continue;
                    }

                    Path sourcePath = sourceFolder.resolve(item.getName());

                    if (!Files.exists(sourcePath)) {
                        skippedCount++;
                        errors.add("파일 없음: " + item.getName());
                        continue;
                    }

                    if (Files.isDirectory(sourcePath)) {
                        skippedCount++;
                        continue;
                    }

                    Path targetFolderPath = organizedRoot.resolve(targetFolderName);
                    Files.createDirectories(targetFolderPath);

                    Path targetPath = resolveDuplicate(targetFolderPath.resolve(item.getName()));
                    Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                    writer.write(sourcePath.toAbsolutePath() + "|" + targetPath.toAbsolutePath());
                    writer.newLine();

                    movedCount++;

                } catch (AccessDeniedException e) {
                    skippedCount++;
                    errors.add("권한 없음: " + item.getName());
                } catch (FileSystemException e) {
                    skippedCount++;
                    errors.add("파일 시스템 오류: " + item.getName() + " / " + e.getMessage());
                } catch (Exception e) {
                    skippedCount++;
                    errors.add("이동 실패: " + item.getName() + " / " + e.getMessage());
                }
            }
        }

        return new OrganizeResult(organizedRoot, logFilePath, movedCount, skippedCount, errors);
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

    public static class OrganizeResult {
        private final Path organizedRoot;
        private final Path logFilePath;
        private final int movedCount;
        private final int skippedCount;
        private final List<String> errors;

        public OrganizeResult(Path organizedRoot, Path logFilePath, int movedCount, int skippedCount, List<String> errors) {
            this.organizedRoot = organizedRoot;
            this.logFilePath = logFilePath;
            this.movedCount = movedCount;
            this.skippedCount = skippedCount;
            this.errors = errors;
        }

        public Path getOrganizedRoot() {
            return organizedRoot;
        }

        public Path getLogFilePath() {
            return logFilePath;
        }

        public int getMovedCount() {
            return movedCount;
        }

        public int getSkippedCount() {
            return skippedCount;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
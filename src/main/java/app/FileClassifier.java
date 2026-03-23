package app;

import java.nio.file.Path;
import java.util.Set;

public class FileClassifier {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "png", "jpg", "jpeg", "gif", "bmp", "webp", "svg"
    );

    private static final Set<String> DESIGN_EXTENSIONS = Set.of(
            "psd", "psb", "ai", "xd"
    );

    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "txt",
            "hwp", "hwpx", "hwt",
            "xls", "xlsx", "ppt", "pptx"
    );

    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "avi", "mkv", "mov", "wmv"
    );

    private static final Set<String> AUDIO_EXTENSIONS = Set.of(
            "mp3", "wav", "flac", "aac"
    );

    private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
            "zip", "rar", "7z", "tar", "gz"
    );

    private static final Set<String> EXECUTABLE_EXTENSIONS = Set.of(
            "exe", "msi", "bat", "cmd"
    );

    private static final Set<String> SHORTCUT_EXTENSIONS = Set.of(
            "lnk", "url"
    );

    public FileItem classify(Path path) {
        String name = path.getFileName().toString();

        if (path.toFile().isDirectory()) {
            return new FileItem(name, "폴더", "-", "폴더", "정리 제외", true);
        }

        String extension = getExtension(name);

        if (extension.isEmpty()) {
            return new FileItem(name, "파일", "-", "기타", "Others", false);
        }

        if (IMAGE_EXTENSIONS.contains(extension)) {
            return new FileItem(name, "파일", extension, "이미지", "Images", false);
        }

        if (DESIGN_EXTENSIONS.contains(extension)) {
            return new FileItem(name, "파일", extension, "디자인", "Designs", false);
        }

        if (DOCUMENT_EXTENSIONS.contains(extension)) {
            return new FileItem(name, "파일", extension, "문서", "Documents", false);
        }

        if (VIDEO_EXTENSIONS.contains(extension)) {
            return new FileItem(name, "파일", extension, "동영상", "Videos", false);
        }

        if (AUDIO_EXTENSIONS.contains(extension)) {
            return new FileItem(name, "파일", extension, "오디오", "Audio", false);
        }

        if (ARCHIVE_EXTENSIONS.contains(extension)) {
            return new FileItem(name, "파일", extension, "압축파일", "Archives", false);
        }

        if (EXECUTABLE_EXTENSIONS.contains(extension)) {
            return new FileItem(name, "파일", extension, "실행파일", "Programs", false);
        }

        if (SHORTCUT_EXTENSIONS.contains(extension)) {
            return new FileItem(name, "파일", extension, "바로가기", "Shortcuts", false);
        }

        return new FileItem(name, "파일", extension, "기타", "Others", false);
    }

    private String getExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');

        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
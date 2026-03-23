package app;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MainApp extends Application {

    private final FileScanner fileScanner = new FileScanner();
    private final FileClassifier fileClassifier = new FileClassifier();
    private final OrganizerService organizerService = new OrganizerService();
    private final UndoService undoService = new UndoService();

    private Path lastOpenedFolderPath;
    private Path lastLogFilePath;
    private List<String> lastErrorDetails = new ArrayList<>();

    private static final List<String> CATEGORY_OPTIONS = List.of(
            "이미지", "디자인", "문서", "동영상", "오디오",
            "압축파일", "실행파일", "바로가기", "기타", "폴더"
    );

    private static final Map<String, String> CATEGORY_TO_FOLDER = Map.ofEntries(
            Map.entry("이미지", "Images"),
            Map.entry("디자인", "Designs"),
            Map.entry("문서", "Documents"),
            Map.entry("동영상", "Videos"),
            Map.entry("오디오", "Audio"),
            Map.entry("압축파일", "Archives"),
            Map.entry("실행파일", "Programs"),
            Map.entry("바로가기", "Shortcuts"),
            Map.entry("기타", "Others"),
            Map.entry("폴더", "정리 제외")
    );

    @Override
    public void start(Stage stage) {
        Label titleLabel = new Label("Folder Organizer");
        titleLabel.getStyleClass().add("page-title");

        Label subtitleLabel = new Label("선택한 폴더 안의 파일만 분류해서 정리합니다. 폴더는 이동하지 않고 항상 정리 대상에서 제외됩니다.");
        subtitleLabel.getStyleClass().add("page-subtitle");

        VBox headerBox = new VBox(6, titleLabel, subtitleLabel);

        Label folderSectionTitle = new Label("작업 폴더");
        folderSectionTitle.getStyleClass().add("section-title");

        Label pathLabel = new Label("선택된 폴더 없음");
        pathLabel.getStyleClass().add("path-label");

        Label infoLabel = new Label("폴더 내부의 파일만 정리되며, 기존 폴더들은 읽기 전용으로 표시됩니다.");
        infoLabel.getStyleClass().add("info-label");

        Button selectFolderButton = new Button("폴더 선택");
        selectFolderButton.getStyleClass().addAll("button", "primary-button");

        Button scanButton = new Button("목록 읽기");
        scanButton.getStyleClass().addAll("button", "secondary-button");

        Button organizeButton = new Button("정리 실행");
        organizeButton.getStyleClass().addAll("button", "primary-button");

        Button openFolderButton = new Button("정리 폴더 열기");
        openFolderButton.getStyleClass().addAll("button", "secondary-button");

        Button undoButton = new Button("되돌리기");
        undoButton.getStyleClass().addAll("button", "danger-button");

        Button selectLogUndoButton = new Button("로그 파일 선택 되돌리기");
        selectLogUndoButton.getStyleClass().addAll("button", "secondary-button");

        Button errorDetailButton = new Button("오류 상세 보기");
        errorDetailButton.getStyleClass().addAll("button", "secondary-button");

        openFolderButton.setDisable(true);
        undoButton.setDisable(true);
        errorDetailButton.setDisable(true);

        TableView<FileItem> tableView = new TableView<>();
        tableView.setEditable(true);

        TableColumn<FileItem, Boolean> excludedColumn = new TableColumn<>("정리 제외");
        excludedColumn.setCellValueFactory(data -> data.getValue().excludedProperty());
        excludedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(excludedColumn));
        excludedColumn.setPrefWidth(90);

        TableColumn<FileItem, String> nameColumn = new TableColumn<>("이름");
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        nameColumn.setPrefWidth(250);

        TableColumn<FileItem, String> typeColumn = new TableColumn<>("종류");
        typeColumn.setCellValueFactory(data -> data.getValue().itemTypeProperty());
        typeColumn.setPrefWidth(80);

        TableColumn<FileItem, String> extensionColumn = new TableColumn<>("확장자");
        extensionColumn.setCellValueFactory(data -> data.getValue().extensionProperty());
        extensionColumn.setPrefWidth(90);

        TableColumn<FileItem, String> categoryColumn = new TableColumn<>("분류결과");
        categoryColumn.setCellValueFactory(data -> data.getValue().categoryProperty());
        categoryColumn.setCellFactory(ComboBoxTableCell.forTableColumn(
                FXCollections.observableArrayList(CATEGORY_OPTIONS)
        ));
        categoryColumn.setOnEditCommit(event -> {
            FileItem item = event.getRowValue();
            String newCategory = event.getNewValue();

            item.setCategory(newCategory);

            if ("폴더".equals(item.getItemType())) {
                item.setTargetFolder("정리 제외");
                item.setExcluded(true);
            } else {
                item.setTargetFolder(CATEGORY_TO_FOLDER.getOrDefault(newCategory, "Others"));
            }

            tableView.refresh();
        });
        categoryColumn.setPrefWidth(120);

        TableColumn<FileItem, String> targetFolderColumn = new TableColumn<>("이동 예정 폴더");
        targetFolderColumn.setCellValueFactory(data -> data.getValue().targetFolderProperty());
        targetFolderColumn.setCellFactory(ComboBoxTableCell.forTableColumn(
                "Images", "Designs", "Documents", "Videos", "Audio",
                "Archives", "Programs", "Shortcuts", "Others", "정리 제외"
        ));
        targetFolderColumn.setOnEditCommit(event -> {
            FileItem item = event.getRowValue();
            item.setTargetFolder(event.getNewValue());

            if ("정리 제외".equals(event.getNewValue())) {
                item.setExcluded(true);
            }

            tableView.refresh();
        });
        targetFolderColumn.setPrefWidth(160);

        tableView.getColumns().addAll(
                excludedColumn, nameColumn, typeColumn,
                extensionColumn, categoryColumn, targetFolderColumn
        );
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableView.setPrefHeight(430);

        Label tableSectionTitle = new Label("파일 분류 미리보기");
        tableSectionTitle.getStyleClass().add("section-title");

        VBox tableCard = new VBox(12, tableSectionTitle, tableView);
        tableCard.getStyleClass().add("card");
        VBox.setVgrow(tableView, Priority.ALWAYS);

        Label statusLabel = new Label("대기 중");
        statusLabel.getStyleClass().add("status-label");

        HBox statusBar = new HBox(statusLabel);
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.getStyleClass().add("status-bar");

        final File[] selectedDirectoryHolder = new File[1];

        Runnable clearErrors = () -> {
            lastErrorDetails = new ArrayList<>();
            errorDetailButton.setDisable(true);
        };

        Runnable refreshTable = () -> {
            File selectedDirectory = selectedDirectoryHolder[0];

            if (selectedDirectory == null) {
                return;
            }

            try {
                List<Path> scannedItems = fileScanner.scan(selectedDirectory.toPath());
                List<FileItem> classifiedItems = scannedItems.stream()
                        .map(fileClassifier::classify)
                        .collect(Collectors.toList());

                tableView.setItems(FXCollections.observableArrayList(classifiedItems));
            } catch (Exception e) {
                statusLabel.setText("자동 새로고침 실패: " + e.getMessage());
            }
        };

        selectFolderButton.setOnAction(event -> {
            try {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("정리할 폴더를 선택하세요");

                File selectedDirectory = directoryChooser.showDialog(stage);

                if (selectedDirectory != null) {
                    selectedDirectoryHolder[0] = selectedDirectory;
                    pathLabel.setText("선택된 폴더: " + selectedDirectory.getAbsolutePath());
                    statusLabel.setText("폴더가 선택되었습니다.");
                    clearErrors.run();
                } else {
                    statusLabel.setText("폴더 선택이 취소되었습니다.");
                }
            } catch (Exception e) {
                statusLabel.setText("폴더 선택 실패: " + e.getMessage());
            }
        });

        scanButton.setOnAction(event -> {
            File selectedDirectory = selectedDirectoryHolder[0];

            if (selectedDirectory == null) {
                statusLabel.setText("먼저 폴더를 선택하세요.");
                return;
            }

            try {
                refreshTable.run();
                clearErrors.run();
                statusLabel.setText("목록을 다시 읽었습니다. 총 " + tableView.getItems().size() + "개");
            } catch (Exception e) {
                statusLabel.setText("목록 읽기 실패: " + e.getMessage());
            }
        });

        organizeButton.setOnAction(event -> {
            File selectedDirectory = selectedDirectoryHolder[0];

            if (selectedDirectory == null) {
                statusLabel.setText("먼저 폴더를 선택하세요.");
                return;
            }

            List<FileItem> items = new ArrayList<>(tableView.getItems());
            long targetCount = items.stream()
                    .filter(item -> !item.isExcluded())
                    .filter(item -> !"정리 제외".equals(item.getTargetFolder()))
                    .filter(item -> "파일".equals(item.getItemType()))
                    .count();

            if (targetCount == 0) {
                statusLabel.setText("정리할 파일이 없습니다.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("정리 실행 확인");
            confirmAlert.setHeaderText("정리를 실행하시겠습니까?");
            confirmAlert.setContentText(
                    "선택한 폴더: " + selectedDirectory.getAbsolutePath() + "\n" +
                    "정리 대상 파일 수: " + targetCount + "\n\n" +
                    "폴더는 이동하지 않고, 파일만 \"" + selectedDirectory.getName() + "_정리결과\" 폴더로 정리합니다."
            );

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                statusLabel.setText("정리 실행이 취소되었습니다.");
                return;
            }

            try {
                OrganizerService.OrganizeResult organizeResult =
                        organizerService.organize(selectedDirectory.toPath(), items);

                lastOpenedFolderPath = organizeResult.getOrganizedRoot();
                lastLogFilePath = organizeResult.getLogFilePath();
                lastErrorDetails = new ArrayList<>(organizeResult.getErrors());

                openFolderButton.setDisable(false);
                undoButton.setDisable(false);
                errorDetailButton.setDisable(lastErrorDetails.isEmpty());

                refreshTable.run();

                String message = "정리 완료: " + organizeResult.getMovedCount()
                        + "개 이동, " + organizeResult.getSkippedCount() + "개 제외/실패";

                if (!organizeResult.getErrors().isEmpty()) {
                    message += " / 오류 " + organizeResult.getErrors().size() + "건";
                }

                statusLabel.setText(message);

                try {
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(lastOpenedFolderPath.toFile());
                    }
                } catch (Exception e) {
                    statusLabel.setText(message + " / 폴더 자동 열기 실패: " + e.getMessage());
                }

            } catch (Exception e) {
                statusLabel.setText("정리 실행 실패: " + e.getMessage());
            }
        });

        openFolderButton.setOnAction(event -> {
            if (lastOpenedFolderPath == null) {
                statusLabel.setText("열 수 있는 정리 폴더가 없습니다.");
                return;
            }

            try {
                if (!Desktop.isDesktopSupported()) {
                    statusLabel.setText("이 환경에서는 폴더 열기를 지원하지 않습니다.");
                    return;
                }

                Desktop.getDesktop().open(lastOpenedFolderPath.toFile());
                statusLabel.setText("정리 폴더를 열었습니다: " + lastOpenedFolderPath);
            } catch (Exception e) {
                statusLabel.setText("폴더 열기 실패: " + e.getMessage());
            }
        });

        undoButton.setOnAction(event -> {
            if (lastLogFilePath == null) {
                statusLabel.setText("되돌릴 로그가 없습니다.");
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("되돌리기 확인");
            confirmAlert.setHeaderText("정리 이전 상태로 되돌리시겠습니까?");
            confirmAlert.setContentText("로그 파일 기준으로 이동된 파일들을 원래 위치로 복구합니다.");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                statusLabel.setText("되돌리기가 취소되었습니다.");
                return;
            }

            try {
                UndoService.UndoResult undoResult = undoService.undo(lastLogFilePath);
                lastErrorDetails = new ArrayList<>(undoResult.getErrors());
                errorDetailButton.setDisable(lastErrorDetails.isEmpty());

                refreshTable.run();

                String message = "되돌리기 완료: " + undoResult.getRestoredCount() + "개 복구";
                if (!undoResult.getErrors().isEmpty()) {
                    message += " / 오류 " + undoResult.getErrors().size() + "건";
                }

                statusLabel.setText(message);

            } catch (Exception e) {
                statusLabel.setText("되돌리기 실패: " + e.getMessage());
            }
        });

        selectLogUndoButton.setOnAction(event -> {
            try {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("되돌릴 로그 파일 선택");
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("로그 파일", "*.txt", "*.log")
                );

                File selectedLogFile = fileChooser.showOpenDialog(stage);
                if (selectedLogFile == null) {
                    statusLabel.setText("로그 파일 선택이 취소되었습니다.");
                    return;
                }

                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("로그 파일 되돌리기 확인");
                confirmAlert.setHeaderText("선택한 로그 파일 기준으로 되돌리시겠습니까?");
                confirmAlert.setContentText("로그 파일: " + selectedLogFile.getAbsolutePath());

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isEmpty() || result.get() != ButtonType.OK) {
                    statusLabel.setText("로그 파일 되돌리기가 취소되었습니다.");
                    return;
                }

                UndoService.UndoResult undoResult = undoService.undo(selectedLogFile.toPath());
                lastLogFilePath = selectedLogFile.toPath();
                lastErrorDetails = new ArrayList<>(undoResult.getErrors());
                errorDetailButton.setDisable(lastErrorDetails.isEmpty());

                refreshTable.run();

                String message = "로그 파일 되돌리기 완료: " + undoResult.getRestoredCount() + "개 복구";
                if (!undoResult.getErrors().isEmpty()) {
                    message += " / 오류 " + undoResult.getErrors().size() + "건";
                }

                statusLabel.setText(message);

            } catch (Exception e) {
                statusLabel.setText("로그 파일 되돌리기 실패: " + e.getMessage());
            }
        });

        errorDetailButton.setOnAction(event -> {
            if (lastErrorDetails == null || lastErrorDetails.isEmpty()) {
                statusLabel.setText("표시할 오류 내역이 없습니다.");
                return;
            }

            Alert errorAlert = new Alert(Alert.AlertType.INFORMATION);
            errorAlert.setTitle("오류 상세");
            errorAlert.setHeaderText("오류 " + lastErrorDetails.size() + "건");

            TextArea textArea = new TextArea(String.join("\n", lastErrorDetails));
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefWidth(700);
            textArea.setPrefHeight(400);

            errorAlert.getDialogPane().setContent(textArea);
            errorAlert.showAndWait();
        });

        HBox actionRow1 = new HBox(10, selectFolderButton, scanButton, organizeButton, openFolderButton);
        actionRow1.setAlignment(Pos.CENTER_LEFT);

        HBox actionRow2 = new HBox(10, undoButton, selectLogUndoButton, errorDetailButton);
        actionRow2.setAlignment(Pos.CENTER_LEFT);

        VBox folderCard = new VBox(12, folderSectionTitle, pathLabel, infoLabel, actionRow1, actionRow2);
        folderCard.getStyleClass().add("card");

        VBox root = new VBox(16, headerBox, folderCard, tableCard, statusBar);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 1160, 780);
        var cssUrl = getClass().getResource("/app/styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        stage.setTitle("Folder Organizer");
        var iconStream = getClass().getResourceAsStream("/app/icon.png");
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
        }
        stage.setMinWidth(1100);
        stage.setMinHeight(760);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
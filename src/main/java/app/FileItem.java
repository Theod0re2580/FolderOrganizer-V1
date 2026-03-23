package app;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class FileItem {

    private final StringProperty name;
    private final StringProperty itemType;
    private final StringProperty extension;
    private final StringProperty category;
    private final StringProperty targetFolder;
    private final BooleanProperty excluded;

    public FileItem(String name, String itemType, String extension, String category, String targetFolder, boolean excluded) {
        this.name = new SimpleStringProperty(name);
        this.itemType = new SimpleStringProperty(itemType);
        this.extension = new SimpleStringProperty(extension);
        this.category = new SimpleStringProperty(category);
        this.targetFolder = new SimpleStringProperty(targetFolder);
        this.excluded = new SimpleBooleanProperty(excluded);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getItemType() {
        return itemType.get();
    }

    public StringProperty itemTypeProperty() {
        return itemType;
    }

    public String getExtension() {
        return extension.get();
    }

    public StringProperty extensionProperty() {
        return extension;
    }

    public String getCategory() {
        return category.get();
    }

    public void setCategory(String category) {
        this.category.set(category);
    }

    public StringProperty categoryProperty() {
        return category;
    }

    public String getTargetFolder() {
        return targetFolder.get();
    }

    public void setTargetFolder(String targetFolder) {
        this.targetFolder.set(targetFolder);
    }

    public StringProperty targetFolderProperty() {
        return targetFolder;
    }

    public boolean isExcluded() {
        return excluded.get();
    }

    public void setExcluded(boolean excluded) {
        this.excluded.set(excluded);
    }

    public BooleanProperty excludedProperty() {
        return excluded;
    }
}
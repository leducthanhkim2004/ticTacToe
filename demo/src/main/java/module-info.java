module your.module.name {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;

    opens Viewer to javafx.fxml;
    exports comnet;
    exports Viewer;
}

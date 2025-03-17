module comnet {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    

    opens comnet to javafx.fxml;
    opens Viewer to javafx.fxml;
    exports comnet;
    exports Viewer;
}

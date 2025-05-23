module comnet.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires javafx.graphics;
    requires java.sql;  // Add this for database access
    
    
    
    opens Viewer to javafx.fxml;
    exports comnet;
    exports Viewer;
    
    // Add these to ensure your other packages are visible
    exports Model;
    exports Control;
    exports Factory;
    exports Online;
    exports Database;  // Make sure Database package is exported
}

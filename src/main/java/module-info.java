module com.kroll.fcsmodify {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.base;

    opens com.kroll.fcsmodify to javafx.fxml;
    exports com.kroll.fcsmodify;
}

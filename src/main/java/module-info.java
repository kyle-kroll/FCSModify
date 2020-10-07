module com.kroll.fcsmodify {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.kroll.fcsmodify to javafx.fxml;
    exports com.kroll.fcsmodify;
}

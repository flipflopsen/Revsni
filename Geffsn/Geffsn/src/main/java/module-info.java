module com.geffsn {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.geffsn to javafx.fxml;
    exports com.geffsn;
}

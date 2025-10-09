module com.clinic {
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports com.clinic;
    exports com.clinic.controllers;

    opens com.clinic to javafx.graphics, javafx.fxml;
    opens com.clinic.controllers to javafx.fxml;
    opens com.clinic.model to javafx.base;
}

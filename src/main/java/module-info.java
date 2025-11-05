module org.example.mst_medical_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires jbcrypt;

    opens org.example.mst_medical_app to javafx.fxml;
    exports org.example.mst_medical_app;


    exports org.example.mst_medical_app.controller.admin;
    opens org.example.mst_medical_app.controller.admin to javafx.fxml;
    exports org.example.mst_medical_app.controller.patient;
    opens org.example.mst_medical_app.controller.patient to javafx.fxml;
    exports org.example.mst_medical_app.controller.doctor;
    opens org.example.mst_medical_app.controller.doctor to javafx.fxml;
    exports org.example.mst_medical_app.controller;
    opens org.example.mst_medical_app.controller to javafx.fxml;
    exports org.example.mst_medical_app.controller.components;
    opens org.example.mst_medical_app.controller.components to javafx.fxml;
    exports org.example.mst_medical_app.features.chat;
    opens org.example.mst_medical_app.features.chat to javafx.fxml;
    exports  org.example.mst_medical_app.features.settings;
    opens org.example.mst_medical_app.features.settings to javafx.fxml;
}

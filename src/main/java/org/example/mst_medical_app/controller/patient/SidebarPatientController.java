package org.example.mst_medical_app.controller.patient;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.example.mst_medical_app.controller.MainLayoutController;
import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.core.utils.SceneManager;

public class SidebarPatientController {

    private MainLayoutController mainLayoutController;

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    @FXML private HBox overviewItem;
    @FXML private HBox doctorItem;
    @FXML private HBox appointmentItem;
    @FXML private HBox settingItem;
    @FXML private HBox chatItem;
    @FXML private HBox logoutItem;

    @FXML private Button overviewButton;
    @FXML private Button doctorButton;
    @FXML private Button appointmentButton;
    @FXML private  Button settingButton;
    @FXML private Button chatButton;
    @FXML private Button logoutButton;

    private HBox currentActiveItem;

    @FXML
    public void initialize() {
        setActive(overviewItem);
        bindNav(overviewItem, overviewButton,
                "/org/example/mst_medical_app/patient/PatientDashboardView.fxml");

        bindNav(doctorItem, doctorButton,
                "/org/example/mst_medical_app/patient/Doctor_View.fxml");

        bindNav(appointmentItem, appointmentButton,
                "/org/example/mst_medical_app/admin/Appointments_Calendar_View.fxml");
        bindNav(settingItem, settingButton,
                "/org/example/mst_medical_app/features/settings/SettingView.fxml");

        bindNav(chatItem, chatButton,
                "/org/example/mst_medical_app/features/chat/ChatView.fxml");
    }

    private void bindNav(HBox item, Button btn, String fxml) {
        Runnable go = () -> {
            if (mainLayoutController == null) return;
            mainLayoutController.setContent(fxml);
            setActive(item);
        };
        item.setOnMouseClicked(e -> go.run());
        btn.setOnAction(e -> go.run());
    }

    private void setActive(HBox item) {
        if (currentActiveItem != null)
            currentActiveItem.getStyleClass().remove("sidebar-item-active");

        if (!item.getStyleClass().contains("sidebar-item-active"))
            item.getStyleClass().add("sidebar-item-active");

        currentActiveItem = item;
    }

    public void highlightItem(String key) {
        switch (key) {
            case "appointments" ->setActive(appointmentItem);
            case "chats" ->setActive(chatItem);
        }
    }
    @FXML
    private void handleLogout() {
        AuthManager.logOut();
        SceneManager.switchScene("/org/example/mst_medical_app/LoginView.fxml", "Login");
    }
}

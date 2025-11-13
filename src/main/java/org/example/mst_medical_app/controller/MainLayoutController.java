package org.example.mst_medical_app.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.example.mst_medical_app.controller.admin.SidebarAdminController;
import org.example.mst_medical_app.controller.doctor.SidebarDoctorController;
import org.example.mst_medical_app.controller.patient.SidebarPatientController;
import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.features.chat.ChatController;
import org.example.mst_medical_app.features.chat.ChatOpenData;

import java.io.IOException;

public class MainLayoutController {

    @FXML private BorderPane rootPane;
    @FXML private StackPane contentArea;
    private static MainLayoutController instance;
    private SidebarAdminController sidebarAdmin;
    private SidebarPatientController sidebarPatient;
    private SidebarDoctorController sidebarDoctor;


    public MainLayoutController() {
        instance = this;
    }

    public static MainLayoutController getInstance() {
        return instance;
    }

    @FXML
    public void initialize() {
        loadHeader();
        loadSidebarByRole();

        Platform.runLater(() -> {
            if (AuthManager.isAdmin()) {
                setContent("/org/example/mst_medical_app/admin/AdminDashboardView.fxml");
            } else if (AuthManager.isPatient()) {
                setContent("/org/example/mst_medical_app/patient/PatientDashboardView.fxml");
            } else if (AuthManager.isDoctor()){
                setContent("/org/example/mst_medical_app/doctor/DoctorDashboardView.fxml");
            }
            else {
                System.err.println("Không xác định được role để load Dashboard!");
            }
        });
    }

    private void loadHeader() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/mst_medical_app/components/Header.fxml"
            ));
            HBox header = loader.load();
            rootPane.setTop(header);
        } catch (IOException e) {
            System.err.println("Lỗi load Header: " + e.getMessage());
        }
    }

    private void loadSidebarByRole() {
        try {
            FXMLLoader loader;

            if (AuthManager.isAdmin()) { loader = new FXMLLoader(getClass().getResource("/org/example/mst_medical_app/admin/Sidebar_Admin.fxml"));
            } else if (AuthManager.isPatient()) {
                loader = new FXMLLoader(getClass().getResource("/org/example/mst_medical_app/patient/Sidebar_Patient.fxml"));
            } else if (AuthManager.isDoctor()) {
                loader = new FXMLLoader(getClass().getResource("/org/example/mst_medical_app/doctor/Sidebar_Doctor.fxml"));
            }
            else {
                System.err.println("Không xác định được role để load Sidebar!");
                return;
            }

            VBox sidebar = loader.load();

            Object controller = loader.getController();
            if (controller instanceof SidebarAdminController admin) {
                sidebarAdmin = admin;
                admin.setMainLayoutController(this);
            } else if (controller instanceof SidebarPatientController patient) {
                sidebarPatient = patient;
                patient.setMainLayoutController(this);
            } else if(controller instanceof SidebarDoctorController doctor) {
                sidebarDoctor = doctor;
                doctor.setMainLayoutController(this);
            }
            rootPane.setLeft(sidebar);

        } catch (IOException e) {
            System.err.println("Lỗi load Sidebar: " + e.getMessage());
        }
    }

    public void setContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            Object controller = loader.getController();
            if (controller instanceof org.example.mst_medical_app.controller.doctor.DoctorDashboardController docCtrl) {
                docCtrl.setMainLayoutController(this);
            }
            else if(controller instanceof  org.example.mst_medical_app.controller.patient.PatientDashboardController patientCtrl) {
                patientCtrl.setMainLayoutController(this);
            }
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            System.err.println("Lỗi load Content: " + e.getMessage());
        }
    }

    public void setContent(Node node) {
        contentArea.getChildren().setAll(node);
    }

    public void setActiveSidebar(String key) {
        if(sidebarAdmin != null) {
            sidebarAdmin.highlightItem(key);
        }
        if(sidebarDoctor != null) {
            sidebarDoctor.highlightItem(key);
        }
        if(sidebarPatient != null) {
            sidebarPatient.highlightItem(key);
        }
    }

    public void loadCenterContent(String fxmlPath, Object data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            if (data != null && loader.getController() instanceof ChatController chatController) {

                ChatOpenData openData = (ChatOpenData) data;
                chatController.openConversation(openData.getConversationId(), openData.getDoctor());
            }

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

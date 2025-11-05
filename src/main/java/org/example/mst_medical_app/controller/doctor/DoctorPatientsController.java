package org.example.mst_medical_app.controller.doctor;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.model.Patient;
import org.example.mst_medical_app.service.PatientService;

import java.util.Optional;

/**
 * Controller cho Bác sĩ quản lý bệnh nhân của mình
 */
public class DoctorPatientsController {

    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, String> colName;
    @FXML private TableColumn<Patient, String> colGender;
    @FXML private TableColumn<Patient, String> colDob;
    @FXML private TableColumn<Patient, String> colAddress;
    @FXML private TableColumn<Patient, Void> colAction;

    @FXML private TextField searchField;

    private final PatientService patientService = new PatientService();
    private ObservableList<Patient> masterPatientsList;

    @FXML
    public void initialize() {
        setupTable();
        loadPatients();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    /**
     * Cấu hình bảng hiển thị
     */
    private void setupTable() {
        colName.setCellValueFactory(cellData -> cellData.getValue().fullNameProperty());
        colGender.setCellValueFactory(cellData -> cellData.getValue().genderProperty());
        colDob.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDateOfBirth() != null)
                return new ReadOnlyStringWrapper(cellData.getValue().getDateOfBirth().toString());
            else
                return new ReadOnlyStringWrapper("-");
        });
        colAddress.setCellValueFactory(cellData -> cellData.getValue().addressProperty());

        // Cột hành động
        colAction.setCellFactory(tc -> new TableCell<>() {
            private final Button viewBtn = new Button("👁 Xem");
            private final Button editBtn = new Button("✏ Sửa");

            {
                viewBtn.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-background-radius: 8;");
                editBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 8;");

                viewBtn.setOnAction(e -> handleViewPatient(getTableView().getItems().get(getIndex())));
                editBtn.setOnAction(e -> handleEditPatient(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(new HBox(10, viewBtn, editBtn));
            }
        });
    }

    /**
     * Tải danh sách bệnh nhân thuộc bác sĩ hiện tại
     */
    private void loadPatients() {
        this.masterPatientsList = patientService.getPatientsForCurrentDoctor();
        patientsTable.setItems(masterPatientsList);
    }

    /**
     * Lọc bệnh nhân theo tên hoặc địa chỉ
     */
    private void applyFilter() {
        String searchText = searchField.getText().trim();
        ObservableList<Patient> filtered = FXCollections.observableArrayList(
                masterPatientsList.stream()
                        .filter(p ->
                                p.getFullName().toLowerCase().contains(searchText.toLowerCase()) ||
                                        p.getAddress().toLowerCase().contains(searchText.toLowerCase()))
                        .toList()
        );
        patientsTable.setItems(filtered);
    }

    /**
     * Xem chi tiết bệnh nhân
     */
    private void handleViewPatient(Patient patient) {
        String info = String.format("""
                🧍 Họ tên: %s
                ⚧ Giới tính: %s
                📅 Ngày sinh: %s
                🏠 Địa chỉ: %s
                👨‍⚕️ Bác sĩ phụ trách: %s
                """,
                patient.getFullName(),
                patient.getGender(),
                patient.getDateOfBirth() != null ? patient.getDateOfBirth() : "-",
                patient.getAddress(),
                AuthManager.getFullName()
        );

        showAlert(Alert.AlertType.INFORMATION, "Thông tin bệnh nhân", info);
    }

    /**
     * Sửa địa chỉ bệnh nhân
     */
    private void handleEditPatient(Patient patient) {
        TextInputDialog dialog = new TextInputDialog(patient.getAddress());
        dialog.setTitle("Cập nhật thông tin bệnh nhân");
        dialog.setHeaderText("Cập nhật địa chỉ cho " + patient.getFullName());
        dialog.setContentText("Nhập địa chỉ mới:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newAddress -> {
            if (newAddress.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Địa chỉ không được để trống!");
                return;
            }

            patient.setAddress(newAddress);
            String error = patientService.updatePatientInfo(patient);
            if (error == null) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật địa chỉ bệnh nhân!");
                patientsTable.refresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", error);
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

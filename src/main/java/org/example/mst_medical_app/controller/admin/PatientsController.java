package org.example.mst_medical_app.controller.admin;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.mst_medical_app.model.Patient;
import org.example.mst_medical_app.service.PatientService;

import java.util.Optional;

/**
 * Controller: Admin quản lý danh sách bệnh nhân
 */
public class PatientsController {

    @FXML private TableView<Patient> patientsTable;
    @FXML private TableColumn<Patient, Integer> idColumn;
    @FXML private TableColumn<Patient, String> nameColumn;
    @FXML private TableColumn<Patient, String> genderColumn;
    @FXML private TableColumn<Patient, String> addressColumn;
    @FXML private TableColumn<Patient, String> dobColumn;

    @FXML private TextField searchField;
    @FXML private Button refreshBtn;
    @FXML private Button deleteBtn;
    @FXML private Button editBtn;

    private final PatientService patientService = new PatientService();
    private ObservableList<Patient> masterList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadPatients();

        // Sự kiện tìm kiếm, refresh, xóa
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
        refreshBtn.setOnAction(e -> loadPatients());
        deleteBtn.setOnAction(e -> handleDelete());
        editBtn.setOnAction(e -> handleEdit());
    }

     // Cấu hình cột TableView
    private void setupTable() {
        idColumn.setCellValueFactory(data -> data.getValue().patientIdProperty().asObject());
        nameColumn.setCellValueFactory(data -> data.getValue().fullNameProperty());
        genderColumn.setCellValueFactory(data -> data.getValue().genderProperty());
        addressColumn.setCellValueFactory(data -> data.getValue().addressProperty());
        dobColumn.setCellValueFactory(data -> {
            if (data.getValue().getDateOfBirth() != null)
                return new ReadOnlyStringWrapper(data.getValue().getDateOfBirth().toString());
            else
                return new ReadOnlyStringWrapper("-");
        });
    }


    // Load danh sách bệnh nhân
    private void loadPatients() {
        masterList = patientService.getAllPatients();
        patientsTable.setItems(masterList);
        applyFilter();
    }

    // Lọc bệnh nhân theo tên hoặc địa chỉ
    private void applyFilter() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            patientsTable.setItems(masterList);
            return;
        }
        ObservableList<Patient> filtered = FXCollections.observableArrayList(
                masterList.stream()
                        .filter(p ->
                                p.getFullName().toLowerCase().contains(keyword) ||
                                        p.getAddress().toLowerCase().contains(keyword))
                        .toList()
        );
        patientsTable.setItems(filtered);
    }

    // Xóa bệnh nhân
    private void handleDelete() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn bệnh nhân", "Vui lòng chọn một bệnh nhân để xoá.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xoá");
        confirm.setHeaderText("Bạn có chắc muốn xoá bệnh nhân " + selected.getFullName() + "?");
        confirm.setContentText("Hành động này không thể hoàn tác.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean success = patientService.deletePatient(selected);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xoá bệnh nhân!");
                loadPatients();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xoá bệnh nhân!");
            }
        }
    }

    // Sửa thông tin bệnh nhân
    private void handleEdit() {
        Patient selected = patientsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn bệnh nhân", "Vui lòng chọn bệnh nhân để chỉnh sửa.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selected.getAddress());
        dialog.setTitle("Chỉnh sửa địa chỉ");
        dialog.setHeaderText("Cập nhật địa chỉ cho bệnh nhân: " + selected.getFullName());
        dialog.setContentText("Nhập địa chỉ mới:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newAddress -> {
            if (newAddress.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Địa chỉ không được để trống!");
                return;
            }

            selected.setAddress(newAddress);
            String error = patientService.updatePatientInfo(selected);
            if (error == null) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin bệnh nhân!");
                patientsTable.refresh();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", error);
            }
        });
    }

    // Hiển thị thông báo
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

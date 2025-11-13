package org.example.mst_medical_app.controller.admin;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.example.mst_medical_app.model.Doctor;
import org.example.mst_medical_app.service.DoctorService;

import java.io.IOException;

public class DoctorsController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> specializationFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private Button addDoctorBtn;

    @FXML private TableView<Doctor> doctorTable;
    @FXML private TableColumn<Doctor, String> colName;
    @FXML private TableColumn<Doctor, String> colSpecialization;
    @FXML private TableColumn<Doctor, Integer> colExperience;
    @FXML private TableColumn<Doctor, String> colLicense;
    @FXML private TableColumn<Doctor, String> colStatus;
    @FXML private TableColumn<Doctor, Void> colActions;

    private final DoctorService doctorService = new DoctorService();

    @FXML
    public void initialize() {
        // setup combo filter
        specializationFilter.getItems().addAll("All", "Dermatology", "Surgery", "Aesthetic", "Cardiology");
        specializationFilter.setValue("All");
        statusFilter.getItems().addAll("All", "Available", "Busy", "Inactive");
        statusFilter.setValue("All");

        setupTableColumns();
        loadDoctors();

        // event listeners
        searchField.textProperty().addListener((obs, oldV, newV) -> loadDoctors());
        specializationFilter.valueProperty().addListener((obs, o, n) -> loadDoctors());
        statusFilter.valueProperty().addListener((obs, o, n) -> loadDoctors());
        addDoctorBtn.setOnAction(e -> openAddDoctorDialog());
    }

    /** setup cột cho TableView **/
    private void setupTableColumns() {
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colSpecialization.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        colExperience.setCellValueFactory(new PropertyValueFactory<>("experienceYears"));
        colLicense.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // cột actions
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setStyle("-fx-background-color:#3B82F6; -fx-text-fill:white; -fx-background-radius:6;");
                deleteBtn.setStyle("-fx-background-color:#EF4444; -fx-text-fill:white; -fx-background-radius:6;");

                editBtn.setOnAction(e -> handleEditDoctor(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e -> handleDeleteDoctor(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    HBox box = new HBox(8, editBtn, deleteBtn);
                    setGraphic(box);
                }
            }
        });
    }

    /** load danh sách bác sĩ **/
    private void loadDoctors() {
        ObservableList<Doctor> doctors = doctorService.searchDoctors(
                searchField.getText(),
                specializationFilter.getValue()
        );
        doctorTable.setItems(doctors);
    }

    /** mở popup Add Doctor (form riêng) **/
    private void openAddDoctorDialog() {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Add Doctor");

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/mst_medical_app/admin/AddDoctorDialog.fxml"
            ));
            dialog.getDialogPane().setContent(loader.load());
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

            dialog.showAndWait();
            loadDoctors(); // refresh table sau khi thêm

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot open Add Doctor form.");
        }
    }

    /** chỉnh sửa bác sĩ **/
    private void handleEditDoctor(Doctor doctor) {
        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Doctor");

            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/mst_medical_app/admin/EditDoctorDialog.fxml"
            ));
            dialog.getDialogPane().setContent(loader.load());
//            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

            // Truyền dữ liệu bác sĩ sang form
            EditDoctorDialogController controller = loader.getController();
            controller.setDoctor(doctor);

            dialog.showAndWait();
            loadDoctors();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Không thể mở form chỉnh sửa.");
        }
    }


    private void handleDeleteDoctor(Doctor doctor) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa bác sĩ");
        confirm.setHeaderText("Bạn có chắc muốn xóa " + doctor.getFullName() + "?");
        confirm.setContentText("Hành động này không thể hoàn tác.");

        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                boolean success = doctorService.deleteDoctor(doctor);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa bác sĩ!");
                    loadDoctors();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa bác sĩ!");
                }
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

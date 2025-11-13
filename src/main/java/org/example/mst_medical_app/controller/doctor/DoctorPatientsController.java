package org.example.mst_medical_app.controller.doctor;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.mst_medical_app.model.AppointmentRepository;
import org.example.mst_medical_app.model.Patient;
import org.example.mst_medical_app.service.AppointmentService;
import org.example.mst_medical_app.service.PatientService;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DoctorPatientsController {

    @FXML private TableView<Patient> patientsTable;

    @FXML private TableColumn<Patient, String> colName;
    @FXML private TableColumn<Patient, String> colGender;
    @FXML private TableColumn<Patient, String> colDob;
    @FXML private TableColumn<Patient, String> colAddress;
    @FXML private TableColumn<Patient, String> colStatus;
    @FXML private TableColumn<Patient, String> colNote;

    @FXML private TextField searchField;

    @FXML private Button viewBtn;
    @FXML private Button noteBtn;
    @FXML private Button historyBtn;

    private final PatientService patientService = new PatientService();
    private final AppointmentService appointmentService = new AppointmentService();


    private final AppointmentRepository appointmentRepository = new AppointmentRepository();

    private ObservableList<Patient> masterPatientsList;

    @FXML
    public void initialize() {
        setupTable();
        loadPatients();

        disableActions(true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());

        viewBtn.setOnAction(e -> handleViewPatient());
        noteBtn.setOnAction(e -> handleMedicalNote());
        historyBtn.setOnAction(e -> handleViewHistory());

        patientsTable.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            disableActions(n == null);
        });
    }

    private void disableActions(boolean disabled) {
        viewBtn.setDisable(disabled);
        noteBtn.setDisable(disabled);
        historyBtn.setDisable(disabled);
    }

    private void setupTable() {
        colName.setCellValueFactory(c -> c.getValue().fullNameProperty());
        colGender.setCellValueFactory(c -> c.getValue().genderProperty());

        colDob.setCellValueFactory(c -> {
            if (c.getValue().getDateOfBirth() != null) {
                return new ReadOnlyStringWrapper(
                        c.getValue().getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                );
            }
            return new ReadOnlyStringWrapper("-");
        });

        colAddress.setCellValueFactory(c -> c.getValue().addressProperty());

        colStatus.setCellValueFactory(c -> {
            String status = c.getValue().getAppointmentStatus();
            return new ReadOnlyStringWrapper(status != null ? status : "-");
        });

        colNote.setCellValueFactory(c -> {
            String note = c.getValue().getMedicalNote();
            return new ReadOnlyStringWrapper(note != null ? note : "—");
        });
    }


    private void loadPatients() {
        masterPatientsList = patientService.getPatientsForCurrentDoctor();

        for (Patient p : masterPatientsList) {
            String latestStatus = appointmentRepository.getLatestStatusByPatientId(p.getPatientId());
            p.setAppointmentStatus(latestStatus != null ? latestStatus : "-");
        }

        patientsTable.setItems(masterPatientsList);
    }

    private void applyFilter() {
        String s = searchField.getText().trim().toLowerCase();

        if (s.isEmpty()) {
            patientsTable.setItems(masterPatientsList);
            return;
        }

        ObservableList<Patient> filtered = FXCollections.observableArrayList(
                masterPatientsList.stream()
                        .filter(p -> p.getFullName().toLowerCase().contains(s)
                                || p.getAddress().toLowerCase().contains(s))
                        .toList()
        );

        patientsTable.setItems(filtered);
    }

    private void handleViewPatient() {
        Patient patient = patientsTable.getSelectionModel().getSelectedItem();
        if (patient == null) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        String info = """
                Họ tên: %s
                Giới tính: %s
                Ngày sinh: %s
                Địa chỉ: %s
                Tình trạng cuộc hẹn: %s
                Ghi chú y tế: %s
                """.formatted(
                patient.getFullName(),
                patient.getGender(),
                patient.getDateOfBirth() != null ? patient.getDateOfBirth().format(formatter) : "-",
                patient.getAddress(),
                patient.getAppointmentStatus(),
                patient.getMedicalNote()
        );

        showAlert(Alert.AlertType.INFORMATION, "Thông tin bệnh nhân", info);
    }

    private void handleMedicalNote() {
        Patient patient = patientsTable.getSelectionModel().getSelectedItem();
        if (patient == null) return;

        TextInputDialog dialog = new TextInputDialog(patient.getMedicalNote());
        dialog.setTitle("Ghi chú bệnh");
        dialog.setHeaderText("Nhập ghi chú y tế cho bệnh nhân: " + patient.getFullName());
        dialog.setContentText("Ghi chú:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(note -> {
            if (note.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Ghi chú không được để trống!");
                return;
            }

            String error = patientService.updateMedicalNote(patient, note);

            if (error == null) {
                patient.setMedicalNote(note);
                patientsTable.refresh();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu ghi chú.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", error);
            }
        });
    }

    private void handleViewHistory() {
        Patient p = patientsTable.getSelectionModel().getSelectedItem();
        if (p == null) return;

        showAlert(Alert.AlertType.INFORMATION, "Lịch sử khám",
                "Tính năng lịch sử khám sẽ được bổ sung ở phần Appointment History.");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}

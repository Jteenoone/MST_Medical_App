package org.example.mst_medical_app.controller.patient;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import org.example.mst_medical_app.model.AppointmentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class BookAppointmentPopupController {

    @FXML private DatePicker appointmentDatePicker;
    @FXML private ComboBox<String> timeSlotCombo;
    @FXML private TextArea noteField;

    private int doctorId;

    private final AppointmentRepository appointmentRepository = new AppointmentRepository();

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    @FXML
    public void initialize() {
        appointmentDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> loadAvailableTimeSlots());
    }

    private void loadAvailableTimeSlots() {
        timeSlotCombo.getItems().clear();

        LocalDate date = appointmentDatePicker.getValue();
        if (date == null) return;

        // Giờ bác sĩ đã bận (trừ canceled)
        List<LocalTime> bookedSlots =
                appointmentRepository.getBookedTimesForDoctor(doctorId, date);

        // Danh sách slot
        List<LocalTime> allSlots = List.of(
                LocalTime.of(8, 0),
                LocalTime.of(9, 0),
                LocalTime.of(10, 0),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                LocalTime.of(15, 0)
        );

        allSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .map(LocalTime::toString)
                .forEach(timeSlotCombo.getItems()::add);
    }

    /** Gọi từ Main UI để lấy kết quả */
    public LocalDateTime getSelectedDateTime() {
        if (appointmentDatePicker.getValue() == null || timeSlotCombo.getValue() == null)
            return null;

        return LocalDateTime.of(appointmentDatePicker.getValue(),
                LocalTime.parse(timeSlotCombo.getValue()));
    }

    public String getNote() {
        return noteField.getText();
    }
}

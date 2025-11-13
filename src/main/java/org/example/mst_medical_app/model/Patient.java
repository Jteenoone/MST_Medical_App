package org.example.mst_medical_app.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Patient {
    private final IntegerProperty patientId = new SimpleIntegerProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty gender = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateOfBirth = new SimpleObjectProperty<>();
    private final StringProperty address = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private StringProperty appointmentStatus = new SimpleStringProperty("Chưa có");
    private StringProperty medicalNote = new SimpleStringProperty("");
    public Patient() {}
    public Patient(int id, String fullName, String gender, LocalDate dateOfBirth, String address) {
        this.patientId.set(id);
        this.fullName.set(fullName);
        this.gender.set(gender);
        this.dateOfBirth.set(dateOfBirth);
        this.address.set(address);
    }

    public int getPatientId() { return patientId.get(); }
    public IntegerProperty patientIdProperty() { return patientId; }

    public String getFullName() { return fullName.get(); }
    public StringProperty fullNameProperty() { return fullName; }

    public String getGender() { return gender.get(); }
    public StringProperty genderProperty() { return gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth.get(); }
    public ObjectProperty<LocalDate> dateOfBirthProperty() { return dateOfBirth; }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }
    public void setEmail(String email) { this.email.set(email); }

    public String getPhone() { return phone.get(); }
    public StringProperty phoneProperty() { return phone; }
    public void setPhone(String phone) { this.phone.set(phone); }

    public String getAppointmentStatus() { return appointmentStatus.get(); }
    public void setAppointmentStatus(String status) { this.appointmentStatus.set(status); }
    public StringProperty appointmentStatusProperty() { return appointmentStatus; }


    public String getMedicalNote() { return medicalNote.get(); }
    public void setMedicalNote(String note) { this.medicalNote.set(note); }
    public StringProperty medicalNoteProperty() { return medicalNote; }

    public String getAddress() { return address.get(); }
    public StringProperty addressProperty() { return address; }

    public void setPatientId(int patientId) { this.patientId.set(patientId); }
    public void setFullName(String fullName) { this.fullName.set(fullName); }
    public void setGender(String gender) { this.gender.set(gender); }
    public void setDateOfBirth(LocalDate date) { this.dateOfBirth.set(date); }
    public void setAddress(String address) { this.address.set(address); }
}

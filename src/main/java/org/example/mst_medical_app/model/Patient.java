package org.example.mst_medical_app.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Patient {
    private final IntegerProperty patientId = new SimpleIntegerProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty gender = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> dateOfBirth = new SimpleObjectProperty<>();
    private final StringProperty address = new SimpleStringProperty();

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

    public String getAddress() { return address.get(); }
    public StringProperty addressProperty() { return address; }

    public void setGender(String gender) { this.gender.set(gender); }
    public void setDateOfBirth(LocalDate date) { this.dateOfBirth.set(date); }
    public void setAddress(String address) { this.address.set(address); }
}

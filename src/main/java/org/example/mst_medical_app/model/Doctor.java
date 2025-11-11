package org.example.mst_medical_app.model;

import javafx.beans.property.*;


public class Doctor {
    private final IntegerProperty doctorId = new SimpleIntegerProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();
    private final StringProperty fullName = new SimpleStringProperty();
    private final StringProperty specialization = new SimpleStringProperty();
    private final IntegerProperty experienceYears = new SimpleIntegerProperty();
    private final StringProperty licenseNumber = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final DoubleProperty rating = new SimpleDoubleProperty();
    private final IntegerProperty reviews = new SimpleIntegerProperty();

    // Constructor
    public Doctor() {}
    public Doctor(int doctorId, int userId, String fullName, String specialization,
                  int experienceYears, String licenseNumber,
                  String phone, String email) {
        this.doctorId.set(doctorId);
        this.userId.set(userId);
        this.fullName.set(fullName);
        this.specialization.set(specialization);
        this.experienceYears.set(experienceYears);
        this.licenseNumber.set(licenseNumber);
        this.phone.set(phone);
        this.email.set(email);
        this.status.set("Available");
        this.rating.set(4.5);
        this.reviews.set(0);
    }


    public Doctor(int id, String name, String spec, String phone, String status, double rating, int reviews) {
        this.doctorId.set(id);
        this.fullName.set(name);
        this.specialization.set(spec);
        this.phone.set(phone);
        this.status.set(status);
        this.rating.set(rating);
        this.reviews.set(reviews);
    }

    // === GETTERS & PROPERTIES ===

    public int getDoctorId() { return doctorId.get(); }
    public IntegerProperty doctorIdProperty() { return doctorId; }

    public int getUserId() { return userId.get(); }
    public IntegerProperty userIdProperty() { return userId; }

    public String getFullName() { return fullName.get(); }
    public StringProperty fullNameProperty() { return fullName; }
    public void setFullName(String name) { this.fullName.set(name); }

    public String getSpecialization() { return specialization.get(); }
    public StringProperty specializationProperty() { return specialization; }
    public void setSpecialization(String spec) { this.specialization.set(spec); }

    public int getExperienceYears() { return experienceYears.get(); }
    public IntegerProperty experienceYearsProperty() { return experienceYears; }
    public void setExperienceYears(int years) { this.experienceYears.set(years); }

    public String getLicenseNumber() { return licenseNumber.get(); }
    public StringProperty licenseNumberProperty() { return licenseNumber; }
    public void setLicenseNumber(String license) { this.licenseNumber.set(license); }

    public String getPhone() { return phone.get(); }
    public StringProperty phoneProperty() { return phone; }
    public void setPhone(String phone) { this.phone.set(phone); }

    public String getEmail() { return email.get(); }
    public StringProperty emailProperty() { return email; }
    public void setEmail(String email) { this.email.set(email); }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }
    public void setStatus(String status) { this.status.set(status); }

    public double getRating() { return rating.get(); }
    public DoubleProperty ratingProperty() { return rating; }
    public void setRating(double rating) { this.rating.set(rating); }

    public int getReviews() { return reviews.get(); }
    public IntegerProperty reviewsProperty() { return reviews; }
    public void setReviews(int reviews) { this.reviews.set(reviews); }
    public void setUserId(int userId) {
        this.userId.set(userId);
    }

}

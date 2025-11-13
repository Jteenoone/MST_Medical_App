package org.example.mst_medical_app.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.model.*;

import java.time.LocalDate;

/**
 * Service xử lý nghiệp vụ của bệnh nhân.
 */
public class PatientService {

    private final PatientRepository patientRepository = new PatientRepository();
    private final AppointmentRepository appointmentRepo = new AppointmentRepository();


    /** Lấy tất cả bệnh nhân (admin) */
    public ObservableList<Patient> getAllPatients() {
        return patientRepository.findAllPatients();
    }

    /** Lấy bệnh nhân cho bác sĩ hiện tại */
    public ObservableList<Patient> getPatientsForCurrentDoctor() {
        UserModel user = AuthManager.getCurUser();
        if (user == null || !AuthManager.isDoctor()) {
            return FXCollections.observableArrayList();
        }
        return patientRepository.findPatientsByDoctorId(getDoctorIdByUserId(user.getId()));
    }

    /** Cập nhật thông tin bệnh nhân */
    public String updatePatientInfo(Patient patient) {
        if (patient == null) return "Bệnh nhân không hợp lệ.";

        boolean success = patientRepository.updatePatient(patient);
        return success ? null : "Cập nhật thất bại! (Lỗi CSDL)";
    }

    /** Tìm kiếm nhanh (admin) */
    public ObservableList<Patient> searchPatients(String searchText) {
        ObservableList<Patient> list = getAllPatients();
        if (searchText == null || searchText.isEmpty()) return list;

        String lower = searchText.toLowerCase();
        return list.filtered(p ->
                p.getFullName().toLowerCase().contains(lower)
                        || p.getAddress().toLowerCase().contains(lower)
        );
    }

    /** Xóa bệnh nhân */
    public boolean deletePatient(Patient patient) {
        if (patient == null) return false;
        return patientRepository.deletePatient(patient.getPatientId());
    }

    public ObservableList<Patient> getPatientsByDoctorId(int doctorId) {
        return patientRepository.findPatientsByDoctorId(doctorId);
    }

    private int getDoctorIdByUserId(int userId) {
        try(var conn = org.example.mst_medical_app.core.database.DatabaseConnection.getConnection();
        var ps = conn.prepareStatement("SELECT doctor_id FROM doctors WHERE user_id = ?")) {
            ps.setInt(1,userId);
            var rs = ps.executeQuery();

            if(rs.next()) {
                return rs.getInt("doctor_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getCountDoctor(int patientId) {
        return patientRepository.countDoctor(patientId);
    }

    public int getContAppointment(int patientId) {
        return patientRepository.countAppointment(patientId);
    }

    public String updatePatientFull(Patient patient, String fullName, String email, String phone, LocalDate dob, String gender, String address) {
        if(patient == null)
            return "không có bệnh nhân này";
        patient.setFullName(fullName);
        patient.setEmail(email);
        patient.setPhone(phone);
        patient.setDateOfBirth(dob);
        patient.setGender(gender);
        patient.setAddress(address);
        boolean success = patientRepository.updatePatient(patient);
        return success ? null : "Lỗi khi chỉnh sửa bệnh nhân";
    }

    public String updateMedicalNote(Patient patient, String note) {
        if(patient == null)
            return "Không có bệnhh nhân này";
        patient.setMedicalNote(note);
        boolean success = patientRepository.updateMedicalNote(patient);
        return success ? null : "Lỗi không thể ghi chú";
    }






}

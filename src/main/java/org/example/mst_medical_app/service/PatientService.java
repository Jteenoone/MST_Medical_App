package org.example.mst_medical_app.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.model.Patient;
import org.example.mst_medical_app.model.PatientRepository;
import org.example.mst_medical_app.model.UserModel;

/**
 * Service xử lý nghiệp vụ của bệnh nhân.
 */
public class PatientService {

    private final PatientRepository patientRepository = new PatientRepository();

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

}

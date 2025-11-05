package org.example.mst_medical_app.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.example.mst_medical_app.model.Doctor;
import org.example.mst_medical_app.model.DoctorRepository;
import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.model.UserModel;

/**
 * Service xử lý nghiệp vụ liên quan đến Bác sĩ.
 */
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService() {
        this.doctorRepository = new DoctorRepository();
    }

    /**
     * Lấy toàn bộ danh sách bác sĩ
     */
    public ObservableList<Doctor> getAllDoctors() {
        return doctorRepository.findAllDoctors();
    }

    /**
     * Tìm kiếm bác sĩ theo từ khóa & chuyên khoa
     */
    public ObservableList<Doctor> searchDoctors(String keyword, String specialization) {
        ObservableList<Doctor> all = doctorRepository.findAllDoctors();

        return all.filtered(d ->
                (keyword == null || keyword.isBlank() ||
                        d.getFullName().toLowerCase().contains(keyword.toLowerCase())) &&
                        ("All".equals(specialization) || d.getSpecialization().equalsIgnoreCase(specialization))
        );
    }

    /**
     * Lấy danh sách bác sĩ phù hợp với người dùng hiện tại
     */
    public ObservableList<Doctor> getDoctorsForCurrentUser() {
        UserModel currentUser = AuthManager.getCurUser();
        if (currentUser == null) {
            return FXCollections.observableArrayList();
        }

        if (AuthManager.isAdmin()) {
            return doctorRepository.findAllDoctors();
        }

        if (AuthManager.isDoctor()) {
            // Nếu là bác sĩ, chỉ hiển thị thông tin của chính họ
            return doctorRepository.findByDoctorId(currentUser.getId());
        }

        if (AuthManager.isPatient()) {
            // Nếu là bệnh nhân, có thể hiển thị tất cả bác sĩ
            return doctorRepository.findAllDoctors();
        }

        return FXCollections.observableArrayList();
    }

    /**
     * Thêm bác sĩ mới (khi đã có user tương ứng)
     */
    public String addNewDoctor(int userId, String specialization, int experienceYears, String licenseNumber) {
        if (userId <= 0) {
            return "Người dùng (user_id) không hợp lệ.";
        }
        if (specialization == null || specialization.trim().isEmpty()) {
            return "Chuyên khoa không được để trống.";
        }

        Doctor doctor = new Doctor();
        doctor.setUserId(userId);
        doctor.setSpecialization(specialization);
        doctor.setExperienceYears(experienceYears);
        doctor.setLicenseNumber(licenseNumber);

        boolean success = doctorRepository.createDoctor(doctor);
        return success ? null : "Thêm bác sĩ thất bại! (Lỗi CSDL)";
    }

    /**
     * Cập nhật thông tin bác sĩ
     */
    public String updateDoctor(Doctor doctor, String newSpec, int newExp, String newLicense) {
        if (doctor == null) {
            return "Bác sĩ không hợp lệ.";
        }

        doctor.setSpecialization(newSpec);
        doctor.setExperienceYears(newExp);
        doctor.setLicenseNumber(newLicense);

        boolean success = doctorRepository.updateDoctor(doctor);
        return success ? null : "Cập nhật thất bại! (Lỗi CSDL)";
    }

    /**
     * Xóa bác sĩ khỏi hệ thống
     */
    public boolean deleteDoctor(Doctor doctor) {
        if (doctor == null) return false;
        return doctorRepository.deleteDoctor(doctor.getDoctorId());
    }
}

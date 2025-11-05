package org.example.mst_medical_app.service;

import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.model.Appointment;
import org.example.mst_medical_app.model.AppointmentRepository;
import org.example.mst_medical_app.model.UserModel;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Xử lý logic liên quan đến Lịch hẹn, tương thích database medical_app.
 */
public class AppointmentService {

    private final AppointmentRepository repository;

    public AppointmentService() {
        this.repository = new AppointmentRepository();
    }

    // =========================
    // 1Lấy lịch theo người dùng hiện tại
    // =========================
    public List<Appointment> getAppointmentsForCurrentUser() {
        UserModel currentUser = AuthManager.getCurUser();
        System.out.println(">>> Current user: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        System.out.println("Role check: Admin=" + AuthManager.isAdmin() +
                ", Doctor=" + AuthManager.isDoctor() +
                ", Patient=" + AuthManager.isPatient());
        if (currentUser == null) return List.of();

        if (AuthManager.isAdmin()) {
            return repository.findAll();
        }

        if (AuthManager.isDoctor()) {
            return repository.findByDoctorId(currentUser.getId());
        }

        if (AuthManager.isPatient()) {
            return repository.findByPatientId(currentUser.getId());
        }

        return List.of();
    }

    // =========================
    // Tạo lịch hẹn mới
    // =========================
    /**
     * Bệnh nhân hoặc Admin tạo lịch hẹn.
     * @param appointmentTime thời gian đặt lịch
     * @param doctorId bác sĩ được chọn
     * @param notes ghi chú thêm
     * @return null nếu thành công, hoặc thông báo lỗi
     */
    public String createAppointment(LocalDateTime appointmentTime, int doctorId, String notes) {
        UserModel currentUser = AuthManager.getCurUser();

        if (currentUser == null || (!AuthManager.isPatient() && !AuthManager.isAdmin())) {
            return "Chỉ bệnh nhân hoặc Admin mới được tạo lịch.";
        }

        // Không cho đặt lịch trong quá khứ
        if (appointmentTime.isBefore(LocalDateTime.now())) {
            return "Không thể đặt lịch trong quá khứ.";
        }

        // Tạo đối tượng Appointment mới
        Appointment newAppt = new Appointment(
                0, // auto increment
                currentUser.getId(), // patient_id
                doctorId,
                appointmentTime,
                Appointment.Status.PENDING,
                notes
        );

        boolean success = repository.create(newAppt);
        return success ? null : "Lỗi CSDL, không thể tạo lịch hẹn.";
    }

    // =========================
    // 3️⃣ Hủy lịch hẹn
    // =========================
    public String cancelAppointment(int appointmentId) {
        boolean success = repository.updateStatus(appointmentId, Appointment.Status.CANCELED);
        return success ? null : "Không thể hủy lịch hẹn.";
    }

    // =========================
    // 4️⃣ Cập nhật trạng thái (Doctor hoặc Admin)
    // =========================
    public String updateAppointmentStatus(int appointmentId, Appointment.Status newStatus) {
        if (!AuthManager.isAdmin() && !AuthManager.isDoctor()) {
            return "Bạn không có quyền cập nhật trạng thái này.";
        }

        boolean success = repository.updateStatus(appointmentId, newStatus);
        return success ? null : "Không thể cập nhật trạng thái.";
    }

    public List<Appointment> findUpcomingAppointmentsByDoctorId(int doctorId) {
        return repository.findByDoctorId(doctorId)
                .stream()
                .filter(a -> a.getAppointmentTime().isAfter(LocalDateTime.now()))
                .limit(5)
                .toList();
    }

    public List<Appointment> findPreviousAppointmentsByDoctorId(int doctorId) {
        return repository.findByDoctorId(doctorId)
                .stream()
                .filter(a -> a.getAppointmentTime().isBefore(LocalDateTime.now()))
                .limit(5)
                .toList();
    }


}

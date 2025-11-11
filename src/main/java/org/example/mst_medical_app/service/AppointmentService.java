package org.example.mst_medical_app.service;

import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.model.Appointment;
import org.example.mst_medical_app.model.AppointmentRepository;
import org.example.mst_medical_app.model.UserModel;

import java.time.LocalDateTime;
import java.util.List;


public class AppointmentService {

    private final AppointmentRepository repository;

    public AppointmentService() {
        this.repository = new AppointmentRepository();
    }

    // Lấy lịch theo người dùng hiện tại
    public List<Appointment> getAppointmentsForCurrentUser() {
        UserModel currentUser = AuthManager.getCurUser();

        if (currentUser == null) return List.of();

        if (AuthManager.isAdmin()) {
            return repository.findAll();
        }
        if (AuthManager.isDoctor()) {
            return repository.findByDoctorId(getDoctorIdByUserId(currentUser.getId()));
        }
        if (AuthManager.isPatient()) {
            return repository.findByPatientId(currentUser.getId());
        }
        return List.of();
    }



    public String cancelAppointment(int appointmentId) {
        boolean success = repository.updateStatus(appointmentId, Appointment.Status.CANCELED);
        return success ? null : "Không thể hủy lịch hẹn.";
    }

    public boolean updateAppointmentStatus(int appointmentId, Appointment.Status newStatus) {
        if (!AuthManager.isAdmin() && !AuthManager.isDoctor()) {
            return false;
        }
        boolean success = repository.updateStatus(appointmentId, newStatus);
        return success ? true : false;
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


    public int getDoctorIdByUserId(int userId) {
        try (var conn = org.example.mst_medical_app.core.database.DatabaseConnection.getConnection();
             var ps = conn.prepareStatement("SELECT doctor_id FROM doctors WHERE user_id = ?")) {

            ps.setInt(1, userId);
            var rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("doctor_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public List<String> getDoctorByPatientId(int patientID) {
        return repository.findByPatientId(patientID).stream().map(Appointment::getDoctorName).distinct().toList();
    }

    public Integer bookAppointment(int patientId, int doctorId, LocalDateTime dateTime, String notes) {
        if (repository.existsAppointmentAt(doctorId, dateTime)) {
            return null;
        }

        return repository.createAppointment(patientId, doctorId, dateTime, notes);
    }

    public boolean confirmAppointment(int appointmentId) {
        return repository.updateStatus(appointmentId, Appointment.Status.CONFIRMED);
    }

}

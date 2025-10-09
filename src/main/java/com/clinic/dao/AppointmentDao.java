package com.clinic.dao;

import com.clinic.model.Appointment;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentDao {
    int create(Appointment appointment);
    boolean update(Appointment appointment);
    boolean delete(int id);
    Optional<Appointment> findById(int id);
    List<Appointment> listByDateRange(LocalDateTime from, LocalDateTime to, int limit, int offset);
    List<Appointment> listByPatient(int patientId, int limit, int offset);
}

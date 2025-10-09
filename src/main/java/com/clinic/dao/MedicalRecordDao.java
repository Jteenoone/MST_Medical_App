package com.clinic.dao;

import com.clinic.model.MedicalRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MedicalRecordDao {
    int create(MedicalRecord record);
    boolean update(MedicalRecord record);
    boolean delete(int id);
    Optional<MedicalRecord> findById(int id);
    List<MedicalRecord> listByPatient(int patientId, int limit, int offset);
    List<MedicalRecord> listByDate(LocalDate from, LocalDate to, int limit, int offset);
}

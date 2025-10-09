package com.clinic.dao;

import com.clinic.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientDao {
    int create(Patient patient);
    boolean update(Patient patient);
    boolean delete(int id);
    Optional<Patient> findById(int id);
    List<Patient> search(String keyword, int limit, int offset);
}

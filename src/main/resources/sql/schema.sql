-- SQL Server schema for Clinic Manager

CREATE TABLE patients (
    id INT IDENTITY(1,1) PRIMARY KEY,
    full_name NVARCHAR(200) NOT NULL,
    date_of_birth DATE NULL,
    gender NVARCHAR(10) NULL,
    phone NVARCHAR(30) NULL,
    address NVARCHAR(300) NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT DF_patients_created_at DEFAULT (SYSUTCDATETIME())
);

CREATE TABLE appointments (
    id INT IDENTITY(1,1) PRIMARY KEY,
    patient_id INT NOT NULL,
    appointment_time DATETIME2 NOT NULL,
    doctor_name NVARCHAR(120) NULL,
    reason NVARCHAR(300) NULL,
    status NVARCHAR(30) NOT NULL CONSTRAINT DF_appointments_status DEFAULT ('scheduled'),
    CONSTRAINT FK_appointments_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

CREATE TABLE medical_records (
    id INT IDENTITY(1,1) PRIMARY KEY,
    patient_id INT NOT NULL,
    visit_date DATE NOT NULL,
    diagnosis NVARCHAR(500) NULL,
    prescription NVARCHAR(500) NULL,
    notes NVARCHAR(MAX) NULL,
    CONSTRAINT FK_records_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

CREATE INDEX IX_patients_name ON patients(full_name);
CREATE INDEX IX_patients_phone ON patients(phone);
CREATE INDEX IX_appt_time ON appointments(appointment_time);
CREATE INDEX IX_records_visit_date ON medical_records(visit_date);

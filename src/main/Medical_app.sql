
DROP DATABASE IF EXISTS medical_app;
CREATE DATABASE IF NOT EXISTS medical_app CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE medical_app;

-- =========================
-- 1. ROLES
-- =========================
CREATE TABLE roles (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) UNIQUE NOT NULL
);

-- =========================
-- 2. USERS
-- =========================
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone_number VARCHAR(20),
    role_id INT,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

-- =========================
-- 3. DOCTORS
-- =========================
CREATE TABLE doctors (
    doctor_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE,
    specialization VARCHAR(100),
    experience_years INT,
    license_number VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- =========================
-- 4. PATIENTS
-- =========================
CREATE TABLE patients (
    patient_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE,
    date_of_birth DATE,
    gender VARCHAR(10),
    address VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- =========================
-- 5. PERMISSIONS
-- =========================
CREATE TABLE permissions (
    permission_id INT PRIMARY KEY AUTO_INCREMENT,
    permission_name VARCHAR(100) UNIQUE NOT NULL
);

-- =========================
-- 6. ROLE_PERMISSIONS
-- =========================
CREATE TABLE role_permissions (
    role_id INT,
    permission_id INT,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(role_id),
    FOREIGN KEY (permission_id) REFERENCES permissions(permission_id)
);

-- =========================
-- 7. APPOINTMENTS
-- =========================
CREATE TABLE appointments (
    appointment_id INT PRIMARY KEY AUTO_INCREMENT,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_time DATETIME NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELED') DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);

-- =========================
-- 8. PRESCRIPTIONS
-- =========================
CREATE TABLE prescriptions (
    prescription_id INT PRIMARY KEY AUTO_INCREMENT,
    appointment_id INT,
    doctor_id INT,
    patient_id INT,
    prescription_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id),
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
);

-- =========================
-- 9. PRESCRIPTION_ITEMS
-- =========================
CREATE TABLE prescription_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    prescription_id INT,
    medicine_name VARCHAR(100),
    dosage VARCHAR(50),
    duration VARCHAR(50),
    FOREIGN KEY (prescription_id) REFERENCES prescriptions(prescription_id)
);

-- =========================
-- 10. ACTIVITY_LOGS
-- =========================
CREATE TABLE activity_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    activity_type VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- =========================
-- 11. SYSTEM_STATS
-- =========================
CREATE TABLE system_stats (
    stat_id INT PRIMARY KEY AUTO_INCREMENT,
    total_patients INT DEFAULT 0,
    total_doctors INT DEFAULT 0,
    total_appointments INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- =========================
-- 12. USER_SETTINGS
-- =========================
CREATE TABLE user_settings (
    user_id INT PRIMARY KEY,
    theme VARCHAR(20) DEFAULT 'light',
    language VARCHAR(20) DEFAULT 'vi',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);


-- Chat

CREATE TABLE conversations (
    conversation_id INT PRIMARY KEY AUTO_INCREMENT,
    user1_id INT NOT NULL,
    user2_id INT NOT NULL,
    last_message TEXT,
    last_message_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user1_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_conversation_pair (user1_id, user2_id)
);


CREATE TABLE messages (
    message_id INT PRIMARY KEY AUTO_INCREMENT,
    conversation_id INT NOT NULL,
    sender_id INT NOT NULL,
    content TEXT NOT NULL,
    sent_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (conversation_id) REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES users(user_id) ON DELETE CASCADE
);


-- activity_logsactivity_logsactivity_logsappointments
-- =========================
-- 13. DỮ LIỆU MẪU CƠ BẢN
-- =========================

INSERT INTO roles (role_id, role_name) VALUES
(1, 'Patient'),
(2, 'Doctor'),
(3, 'Admin');

INSERT INTO permissions (permission_id, permission_name) VALUES
(1, 'UPDATE_PASSWORD'),
(2, 'VIEW_PERSONAL_PROFILE'),
(3, 'BOOK_APPOINTMENT'),
(4, 'VIEW_APPOINTMENT'),
(5, 'VIEW_PATIENT_LIST'),
(6, 'MANAGE_ACCOUNTS'),
(7, 'MANAGE_PERMISSIONS'),
(8, 'VIEW_SYSTEM_STATS'),
(9, 'VIEW_DOCTOR_LIST');

INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3),
(2, 1), (2, 2), (2, 4), (2, 5),
(3, 1), (3, 2), (3, 3), (3, 4), (3, 5), (3, 6), (3, 7), (3, 8), (3, 9);

INSERT INTO users (username, password_hash, full_name, email, role_id) VALUES
('admin', '123', 'Admin User', 'admin@mail.com', 3),
('doctor1', '123', 'BS. John', 'john@mail.com', 2),
('patient1', '123', 'Nguyễn Huy', 'huy.nguyen@mail.com', 1);

INSERT INTO doctors (user_id, specialization, experience_years, license_number)
SELECT user_id, 'Cardiology', 10, 'DOC12345'
FROM users WHERE username = 'doctor1';

INSERT INTO patients (user_id, date_of_birth, gender, address)
SELECT user_id, '1995-05-15', 'Male', '123 Trần Phú, Hà Đông, Hà Nội'
FROM users WHERE username = 'patient1';

-- =========================
-- 14. DỮ LIỆU MỞ RỘNG
-- =========================

-- Thêm Users
INSERT INTO users (username, password_hash, full_name, email, phone_number, role_id) VALUES
('doctor2',  '123', 'BS. Nguyễn Anh', 'anh.nguyen@vinmec.com', '0987654321', 2),
('doctor3',  '123', 'BS. Trần Thu',   'thu.tran@benhvien.com', '0912345678', 2),
('patient2', '123', 'Phạm Minh',      'minh.pham@example.com', '0971112233', 1),
('patient3', '123', 'Nguyễn Lan',     'lan.nguyen@example.com','0962223344', 1),
('patient4', '123', 'Đỗ Tuấn',        'tuan.do@example.com',   '0933334455', 1),
('patient5', '123', 'Lê Hoa',         'hoa.le@example.com',    '0924445566', 1);

-- Bác sĩ
INSERT INTO doctors (user_id, specialization, experience_years, license_number)
SELECT user_id, 'Pediatrics', 7, 'HNO-PED-67890' FROM users WHERE username = 'doctor2';
INSERT INTO doctors (user_id, specialization, experience_years, license_number)
SELECT user_id, 'ENT', 12, 'HNO-ENT-34567' FROM users WHERE username = 'doctor3';

-- Bệnh nhân
INSERT INTO patients (user_id, date_of_birth, gender, address)
SELECT user_id, '1998-03-12', 'Male', '12 Trần Duy Hưng, Cầu Giấy, Hà Nội' FROM users WHERE username = 'patient2';
INSERT INTO patients (user_id, date_of_birth, gender, address)
SELECT user_id, '2000-10-02', 'Female', '192 Lê Trọng Tấn, Thanh Xuân, Hà Nội' FROM users WHERE username = 'patient3';
INSERT INTO patients (user_id, date_of_birth, gender, address)
SELECT user_id, '1996-07-25', 'Male', '5 Phố Huế, Hai Bà Trưng, Hà Nội' FROM users WHERE username = 'patient4';
INSERT INTO patients (user_id, date_of_birth, gender, address)
SELECT user_id, '1999-01-30', 'Female', '6 Phúc Lợi, Long Biên, Hà Nội' FROM users WHERE username = 'patient5';

-- Lịch hẹn
INSERT INTO appointments (patient_id, doctor_id, appointment_time, status, notes)
SELECT p.patient_id, d.doctor_id, '2025-11-05 09:00:00', 'CONFIRMED', 'Khám tổng quát - Cầu Giấy'
FROM patients p JOIN users u ON p.user_id=u.user_id AND u.username='patient2'
JOIN doctors d ON d.user_id=(SELECT user_id FROM users WHERE username='doctor2');

INSERT INTO appointments (patient_id, doctor_id, appointment_time, status, notes)
SELECT p.patient_id, d.doctor_id, '2025-11-06 14:30:00', 'PENDING', 'Đau họng, nghi viêm amidan - Thanh Xuân'
FROM patients p JOIN users u ON p.user_id=u.user_id AND u.username='patient3'
JOIN doctors d ON d.user_id=(SELECT user_id FROM users WHERE username='doctor3');

INSERT INTO appointments (patient_id, doctor_id, appointment_time, status, notes)
SELECT p.patient_id, d.doctor_id, '2025-11-02 10:15:00', 'COMPLETED', 'Khám xong - Hai Bà Trưng'
FROM patients p JOIN users u ON p.user_id=u.user_id AND u.username='patient4'
JOIN doctors d ON d.user_id=(SELECT user_id FROM users WHERE username='doctor1');

INSERT INTO appointments (patient_id, doctor_id, appointment_time, status, notes)
SELECT p.patient_id, d.doctor_id, '2025-11-07 16:00:00', 'CANCELED', 'Hủy do bận công tác - Long Biên'
FROM patients p JOIN users u ON p.user_id=u.user_id AND u.username='patient5'
JOIN doctors d ON d.user_id=(SELECT user_id FROM users WHERE username='doctor2');

-- Đơn thuốc
INSERT INTO prescriptions (appointment_id, doctor_id, patient_id, notes)
SELECT appointment_id, doctor_id, patient_id, 'Đơn giảm đau + hạ sốt'
FROM appointments WHERE appointment_time='2025-11-02 10:15:00';

INSERT INTO prescription_items (prescription_id, medicine_name, dosage, duration)
SELECT p.prescription_id, 'Paracetamol', '500mg', '3 ngày'
FROM prescriptions p JOIN appointments a ON p.appointment_id=a.appointment_id
WHERE a.appointment_time='2025-11-02 10:15:00';

-- Tạo cuộc hội thoại giữa bác sĩ và bệnh nhân
INSERT INTO conversations (user1_id, user2_id, last_message, last_message_time)
VALUES
((SELECT user_id FROM users WHERE username='doctor1'),
 (SELECT user_id FROM users WHERE username='patient2'),
 'Xin chào bác sĩ', NOW());

-- Thêm tin nhắn mẫu
INSERT INTO messages (conversation_id, sender_id, content)
VALUES
(1, (SELECT user_id FROM users WHERE username='patient2'), 'Xin chào bác sĩ'),
(1, (SELECT user_id FROM users WHERE username='doctor1'), 'Chào bạn, bạn đang gặp vấn đề gì?');


-- Logs
INSERT INTO activity_logs (user_id, activity_type, description)
SELECT user_id, 'LOGIN', 'Đăng nhập thành công tại Hà Nội'
FROM users WHERE username IN ('admin','doctor1','patient2');

-- Settings
INSERT INTO user_settings (user_id, theme, language, notifications_enabled)
SELECT user_id, 'light', 'vi', TRUE FROM users;

-- Stats
INSERT INTO system_stats (total_patients, total_doctors, total_appointments)
SELECT (SELECT COUNT(*) FROM patients),
       (SELECT COUNT(*) FROM doctors),
       (SELECT COUNT(*) FROM appointments);

-- ===============================================
-- KIỂM TRA
-- ===============================================
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_appointments FROM appointments;
SELECT * FROM appointments ORDER BY appointment_time LIMIT 5;

CREATE TABLE patient_doctor (
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    assigned_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (patient_id, doctor_id),
    CONSTRAINT fk_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE
);

use medical_app;
INSERT INTO patient_doctor (doctor_id, patient_id) VALUES (1, 3);

use medical_app;
ALTER TABLE messages ADD COLUMN appointment_id INT NULL;

use medical_app;
ALTER TABLE messages
ADD COLUMN appointment_status VARCHAR(20) DEFAULT NULL;

use medical_app;
ALTER TABLE appointments
MODIFY appointment_time TIME NOT NULL;













package org.example.mst_medical_app.service;

import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.model.UserModel;
import org.example.mst_medical_app.model.UserRepository;
import org.mindrot.jbcrypt.BCrypt;


public class UserService {

    private final UserRepository userRepository;
    public UserService() {
        this.userRepository = new UserRepository();
    }

    //======================================================================
    // KIỂM TRA MẬT KHẨU (AN TOÀN)
    //======================================================================

    private boolean checkPassword(String plainTextPassword, String hashedPassword) {
        if (plainTextPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
//        if (plainTextPassword.equals(hashedPassword)) {
//            return true;
//        }
//
//        try {
//            return BCrypt.checkpw(plainTextPassword, hashedPassword);
//        } catch (Exception e) {
//            return false;
//        }
        return plainTextPassword.equals(hashedPassword);
    }


    /**
     *CHỨC NĂNG ĐĂNG NHẬP / ĐĂNG XUẤT
    **/


    // Xử lý logic đăng nhập
    public UserModel login(String usernameOrEmail, String password) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return null;
        }

        UserRepository.LoginData loginData = userRepository.getUserDataForLogin(usernameOrEmail);

        if (loginData == null) {
            loginData = userRepository.getUserDataByEmail(usernameOrEmail);
        }

        if (loginData == null) {
            return null;
        }
        if (checkPassword(password, loginData.getHashedPassword())) {
            UserModel user = loginData.getUserModel();
            AuthManager.login(user);
            return user;
        }

        return null;
    }

    public void logout() {
        AuthManager.logOut();
    }

    // CHỨC NĂNG ĐĂNG KÝ

    public String register(String username, String fullName, String email, String password, String confirm, String role) {

        if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty() || role == null) {
            return "Vui lòng điền đầy đủ thông tin.";
        }
        if (!password.equals(confirm)) {
            return "Mật khẩu xác nhận không khớp!";
        }

        if (checkUsernameExists(username) || checkEmailExists(email)) {
            return "Tên đăng nhập hoặc Email đã được sử dụng.";
        }

        int roleId = switch (role.toUpperCase()) {
            case "DOCTOR" -> 2;
            case "PATIENT" -> 1;
            default -> 0;
        };
        if (roleId == 0) {
            return "Vai trò không hợp lệ.";
        }

        boolean success = userRepository.createUser(username, password, fullName, email, roleId);
        if (success) {
            return null; // Thành công
        } else {
            return "Tạo tài khoản thất bại! (Lỗi CSDL)";
        }
    }

    //======================================================================
    // CHỨC NĂNG HỒ SƠ & BẢO MẬT
    //======================================================================

    public String updateProfile(String newFullName, String newEmail, String newPhone) {
        UserModel currentUser = AuthManager.getCurUser();
        if (currentUser == null) {
            return "Người dùng chưa đăng nhập.";
        }

        if (newFullName == null || newFullName.trim().isEmpty() ||
                newEmail == null || newEmail.trim().isEmpty()) {
            return "Họ tên và Email không được để trống.";
        }

        if (!newEmail.trim().equals(currentUser.getEmail())) {
            if (checkEmailExists(newEmail.trim())) {
                return "Email mới này đã được người khác sử dụng.";
            }
        }

        currentUser.setFullName(newFullName.trim());
        currentUser.setEmail(newEmail.trim());
        currentUser.setPhone(newPhone.trim());

        boolean success = userRepository.updateProfile(currentUser);

        if (success) {
            return null;
        } else {
            return "Cập nhật hồ sơ thất bại! (Lỗi CSDL)";
        }
    }

    public String changePassword(String oldPassword, String newPassword, String confirmNewPassword) {
        UserModel currentUser = AuthManager.getCurUser();
        if (currentUser == null) {
            return "Người dùng chưa đăng nhập.";
        }

        if (newPassword.isEmpty() || !newPassword.equals(confirmNewPassword)) {
            return "Mật khẩu mới không khớp hoặc bị trống.";
        }
        if (oldPassword.equals(newPassword)) {
            return "Mật khẩu mới phải khác mật khẩu cũ.";
        }

        UserRepository.LoginData loginData = userRepository.getUserDataForLogin(currentUser.getUsername());

        if (loginData == null || !checkPassword(oldPassword, loginData.getHashedPassword())) {
            return "Mật khẩu cũ không chính xác.";
        }

        boolean success = userRepository.updatePassword(currentUser.getId(), newPassword);

        if (success) {
            return null;
        } else {
            return "Đổi mật khẩu thất bại! (Lỗi CSDL)";
        }
    }

    //======================================================================
    // CÁC HÀM TIỆN ÍCH
    //======================================================================

    public boolean checkUsernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return userRepository.findByUsername(username.trim());
    }

    public boolean checkEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userRepository.findByEmail(email.trim());
    }

}
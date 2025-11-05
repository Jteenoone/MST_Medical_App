package org.example.mst_medical_app.core.utils;

import org.example.mst_medical_app.model.UserModel;

public class UserSession {


    private static UserModel currentUser;


    public static void setUser(UserModel user) {
        currentUser = user;
    }


    public static UserModel getUser() {
        return currentUser;
    }


    public static int getCurrentUserId() {
        return (currentUser != null) ? currentUser.getId() : 0;
    }

    public static String getRole() {
        return (currentUser != null) ? currentUser.getRole() : null;
    }

    public static String getUsername() {
        return (currentUser != null) ? currentUser.getUsername() : null;
    }

    public static boolean isAdmin() {
        return "Admin".equalsIgnoreCase(getRole());
    }

    public static boolean isDoctor() {
        return "Doctor".equalsIgnoreCase(getRole());
    }

    public static boolean isPatient() {
        return "Patient".equalsIgnoreCase(getRole());
    }
    public static void clear() {
        currentUser = null;
    }
}

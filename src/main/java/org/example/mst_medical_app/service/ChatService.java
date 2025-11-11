package org.example.mst_medical_app.service;

import org.example.mst_medical_app.core.database.DatabaseConnection;
import org.example.mst_medical_app.features.chat.ChatDAO;


import java.sql.*;

public class ChatService {
    public int createOrGetConversation(int userA, int userB) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            ChatDAO dao = new ChatDAO(conn);
            return dao.createConversationIfNotExist(userA, userB);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

}

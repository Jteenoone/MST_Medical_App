package org.example.mst_medical_app.features.chat;

import org.example.mst_medical_app.model.Doctor;

public class ChatOpenData {

    private int conversationId;
    private Doctor doctor;

    public ChatOpenData(int conversationId, Doctor doctor) {
        this.conversationId = conversationId;
        this.doctor = doctor;
    }

    public int getConversationId() {
        return conversationId;
    }

    public Doctor getDoctor() {
        return doctor;
    }
}

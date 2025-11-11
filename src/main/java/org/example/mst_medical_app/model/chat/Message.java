package org.example.mst_medical_app.model.chat;

import java.time.LocalDateTime;

public class Message {

    private int id;
    private int conversationId;
    private int senderId;
    private String content;
    private LocalDateTime sentTime;
    private String appointmentStatus;
    private Integer appointmentId;

    public Message() {}

    public Message(int conversationId, int senderId, String content, LocalDateTime sentTime) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.sentTime = sentTime;
    }

    // Constructor có appointmentStatus
    public Message(int conversationId, int senderId, String content, LocalDateTime sentTime, String appointmentStatus) {
        this(conversationId, senderId, content, sentTime);
        this.appointmentStatus = appointmentStatus;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentTime() { return sentTime; }
    public void setSentTime(LocalDateTime sentTime) { this.sentTime = sentTime; }

    public String getAppointmentStatus() { return appointmentStatus; }
    public void setAppointmentStatus(String appointmentStatus) { this.appointmentStatus = appointmentStatus; }

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }
}

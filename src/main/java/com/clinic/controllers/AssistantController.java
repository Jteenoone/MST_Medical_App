package com.clinic.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AssistantController {
    @FXML private TextArea chatArea;
    @FXML private TextField userInput;
    @FXML private Button sendBtn;

    @FXML
    private void initialize() {
        sendBtn.setOnAction(e -> handleSend());
        userInput.setOnAction(e -> handleSend());
    }

    private void handleSend() {
        String q = userInput.getText();
        if (q == null || q.isBlank()) return;
        append("Bạn: " + q);
        String a = basicAnswer(q);
        append("AI: " + a);
        userInput.clear();
    }

    private void append(String line) {
        chatArea.appendText(line + "\n");
    }

    private String basicAnswer(String q) {
        String lower = q.toLowerCase();
        if (lower.contains("đau") || lower.contains("pain")) {
            return "Nếu đau nhiều hoặc kéo dài, hãy đặt lịch khám sớm và nghỉ ngơi.";
        }
        if (lower.contains("sốt") || lower.contains("fever")) {
            return "Theo dõi nhiệt độ, uống đủ nước, dùng hạ sốt theo chỉ dẫn bác sĩ.";
        }
        if (lower.contains("ho") || lower.contains("cough")) {
            return "Tránh khói bụi, uống ấm; nếu ho kéo dài > 2 tuần hãy đi khám.";
        }
        return "Tôi là trợ lý cơ bản, vui lòng mô tả triệu chứng rõ hơn hoặc liên hệ bác sĩ.";
    }
}

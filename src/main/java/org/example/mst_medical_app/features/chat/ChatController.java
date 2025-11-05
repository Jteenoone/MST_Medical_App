package org.example.mst_medical_app.features.chat;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.example.mst_medical_app.core.database.DatabaseConnection;
import org.example.mst_medical_app.core.utils.UserSession;
import org.example.mst_medical_app.model.chat.Conversation;
import org.example.mst_medical_app.model.chat.Message;

public class ChatController {

    private ChatDAO chatDAO;
    private int currentUserId;           // id người đang đăng nhập
    private int currentConversationId;   // id cuộc trò chuyện hiện tại

    // LEFT SIDE
    @FXML private TextField searchField;
    @FXML private VBox conversationList;

    // RIGHT SIDE
    @FXML private VBox chatWindow;
    @FXML private ImageView chatAvatar;
    @FXML private Label chatName;
    @FXML private Label chatStatus;

    @FXML private ScrollPane messageScrollPane;
    @FXML private VBox messageContainer;

    @FXML private HBox typingIndicator;
    @FXML private Label typingDots;

    @FXML private TextField messageField;
    @FXML private Button sendBtn;

    private String currentChatUser = null;

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            chatDAO = new ChatDAO(conn);

            // Lấy user ID từ session
            currentUserId = UserSession.getCurrentUserId();
            if (currentUserId == 0) {
                System.out.println("Không có user đăng nhập — tạm set userId = 1 để test.");
                currentUserId = 1; // fallback cho debug
            }

            loadConversationsFromDB();
            setupSendMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void loadConversationsFromDB() {
        conversationList.getChildren().clear();
        try {
            List<Conversation> list = chatDAO.getConversationsByUser(currentUserId);

            for (Conversation c : list) {
                String otherName = c.getOtherUserName();
                addConversationItem(
                        c.getId(),
                        otherName,
                        "/images/doctor1.png",
                        c.getLastMessage(),
                        c.getLastMessageTime() != null
                                ? c.getLastMessageTime().toString()
                                : ""
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void addConversationItem(int conversationId, String name, String avatarPath, String lastMsg, String time) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ChatConversationItem.fxml"));
            HBox item = loader.load();

            ChatConversationItemController controller = loader.getController();
            controller.setData(name, avatarPath, lastMsg, time);

            item.setOnMouseClicked(e -> openChat(conversationId, name, avatarPath));

            conversationList.getChildren().add(item);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void openChat(int conversationId, String user, String avatarPath) {
        currentConversationId = conversationId;
        currentChatUser = user;

        chatName.setText(user);
        chatStatus.setText("Online");

        try {
            chatAvatar.setImage(new Image(getClass().getResourceAsStream(avatarPath)));
        } catch (Exception e) {
            chatAvatar.setImage(new Image(getClass().getResourceAsStream("/images/default_avatar.png")));
        }

        messageContainer.getChildren().clear();

        // Load messages from DB
        try {
            List<Message> messages = chatDAO.getMessages(conversationId);
            for (Message m : messages) {
                boolean isSender = (m.getSenderId() == currentUserId);
                addMessageBubble(m.getContent(), isSender);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        scrollToBottom();
    }


    private void setupSendMessage() {
        sendBtn.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage()); // Enter key
    }

    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty() || currentChatUser == null) return;

        // Hiển thị ngay trên UI
        addMessageBubble(msg, true);
        messageField.clear();
        scrollToBottom();

        // Lưu vào DB
        try {
            Message message = new Message();
            message.setConversationId(currentConversationId);
            message.setSenderId(currentUserId);
            message.setContent(msg);

            chatDAO.sendMessage(message);
            System.out.println("Đã lưu tin nhắn vào DB!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addMessageBubble(String message, boolean isSender) {
        Label bubble = new Label(message);
        bubble.setWrapText(true);
        bubble.setMaxWidth(380);

        if (isSender) {
            bubble.setStyle("-fx-background-color: #0D6EFD; -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 14;");
            HBox box = new HBox(bubble);
            box.setAlignment(Pos.CENTER_RIGHT);
            messageContainer.getChildren().add(box);
        } else {
            bubble.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #0D6EFD; -fx-text-fill: black; -fx-padding: 8 12; -fx-background-radius: 14;");
            HBox box = new HBox(bubble);
            box.setAlignment(Pos.CENTER_LEFT);
            messageContainer.getChildren().add(box);
        }

        scrollToBottom();
    }

    private void scrollToBottom() {
        messageScrollPane.layout();
        messageScrollPane.setVvalue(1.0);
    }
}

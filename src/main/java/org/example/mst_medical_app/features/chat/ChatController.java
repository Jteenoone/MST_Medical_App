package org.example.mst_medical_app.features.chat;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.example.mst_medical_app.core.database.DatabaseConnection;
import org.example.mst_medical_app.core.security.AuthManager;
import org.example.mst_medical_app.model.Appointment;
import org.example.mst_medical_app.model.Doctor;
import org.example.mst_medical_app.model.chat.Conversation;
import org.example.mst_medical_app.model.chat.Message;
import org.example.mst_medical_app.service.AppointmentService;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ChatController {

    private ChatDAO chatDAO;
    private int currentUserId;
    private int currentConversationId;
    private int currentChatUserId;

    @FXML private TextField searchField;
    @FXML private VBox conversationList;

    @FXML private VBox chatWindow;
    @FXML private ImageView chatAvatar;
    @FXML private Label chatName;
    @FXML private Label chatStatus;

    @FXML private ScrollPane messageScrollPane;
    @FXML private VBox messageContainer;

    @FXML private TextField messageField;
    @FXML private Button sendBtn;

    private String currentChatUserName;

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            chatDAO = new ChatDAO(conn);


            currentUserId = AuthManager.getCurUser().getId();

            loadConversationsFromDB();

            setupSendMessage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* =====================================================
     *                 LOAD CONVERSATIONS
     * ===================================================== */

    private void loadConversationsFromDB() {
        conversationList.getChildren().clear();
        try {
            List<Conversation> list = chatDAO.getConversationsByUser(currentUserId);

            for (Conversation c : list) {
                String otherName = c.getOtherUserName();
                String avatarPath = c.getOtherUserAvatar() != null
                        ? c.getOtherUserAvatar()
                        : "/images/default_avatar.png";

                addConversationItem(
                        c.getId(),
                        otherName,
                        avatarPath,
                        c.getLastMessage() != null ? c.getLastMessage() : "(Chưa có tin nhắn)",
                        c.getLastMessageTime() != null
                                ? c.getLastMessageTime().format(DateTimeFormatter.ofPattern("HH:mm dd/MM"))
                                : ""
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadMessages(int conversationId) {
        try {
            messageContainer.getChildren().clear();

            List<Message> messages = chatDAO.getMessages(conversationId);

            for (Message m : messages) {
                boolean isSender = (m.getSenderId() == currentUserId);
                boolean showApproveBtn =
                        "PENDING".equals(m.getAppointmentStatus()) &&
                                !isSender &&
                                AuthManager.getCurUser().getRole().equalsIgnoreCase("DOCTOR") && m.getAppointmentId() != null;
                addMessageBubbleWithApprove(m, isSender, showApproveBtn);
            }

            scrollToBottom();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* =====================================================
     *           HIỂN THỊ MỖI CONVERSATION ITEM
     * ===================================================== */

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

    /* =====================================================
     *               MỞ KHUNG CHAT CHI TIẾT
     * ===================================================== */

    private void openChat(int conversationId, String otherName, String avatarPath) {
        try {
            currentChatUserName = otherName;
            currentChatUserId = getUserIdByName(otherName);

            // Đảm bảo conversation tồn tại
            currentConversationId = chatDAO.createConversationIfNotExist(currentUserId, currentChatUserId);

            chatName.setText(otherName);
            chatStatus.setText("🟢 Online");

            try {
                chatAvatar.setImage(new Image(getClass().getResourceAsStream(avatarPath)));
            } catch (Exception e) {
                chatAvatar.setImage(new Image(getClass().getResourceAsStream("/images/default_avatar.png")));
            }

            messageContainer.getChildren().clear();

            // ✅ Load tin nhắn từ DB
            loadMessages(currentConversationId);
            scrollToBottom();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* =====================================================
     *                     GỬI TIN NHẮN
     * ===================================================== */

    private void setupSendMessage() {
        sendBtn.setOnAction(e -> sendMessage());
        messageField.setOnAction(e -> sendMessage()); // Enter key
    }

    public void setChatOpenData(ChatOpenData data) {
        this.currentConversationId = data.getConversationId();
        Doctor doctor = data.getDoctor();

        chatName.setText("Dr. " + doctor.getFullName());
        currentChatUserName = doctor.getFullName();

        loadMessages(currentConversationId);

//        sendAutoAppointmentMessage(doctor);
    }


    private void sendMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty() || currentChatUserName == null) return;

        addMessageBubble(msg, true);
        messageField.clear();
        scrollToBottom();

        try {
            Message message = new Message();
            message.setConversationId(currentConversationId);
            message.setSenderId(currentUserId);
            message.setContent(msg);

            // Lấy appointmentId đính kèm nếu có (ví dụ bạn set vào userData)
            Object udata = messageField.getUserData();
            if (udata instanceof Integer) {
                message.setAppointmentId((Integer) udata);
                // optionally set appointmentStatus if this is the booking message
                message.setAppointmentStatus("PENDING");
                // remove userData so subsequent messages won't include same appointmentId
                messageField.setUserData(null);
            }

            chatDAO.sendMessage(message);
            System.out.println("💬 Tin nhắn đã gửi và cập nhật last_message!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /* =====================================================
     *              HIỂN THỊ BONG BÓNG TIN NHẮN
     * ===================================================== */
    private void addMessageBubble(String message, boolean isSender) {
        Label bubble = new Label(message);
        bubble.setWrapText(true);
        bubble.setMaxWidth(380);

        if (isSender) {
            bubble.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-padding: 10 14; -fx-background-radius: 18 18 4 18;");
            HBox box = new HBox(bubble);
            box.setAlignment(Pos.CENTER_RIGHT);
            messageContainer.getChildren().add(box);
        } else {
            bubble.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: black; -fx-padding: 10 14; -fx-background-radius: 18 18 18 4;");
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

    public void openConversation(int conversationId, Doctor doctor) {
        this.currentConversationId = conversationId;
        this.currentChatUserName = doctor.getFullName();
        chatName.setText("Dr. " + doctor.getFullName());

        // load tin nhắn từ DB
        loadMessages(conversationId);

        // gửi message auto
//        sendAutoAppointmentMessage(doctor);
    }

    private void sendAutoAppointmentMessage(Doctor doctor) {
        String msg = "📅 Bệnh nhân muốn đặt lịch hẹn với bác sĩ\n"
                + "Tên bác sĩ: " + doctor.getFullName() + "\n"
                + "Chờ bác sĩ xác nhận ✅";

        Message message = new Message();
        message.setConversationId(currentConversationId);
        message.setSenderId(currentUserId);
        message.setContent(msg);
        message.setAppointmentStatus("PENDING");

        try {
            chatDAO.sendMessage(message);
            loadMessages(currentConversationId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addMessageBubbleWithApprove(Message m, boolean isSender, boolean showApproveBtn) {
        VBox wrapper = new VBox(5);
        wrapper.setAlignment(isSender ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label bubble = new Label(m.getContent());
        bubble.setWrapText(true);
        bubble.setMaxWidth(380);

        bubble.setStyle(isSender
                ? "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-padding: 10 14; -fx-background-radius: 18 18 4 18;"
                : "-fx-background-color: #F1F5F9; -fx-text-fill: black; -fx-padding: 10 14; -fx-background-radius: 18 18 18 4;"
        );

        wrapper.getChildren().add(bubble);

        // ✅ Nếu là message pending và bác sĩ đang xem thì hiện nút
        if (!isSender && showApproveBtn && m.getAppointmentId() != null) {

            HBox btnArea = new HBox(10);
            btnArea.setAlignment(Pos.CENTER_LEFT);

            Button approveBtn = new Button("✅ Xác nhận");
            Button denyBtn = new Button("❌ Từ chối");

            approveBtn.setStyle("""
                -fx-background-color: #16A34A;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-padding: 6 12;
                -fx-background-radius: 10;
        """);

            denyBtn.setStyle("""
                -fx-background-color: #DC2626;
                -fx-text-fill: white;
                -fx-font-weight: bold;
                -fx-padding: 6 12;
                -fx-background-radius: 10;
        """);

            approveBtn.setOnAction(e -> approveAppointment(m.getAppointmentId()));
            denyBtn.setOnAction(e -> denyAppointment(m.getAppointmentId()));

            btnArea.getChildren().addAll(approveBtn, denyBtn);

            wrapper.getChildren().add(btnArea);
        }

        messageContainer.getChildren().add(wrapper);
        scrollToBottom();
    }


    // Đồng ý
    private void approveAppointment(Integer appointmentId) {
        if (appointmentId == null) {
            System.out.println("Không có appointmentId để xác nhận.");
            return;
        }

        AppointmentService appointmentService = new AppointmentService();
        boolean success = appointmentService.updateAppointmentStatus(
                appointmentId,
                Appointment.Status.CONFIRMED
        );

        if (!success) {
            System.out.println("Lỗi cập nhật trạng thái appointment!");
            // nếu muốn, re-enable nút ở caller bằng cách giữ ref tới button; đơn giản là thông báo.
            return;
        }

        // gửi tin nhắn phản hồi vào chat
        Message replyMsg = new Message();
        replyMsg.setConversationId(currentConversationId);
        replyMsg.setSenderId(currentUserId);
        replyMsg.setContent("✅ Lịch hẹn #" + appointmentId + " đã được bác sĩ xác nhận.");
        replyMsg.setAppointmentStatus("CONFIRMED");
        replyMsg.setAppointmentId(appointmentId);

        try {
            chatDAO.sendMessage(replyMsg);
            // reload messages để cập nhật view (nút xác nhận sẽ biến mất khi status != PENDING)
            loadMessages(currentConversationId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Từ chối
    private void denyAppointment(Integer appointmentId) {
        AppointmentService appointmentService = new AppointmentService();

        boolean success = appointmentService.updateAppointmentStatus(
                appointmentId,
                Appointment.Status.CANCELED
        );

        if (!success) {
            System.out.println("Lỗi khi hủy appointment");
            return;
        }

        Message replyMsg = new Message();
        replyMsg.setConversationId(currentConversationId);
        replyMsg.setSenderId(currentUserId);
        replyMsg.setContent("❌ Lịch hẹn #" + appointmentId + " đã bị từ chối.");
        replyMsg.setAppointmentStatus("CANCELED");
        replyMsg.setAppointmentId(appointmentId);

        try {
            chatDAO.sendMessage(replyMsg);
            loadMessages(currentConversationId); // reload để mất nút
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /* =====================================================
     *               TÌM USER_ID THEO TÊN
     * ===================================================== */
    private int getUserIdByName(String fullName) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM users WHERE full_name = ?")) {
            ps.setString(1, fullName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}

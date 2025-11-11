package org.example.mst_medical_app.features.chat;

import org.example.mst_medical_app.model.chat.Conversation;
import org.example.mst_medical_app.model.chat.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {

    private final Connection conn;

    public ChatDAO(Connection conn) {
        this.conn = conn;
    }

    /** ========================
     *  LẤY DANH SÁCH CUỘC HỘI THOẠI
     * ======================== */
    public List<Conversation> getConversationsByUser(int userId) throws SQLException {
        String sql = """
            SELECT c.*, 
                   CASE WHEN c.user1_id = ? THEN u2.full_name ELSE u1.full_name END AS other_name
            FROM conversations c
            JOIN users u1 ON u1.user_id = c.user1_id
            JOIN users u2 ON u2.user_id = c.user2_id
            WHERE c.user1_id = ? OR c.user2_id = ?
            ORDER BY c.last_message_time DESC
        """;

        List<Conversation> list = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Conversation c = new Conversation();
                    c.setId(rs.getInt("conversation_id"));
                    c.setUser1Id(rs.getInt("user1_id"));
                    c.setUser2Id(rs.getInt("user2_id"));
                    c.setLastMessage(rs.getString("last_message"));

                    Timestamp ts = rs.getTimestamp("last_message_time");
                    if (ts != null)
                        c.setLastMessageTime(ts.toLocalDateTime());
                    else
                        c.setLastMessageTime(LocalDateTime.now()); // fallback tránh null

                    c.setOtherUserName(rs.getString("other_name"));
                    list.add(c);
                }
            }
        }

        return list;
    }

    /** ========================
     *  LẤY TOÀN BỘ TIN NHẮN
     * ======================== */
    public List<Message> getMessages(int conversationId) throws SQLException {
        String sql = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY sent_time ASC";
        List<Message> messages = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, conversationId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setId(rs.getInt("message_id"));
                    m.setConversationId(rs.getInt("conversation_id"));
                    m.setSenderId(rs.getInt("sender_id"));
                    m.setContent(rs.getString("content"));
                    Timestamp ts = rs.getTimestamp("sent_time");
                    if (ts != null)
                        m.setSentTime(ts.toLocalDateTime());
                    int apId = rs.getInt("appointment_id");
                    if (!rs.wasNull()) {
                        m.setAppointmentId(apId);
                    }

                    String apStatus = rs.getString("appointment_status");
                    if (apStatus != null) {
                        m.setAppointmentStatus(apStatus);
                    }

                    messages.add(m);
                }
            }
        }
        return messages;
    }

    /** ========================
     *  GỬI TIN NHẮN MỚI
     * ======================== */
    public void sendMessage(Message message) throws SQLException {
        String insert = """
        INSERT INTO messages (conversation_id, sender_id, content, appointment_id, appointment_status)
        VALUES (?, ?, ?, ?, ?)
    """;

        String update = """
        UPDATE conversations
        SET last_message = ?, last_message_time = NOW()
        WHERE conversation_id = ?
    """;

        try (PreparedStatement ps = conn.prepareStatement(insert);
             PreparedStatement ps2 = conn.prepareStatement(update)) {

            ps.setInt(1, message.getConversationId());
            ps.setInt(2, message.getSenderId());
            ps.setString(3, message.getContent());

            // ✅ thêm dòng này để lưu appointment vào message
            if (message.getAppointmentId() != null)
                ps.setInt(4, message.getAppointmentId());
            else
                ps.setNull(4, java.sql.Types.INTEGER);

            if (message.getAppointmentStatus() != null)
                ps.setString(5, message.getAppointmentStatus());
            else
                ps.setNull(5, java.sql.Types.VARCHAR);

            ps.executeUpdate();

            // cập nhật last message
            ps2.setString(1, message.getContent());
            ps2.setInt(2, message.getConversationId());
            ps2.executeUpdate();
        }
    }



    public void sendMessageSystem(int conversationId, String content) throws SQLException {
        String sql = "INSERT INTO messages (conversation_id, sender_id, content) VALUES (?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, conversationId);
        ps.setInt(2, 0); // 0 = SYSTEM MESSAGE
        ps.setString(3, content);
        ps.executeUpdate();

        String update = "UPDATE conversations SET last_message = ?, last_message_time = NOW() WHERE conversation_id = ?";
        PreparedStatement ps2 = conn.prepareStatement(update);
        ps2.setString(1, content);
        ps2.setInt(2, conversationId);
        ps2.executeUpdate();
    }


    /** ========================
     *  TẠO CONVERSATION NẾU CHƯA CÓ
     * ======================== */
    public int createConversationIfNotExist(int userA, int userB) throws SQLException {
        // Sắp xếp user để tránh trùng nghịch chiều
        int u1 = Math.min(userA, userB);
        int u2 = Math.max(userA, userB);

        String checkSql = "SELECT conversation_id FROM conversations WHERE user1_id = ? AND user2_id = ?";
        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setInt(1, u1);
            check.setInt(2, u2);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) return rs.getInt("conversation_id");
            }
        }

        // Nếu chưa tồn tại thì tạo mới
        String insert = """
            INSERT INTO conversations (user1_id, user2_id, last_message, last_message_time)
            VALUES (?, ?, '', NOW())
        """;
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, u1);
            ps.setInt(2, u2);
            ps.executeUpdate();

            try (ResultSet generated = ps.getGeneratedKeys()) {
                if (generated.next()) return generated.getInt(1);
            }
        }

        return -1; // lỗi hoặc không tạo được
    }

    /** ========================
     *  LẤY conversation_id GIỮA 2 NGƯỜI (dễ dùng cho AppointmentService)
     * ======================== */
    public int getConversationId(int userA, int userB) throws SQLException {
        String sql = """
            SELECT conversation_id FROM conversations
            WHERE (user1_id = ? AND user2_id = ?) OR (user1_id = ? AND user2_id = ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userA);
            ps.setInt(2, userB);
            ps.setInt(3, userB);
            ps.setInt(4, userA);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("conversation_id");
            }
        }

        return -1; // chưa có conversation
    }
}

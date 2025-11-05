package org.example.mst_medical_app.features.chat;

import org.example.mst_medical_app.model.chat.Conversation;
import org.example.mst_medical_app.model.chat.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {
    private final Connection conn;

    public ChatDAO(Connection conn) {
        this.conn = conn;
    }

    // Lấy danh sách cuộc hội thoại người dùng
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

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, userId);
        ps.setInt(2, userId);
        ps.setInt(3, userId);
        ResultSet rs = ps.executeQuery();

        List<Conversation> list = new ArrayList<>();
        while (rs.next()) {
            Conversation c = new Conversation();
            c.setId(rs.getInt("conversation_id"));
            c.setUser1Id(rs.getInt("user1_id"));
            c.setUser2Id(rs.getInt("user2_id"));
            c.setLastMessage(rs.getString("last_message"));
            c.setLastMessageTime(rs.getTimestamp("last_message_time").toLocalDateTime());
            c.setOtherUserName(rs.getString("other_name"));
            list.add(c);
        }
        return list;
    }

    // Lấy toàn bộ tin nhắn của một cuộc hội thoại
    public List<Message> getMessages(int conversationId) throws SQLException {
        String sql = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY sent_time ASC";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, conversationId);
        ResultSet rs = ps.executeQuery();

        List<Message> messages = new ArrayList<>();
        while (rs.next()) {
            Message m = new Message();
            m.setId(rs.getInt("message_id"));
            m.setConversationId(rs.getInt("conversation_id"));
            m.setSenderId(rs.getInt("sender_id"));
            m.setContent(rs.getString("content"));
            m.setSentTime(rs.getTimestamp("sent_time").toLocalDateTime());
            messages.add(m);
        }
        return messages;
    }

    // Gửi tin nhắn mới
    public void sendMessage(Message message) throws SQLException {
        String insert = "INSERT INTO messages (conversation_id, sender_id, content) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(insert);
        ps.setInt(1, message.getConversationId());
        ps.setInt(2, message.getSenderId());
        ps.setString(3, message.getContent());
        ps.executeUpdate();

        String update = "UPDATE conversations SET last_message = ?, last_message_time = NOW() WHERE conversation_id = ?";
        PreparedStatement ps2 = conn.prepareStatement(update);
        ps2.setString(1, message.getContent());
        ps2.setInt(2, message.getConversationId());
        ps2.executeUpdate();
    }

    // Tạo một cuộc hội thoại mới nếu chưa có
    public int createConversationIfNotExist(int userA, int userB) throws SQLException {
        int u1 = Math.min(userA, userB);
        int u2 = Math.max(userA, userB);

        String checkSql = "SELECT conversation_id FROM conversations WHERE user1_id = ? AND user2_id = ?";
        PreparedStatement check = conn.prepareStatement(checkSql);
        check.setInt(1, u1);
        check.setInt(2, u2);
        ResultSet rs = check.executeQuery();

        if (rs.next()) return rs.getInt("conversation_id");

        String insert = "INSERT INTO conversations (user1_id, user2_id, last_message, last_message_time) VALUES (?, ?, '', NOW())";
        PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, u1);
        ps.setInt(2, u2);
        ps.executeUpdate();

        ResultSet generated = ps.getGeneratedKeys();
        if (generated.next()) return generated.getInt(1);
        return -1;
    }
}

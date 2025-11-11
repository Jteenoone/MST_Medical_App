package org.example.mst_medical_app.core.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import org.example.mst_medical_app.controller.MainLayoutController;
import org.example.mst_medical_app.features.chat.ChatController;
import org.example.mst_medical_app.features.chat.ChatOpenData;
import org.example.mst_medical_app.model.Doctor;

import java.io.IOException;
import java.net.URL;

public class SceneManager {

    private static Stage mainStage;

    public static void setMainStage(Stage stage) {
        mainStage = stage;
        mainStage.setMaximized(true);
    }

    public static void switchScene(String fxmlPath, String title) {
        if (mainStage == null) {
            System.err.println("Lỗi SceneManager: mainStage chưa được thiết lập.");
            return;
        }

        try {
            URL fxmlUrl = SceneManager.class.getResource(fxmlPath);
            if (fxmlUrl == null)
                throw new IOException("Không thể tìm thấy file FXML: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load());

            mainStage.setScene(scene);
            mainStage.setTitle(title);
            setFullScreenBounds();
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setScene(Parent root) {
        if (mainStage == null) {
            System.err.println("SceneManager: mainStage chưa được set.");
            return;
        }

        Scene scene = new Scene(root);
        mainStage.setScene(scene);
        setFullScreenBounds();
        mainStage.show();
    }

    public static void openChat(int conversationId, Doctor doctor) throws IOException {

        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource("/org/example/mst_medical_app/features/chat/ChatView.fxml"));
        Parent view = loader.load();

        ChatController controller = loader.getController();
        controller.openConversation(conversationId, doctor);

        // ✅ Lấy MainLayoutController và thay phần content bằng ChatView
        MainLayoutController.getInstance().setContent(view);
    }





    private static void setFullScreenBounds() {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        mainStage.setX(bounds.getMinX());
        mainStage.setY(bounds.getMinY());
        mainStage.setWidth(bounds.getWidth());
        mainStage.setHeight(bounds.getHeight());
    }
}

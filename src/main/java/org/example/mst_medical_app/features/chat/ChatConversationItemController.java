package org.example.mst_medical_app.features.chat;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class ChatConversationItemController {

    @FXML private HBox root;
    @FXML private ImageView avatarImg;
    @FXML private Label nameLbl;
    @FXML private Label lastMsgLbl;
    @FXML private Label timeLbl;

    public void setData(String name, String avatarPath, String lastMsg, String time) {
        nameLbl.setText(name);
        lastMsgLbl.setText(lastMsg);
        timeLbl.setText(time);

        try {
            avatarImg.setImage(new Image(getClass().getResourceAsStream(avatarPath)));
        } catch (Exception e) {
            System.out.println("Avatar not found -> using default");
            avatarImg.setImage(new Image(getClass().getResourceAsStream("/images/default_avatar.png")));
        }
    }

    public void setSelected(boolean selected) {
        root.setStyle(selected
                ? "-fx-background-color: #E3EEFF;"
                : "-fx-background-color: transparent;");
    }
}

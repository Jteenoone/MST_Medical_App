package com.clinic.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;

public class MainController {
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem aboutMenuItem;
    @FXML private TabPane mainTabPane;

    @FXML
    private void initialize() {
        if (exitMenuItem != null) {
            exitMenuItem.setOnAction(e -> System.exit(0));
        }
        if (aboutMenuItem != null) {
            aboutMenuItem.setOnAction(e -> {
                // Placeholder: show about dialog later
            });
        }
    }
}

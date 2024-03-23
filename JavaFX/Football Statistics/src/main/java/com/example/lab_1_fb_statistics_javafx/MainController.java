package com.example.lab_1_fb_statistics_javafx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private Label welcomeText;

    @FXML
    private TableView<String> tableView;

    @FXML
    private TableColumn<String, String> itemColumn;

    private ObservableList<String> data = FXCollections.observableArrayList(
            "Item 1", "Item 2", "Item 3", "Item 4", "Item 5"
    );

    @FXML
    private Button helloButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        welcomeText.setText("Welcome to JavaFX TableView Example!");

        tableView.setItems(data);
        itemColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()));
    }

    @FXML
    private void onHelloButtonClick() {
        welcomeText.setText("Hello Button Clicked!");
    }
}

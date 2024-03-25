package com.example.lab_1_fb_statistics_javafx;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.*;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public class Main extends Application {
    private ComboBox<Object> teamComboBox;
    private ComboBox<Object> startDateComboBox;
    private ComboBox<Object> endDateComboBox;
    private Button searchButton;
    private Button deleteButton;
    private Button addButton;
    private TableView<String[]> statsTable;
    private ToggleButton editToggle;
    private boolean editMode = false;

    @Override

    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));
        stage.setTitle("Football Statistics");

        BorderPane root = new BorderPane();

        VBox comboBoxPanel = new VBox();
        comboBoxPanel.setSpacing(10);

        teamComboBox = new ComboBox<Object>();
        comboBoxPanel.getChildren().addAll(new Label("Select Team:"), teamComboBox);

        startDateComboBox = new ComboBox<Object>();
        endDateComboBox = new ComboBox<Object>();
        comboBoxPanel.getChildren().addAll(new Label("Select Start Date:"), startDateComboBox, new Label("Select End Date:"), endDateComboBox);

        root.setTop(comboBoxPanel);

        HBox buttonPanel = new HBox();
        buttonPanel.setSpacing(10);

        searchButton = new Button("Search");
        editToggle = new ToggleButton("Edit Mode");
        deleteButton = new Button("Delete");
        addButton = new Button("Add");

        buttonPanel.getChildren().addAll(searchButton, editToggle, deleteButton, addButton);

        root.setCenter(buttonPanel);

        statsTable = new TableView<>();
        root.setBottom(statsTable);

        Scene scene = new Scene(root, 1000, 600);
        statsTable.setEditable(false);


        loadDataFromDatabase();

        // Set action listeners
        searchButton.setOnAction(event -> {

            // Код, который нужно выполнить при нажатии на кнопку
            System.out.println("Кнопка была нажата");
            searchMatches();
        });
        deleteButton.setOnAction(event -> deleteSelectedRow());
        addButton.setOnAction(event -> showAddMatchForm());
        editToggle.setOnAction(event -> updateEditMode());
        stage.setScene(scene);
        stage.show();
    }
    private void showAddMatchForm() {
        TextField homeTeamField = new TextField();
        TextField awayTeamField = new TextField();
        TextField dateField = new TextField();
        TextField resultField = new TextField();
        TextField homeGoalsField = new TextField();
        TextField awayGoalsField = new TextField();
        TextField stadiumField = new TextField();
        TextField refereeField = new TextField();

        Label homeTeamLabel= new Label("Home team:");
        Label awayTeamLabel=new Label("Away team:");
        Label dateLabel=new Label("Date:");
        Label resultLabel=new Label("Result:");
        Label homeGoalsLabel=new Label("Home team goals:");
        Label awayGoalsLabel=new Label("Away team goals:");
        Label stadiumLabel=new Label("Stadium:");
        Label refereeLabel= new Label("Referee:");



        // Create VBox to hold Labels and TextFields
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(homeTeamLabel,homeTeamField,awayTeamLabel,awayTeamField,
                dateLabel,dateField,resultLabel,resultField,homeGoalsLabel,homeGoalsField,
                awayGoalsLabel,awayGoalsField,stadiumLabel,stadiumField,refereeLabel,refereeField);
        vbox.setPadding(new Insets(10));

        // Create Button for confirmation
        Button addButton = new Button("Add");

        // Create VBox to hold the form and Button
        VBox formWithButton = new VBox(10);
        formWithButton.getChildren().addAll(vbox, addButton);
        formWithButton.setAlignment(Pos.CENTER);

        // Create Stage to display the form
        Stage stage = new Stage();
        stage.setTitle("Add New Match");
        stage.initModality(Modality.APPLICATION_MODAL);

        // Set action for the Add button
        addButton.setOnAction(event -> {
            // Retrieve input values
            String homeTeam = homeTeamField.getText();
            String awayTeam = awayTeamField.getText();
            String date = dateField.getText();
            String result = resultField.getText();
            String homeGoals = homeGoalsField.getText();
            String awayGoals = awayGoalsField.getText();
            String stadium = stadiumField.getText();
            String referee = refereeField.getText();

            // Call method to insert team into database
            try {
                DAO.insertMatch(homeTeam, awayTeam, date, result, homeGoals, awayGoals, stadium, referee);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            addDateInComboBox(date);
            // Close the stage after adding the team
            stage.close();
        });

        // Create Scene and set it to the Stage
        Scene scene = new Scene(formWithButton);
        stage.setScene(scene);

        // Show the Stage
        stage.show();
    }

    protected static void showAddTeamForm() {
        // Create TextFields for input
        TextField teamNameField = new TextField();
        TextField cityField = new TextField();
        TextField coachField = new TextField();
        TextField captainField = new TextField();

        // Create Labels for TextFields
        Label teamNameLabel = new Label("Team Name:");
        Label cityLabel = new Label("City:");
        Label coachLabel = new Label("Coach:");
        Label captainLabel = new Label("Captain:");

        // Create VBox to hold Labels and TextFields
        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(teamNameLabel, teamNameField, cityLabel, cityField,
                coachLabel, coachField, captainLabel, captainField);
        vbox.setPadding(new Insets(10));

        // Create Button for confirmation
        Button addButton = new Button("Add");

        // Create VBox to hold the form and Button
        VBox formWithButton = new VBox(10);
        formWithButton.getChildren().addAll(vbox, addButton);
        formWithButton.setAlignment(Pos.CENTER);

        // Create Stage to display the form
        Stage stage = new Stage();
        stage.setTitle("Add New Team");
        stage.initModality(Modality.APPLICATION_MODAL);

        // Set action for the Add button
        addButton.setOnAction(event -> {
            // Retrieve input values
            String teamName = teamNameField.getText();
            String city = cityField.getText();
            String coach = coachField.getText();
            String captain = captainField.getText();

            // Call method to insert team into database
            DAO.insertTeam(teamName, city, coach, captain);
            // Refresh teamComboBox
            Main mainApp = new Main();
            mainApp.loadDataFromDatabase();
            mainApp.teamComboBox.setValue(teamName);

            // Close the stage after adding the team
            stage.close();
        });

        // Create Scene and set it to the Stage
        Scene scene = new Scene(formWithButton);
        stage.setScene(scene);

        // Show the Stage
        stage.show();
    }

    private void loadDataFromDatabase() {
        ArrayList<Object> teams = DAO.searchMatches();
        ObservableList<Object> teamList = FXCollections.observableArrayList(teams);
        teamComboBox.setItems(teamList);

        ArrayList<Object> dates = DAO.searchDate();
        ObservableList<Object> dateList = FXCollections.observableArrayList(dates);
        startDateComboBox.setItems(dateList);
        endDateComboBox.setItems(dateList);
    }

//add there part but also add in controller and fxml
    private void searchMatches() {
        statsTable.getItems().clear();
        statsTable.getColumns().clear();
        String[] columns = new String[]{"Home Team", "Away Team", "Date", "Result", "Home Goals", "Away Goals", "Stadium", "Referee"};
        for (int i = 0; i < columns.length; i++) {
            final int j = i;
            TableColumn<String[], String> column = new TableColumn<>(columns[i]);
            column.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[j]));
            column.setCellFactory(TextFieldTableCell.forTableColumn());  // Enable editing
            statsTable.getColumns().add(column);
        }
        String selectedTeam = (String) teamComboBox.getValue();
        String startDate = (String) startDateComboBox.getValue();
        String endDate = (String) endDateComboBox.getValue();

        LocalDate stDate = LocalDate.parse(startDate);
        LocalDate eDate = LocalDate.parse(endDate);
        if (stDate.compareTo(eDate) > 0) {
            // Swap startDate and endDate if startDate is after endDate
            String temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        int teamId = DAO.getTeamIdByName(selectedTeam);

         ArrayList<Object[]> matches = DAO.selectMatches(teamId, startDate, endDate);

        ObservableList<String[]> matchData = FXCollections.observableArrayList();
        for (Object[] match : matches) {
            String[] matchStringArray = new String[match.length];
            for (int i = 0; i < match.length; i++) {
                matchStringArray[i] = match[i].toString();
            }
            matchData.add(matchStringArray);
        }
        statsTable.getItems().clear();
       // statsTable.getColumns().addAll(new TableColumn<>("1"),new TableColumn<>("1"),new TableColumn<>("1"),new TableColumn<>("1"),new TableColumn<>("1"),new TableColumn<>("1"),new TableColumn<>("1"),new TableColumn<>("1"));
        statsTable.getItems().addAll( matchData);
    }


    private void updateEditMode() {
        if (!editMode) {
            // Enable table editing
            statsTable.setEditable(true);
            // Set the columns to editable
            statsTable.getColumns().forEach(column -> {
                if (column instanceof TableColumn) {
                    TableColumn<String[], String> tableColumn = (TableColumn<String[], String>) column;
                    tableColumn.setEditable(true);
                    tableColumn.setOnEditCommit(event -> {
                        TableColumn.CellEditEvent<String[], String> ce = (TableColumn.CellEditEvent<String[], String>) event;
                        int editedRow = ce.getTablePosition().getRow();
                        int editedColumn = ce.getTablePosition().getColumn();

                        String[] rowData = ce.getRowValue();
                        String homeTeam = rowData[0];
                        String awayTeam = rowData[1];
                        String date = rowData[2];

                        switch (editedColumn) {
                            case 2:
                                // If date column is edited, update the date in the database
                                deleteDateInComboBox(date);
                                DAO.updateMatchDate(homeTeam, awayTeam, date, ce.getNewValue());
                                addDateInComboBox(ce.getNewValue());

                                // Update the table model with new data
                                rowData[editedColumn] = ce.getNewValue();
                                break;
                            case 4:
                                // If home goals column is edited, update the home goals in the database
                                DAO.updateMatchHomeGoals(homeTeam, awayTeam, date, ce.getNewValue());

                                // Update the table model with new data
                                rowData[editedColumn] = ce.getNewValue();
                                break;
                            case 5:
                                // If away goals column is edited, update the away goals in the database
                                DAO.updateMatchAwayGoals(homeTeam, awayTeam, date, ce.getNewValue());

                                // Update the table model with new data
                                rowData[editedColumn] = ce.getNewValue();
                                break;
                        }
                    });
                }
            });
        } else {
            // Disable table editing
            statsTable.setEditable(false);
        }
    }





    private void deleteSelectedRow() {
        String[] selectedItem = statsTable.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            String homeTeam = selectedItem[0];
            String awayTeam = selectedItem[1];
            String date = selectedItem[2];
            DAO.deleteMatch(homeTeam, awayTeam, date);

            // Remove deleted row from TableView
            statsTable.getItems().remove(selectedItem);
        } else {
            // Display error message if no row is selected
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Please select a row to delete.");
            alert.showAndWait();
        }
    }




    private void addDateInComboBox(String date) {
        if (!startDateComboBox.getItems().contains(date)) {
            startDateComboBox.getItems().add(date);
            endDateComboBox.getItems().add(date);
        }
    }
    private void deleteDateInComboBox(String date) {
        ObservableList<Object> startDateList = startDateComboBox.getItems();
        ObservableList<Object> endDateList = endDateComboBox.getItems();

        int indexOfDate = startDateList.indexOf(date);
        if (indexOfDate != -1 && DAO.countOfDateInMatches(date) == 1) {
            startDateList.remove(indexOfDate);
            endDateList.remove(indexOfDate);
        }
    }


    public static void main(String[] args) {
        launch();
    }
}

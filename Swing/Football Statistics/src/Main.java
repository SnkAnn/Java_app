import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class Main extends JFrame {
    private JComboBox<String> teamComboBox;
    private JComboBox<String> startDateComboBox;
    private JComboBox<String> endDateComboBox;
    private JButton searchButton;
    private JButton deleteButton;
    private JButton addButton;
    private JTable statsTable;
    private JToggleButton editToggle;
    private boolean editMode = false; // Флаг для отслеживания режима редактирования


    public Main() {
        setTitle("Football Statistics");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel comboBoxPanel = new JPanel(); // Создаем панель для ComboBox'ов
        comboBoxPanel.setLayout(new FlowLayout()); // Используем FlowLayout для ComboBox'ов


        // Создаем выпадающий список для команд
        teamComboBox = new JComboBox<>();
        comboBoxPanel.add(new JLabel("Select Team:"));
        comboBoxPanel.add(teamComboBox);

        // Создаем выпадающие списки для дат
        startDateComboBox = new JComboBox<>();
        endDateComboBox = new JComboBox<>();
        comboBoxPanel.add(new JLabel("Select Start Date:"));
        comboBoxPanel.add(startDateComboBox);
        comboBoxPanel.add(new JLabel("Select End Date:"));
        comboBoxPanel.add(endDateComboBox);


        // Добавляем панель с ComboBox'ами в верхнюю часть панели
        panel.add(comboBoxPanel, BorderLayout.NORTH);

        // Создаем панель для кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
// Создаем кнопку для поиска статистики
        searchButton = new JButton("Search");
        buttonPanel.add(searchButton);
        // Создаем переключатель для включения и выключения режима редактирования
        editToggle = new JToggleButton("Edit Mode");
        buttonPanel.add(editToggle);
        deleteButton = new JButton("Delete");
        buttonPanel.add(deleteButton);
        addButton = new JButton("Add");
        buttonPanel.add(addButton);

// Добавляем панель с кнопками внизу панели
        panel.add(buttonPanel, BorderLayout.CENTER);

        // Создаем текстовую область для отображения статистики
        statsTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(statsTable);
        // Добавляем таблицу в нижнюю часть панели
        panel.add(scrollPane, BorderLayout.SOUTH);

        // Добавляем слушателя для кнопки поиска
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedTeam = (String) teamComboBox.getSelectedItem();
                String startDate = (String) startDateComboBox.getSelectedItem();
                String endDate = (String) endDateComboBox.getSelectedItem();

                // Вызовите метод для поиска матчей и отображения статистики
                searchMatches(selectedTeam, startDate, endDate);
            }
        });
        // Добавляем слушатель событий к переключателю режима редактирования
        editToggle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Переключаем режим редактирования и обновляем таблицу
                editMode = editToggle.isSelected();
                updateEditMode();
                if (editMode) {
                    editToggle.setText("change");
                } else {
                    editToggle.setText("Edit Mode");
                }

            }
        });
        // Добавляем слушателя событий к кнопке удаления
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = statsTable.getSelectedRow();
                if (selectedRow != -1) { // Проверяем, что строка выбрана
                    DefaultTableModel model = (DefaultTableModel) statsTable.getModel();
                    String homeTeam = (String) model.getValueAt(selectedRow, 0);
                    String awayTeam = (String) model.getValueAt(selectedRow, 1);
                    String date = (String) model.getValueAt(selectedRow, 2);
                    // Удаляем выбранную строку из базы данных
                    deleteDateInComboBox(date);
                    DAO.deleteMatch(homeTeam, awayTeam, date);
                    // Удаляем выбранную строку из таблицы
                    model.removeRow(selectedRow);


                } else {
                    JOptionPane.showMessageDialog(Main.this, "Please select a row to delete.");
                }
            }
        });
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddMatchForm();
            }
        });

        add(panel);
        setVisible(true);
        loadDataFromDatabase();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }

    protected static void showAddTeamForm() {
        JTextField teamNameField = new JTextField();
        JTextField cityField = new JTextField();
        JTextField coachField = new JTextField();
        JTextField captainField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Team Name:"));
        panel.add(teamNameField);
        panel.add(new JLabel("City:"));
        panel.add(cityField);
        panel.add(new JLabel("Coach:"));
        panel.add(coachField);
        panel.add(new JLabel("Captain:"));
        panel.add(captainField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add New Team",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            // Получаем введенные значения из полей
            String teamName = teamNameField.getText();
            String city = cityField.getText();
            String coach = coachField.getText();
            String captain = captainField.getText();

            // Вызываем метод добавления команды в базу данных
            DAO.insertTeam(teamName, city, coach, captain);
            new Main().teamComboBox.addItem(teamName);
        }
    }

    private void loadDataFromDatabase() {

        ArrayList<Object> teams = DAO.searchMatches();
        while (!teams.isEmpty()) {
            teamComboBox.addItem((String) teams.remove(0));
        }


        // Загрузите список дат
        ArrayList<Object> date = DAO.searchDate();
        while (!date.isEmpty()) {
            startDateComboBox.addItem((String) date.remove(0));
            endDateComboBox.addItem((String) date.remove(0));
        }

    }

    private void searchMatches(String selectedTeam, String startDate, String endDate) {

        LocalDate stDate = LocalDate.parse(startDate);
        LocalDate eDate = LocalDate.parse(endDate);
        if (stDate.compareTo(eDate) > 0) {//stDate идет после eDate
            String temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        int teamId = DAO.getTeamIdByName(selectedTeam);

        DefaultTableModel tableModel = new DefaultTableModel();
        // Устанавливаем названия столбцов
        tableModel.addColumn("Home Team");
        tableModel.addColumn("Away Team");
        tableModel.addColumn("Date");
        tableModel.addColumn("Result");
        tableModel.addColumn("Home Goals");
        tableModel.addColumn("Away Goals");
        tableModel.addColumn("Stadium");
        tableModel.addColumn("Referee");
        ArrayList<Object[]> matches = DAO.selectMatches(teamId, startDate, endDate);
        while (!matches.isEmpty()) {
            tableModel.addRow(matches.remove(0));
        }
        // Устанавливаем модель таблицы для statsTable
        statsTable.setModel(tableModel);

    }

    private void updateEditMode() {
        if (editMode) {
            // Разрешаем редактирование таблицы
            statsTable.setEnabled(true);
            statsTable.setFocusable(true);
            // Добавляем слушателя изменений в таблице
            statsTable.getModel().addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    if (e.getType() == TableModelEvent.UPDATE && editMode) {
                        int row = e.getFirstRow();
                        int column = e.getColumn();
                        DefaultTableModel model = (DefaultTableModel) statsTable.getModel();
                        Object data = model.getValueAt(row, column);
                        String homeTeam = (String) model.getValueAt(row, 0);
                        String awayTeam = (String) model.getValueAt(row, 1);
                        String date = DAO.selectDate(homeTeam,awayTeam);
                        switch (column) {
                            case 2:
                                // Если изменена колонка с датой, обновляем дату в базе данных
                                deleteDateInComboBox(date);
                                DAO.updateMatchDate(homeTeam, awayTeam, date, (String) data);
                                addDateInComboBox((String) data);
                                break;
                            case 4:
                                // Если изменена колонка с результатом, обновляем результат в базе данных
                                DAO.updateMatchHomeGoals(homeTeam, awayTeam, date, data);
                                break;
                            case 5:
                                DAO.updateMatchAwayGoals(homeTeam, awayTeam, date, data);
                                break;
                        }
                    }
                }
            });
        }
    }

    private void showAddMatchForm() {

        JTextField homeTeamField = new JTextField(10);
        JTextField awayTeamField = new JTextField(10);
        JTextField dateField = new JTextField(10);
        JTextField resultField = new JTextField(10);
        JTextField homeGoalsField = new JTextField(10);
        JTextField awayGoalsField = new JTextField(10);
        JTextField stadiumField = new JTextField(10);
        JTextField refereeField = new JTextField(10);
        // Добавьте другие компоненты для ввода данных, если это необходимо

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Home Team:"));
        panel.add(homeTeamField);
        panel.add(new JLabel("Away Team:"));
        panel.add(awayTeamField);
        panel.add(new JLabel("Date (YYYY-MM-DD):"));
        panel.add(dateField);
        panel.add(new JLabel("Result:"));
        panel.add(resultField);
        panel.add(new JLabel("Home Goals:"));
        panel.add(homeGoalsField);
        panel.add(new JLabel("Away Goals:"));
        panel.add(awayGoalsField);
        panel.add(new JLabel("Stadium:"));
        panel.add(stadiumField);
        panel.add(new JLabel("Referee:"));
        panel.add(refereeField);

        int res = JOptionPane.showConfirmDialog(null, panel, "Add Data", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            // Получите введенные пользователем данные
            String homeTeam = homeTeamField.getText();
            String awayTeam = awayTeamField.getText();
            String date = dateField.getText();
            String result = resultField.getText();
            String homeGoals = homeGoalsField.getText();
            String awayGoals = awayGoalsField.getText();
            String stadium = stadiumField.getText();
            String referee = refereeField.getText();
            // Добавьте введенные данные в базу данных
            DAO.insertMatch(homeTeam, awayTeam, date, result, homeGoals, awayGoals, stadium, referee);
            addDateInComboBox(date);
        }
    }
    private void addDateInComboBox(String date){
        if (((DefaultComboBoxModel<String>) startDateComboBox.getModel()).getIndexOf(date) == -1) {
            startDateComboBox.addItem(date);
            endDateComboBox.addItem(date);
        }
    }
    private void deleteDateInComboBox(String date){
        DefaultComboBoxModel<String> startDateModel = (DefaultComboBoxModel<String>) startDateComboBox.getModel();
        DefaultComboBoxModel<String> endDateModel=(DefaultComboBoxModel<String>) endDateComboBox.getModel();
        int indexOfDate = startDateModel.getIndexOf(date);
        if (indexOfDate != -1 && DAO.countOfDateInMatches(date) == 1) {
            startDateModel.removeElementAt(indexOfDate);
            endDateModel.removeElementAt(indexOfDate);
        }
    }

}

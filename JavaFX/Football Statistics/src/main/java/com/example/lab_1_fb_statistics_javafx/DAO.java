package com.example.lab_1_fb_statistics_javafx;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DAO {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/fbmatch_db";
    private static final String DB_USER = "Ann";
    private static final String DB_PASSWORD = "MySQL0.1mina1a2a3a";

    protected static void updateMatchDate(String homeTeam, String awayTeam, String oldDate, String newDate) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "UPDATE matches SET Date = ? WHERE HomeTeamID = ? AND AwayTeamID = ? AND Date=?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, newDate);
                statement.setInt(2, getTeamIdByName(homeTeam));
                statement.setInt(3, getTeamIdByName(awayTeam));
                statement.setString(4, oldDate);
                statement.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected static String selectDate(String homeTeam, String awayTeam) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT Date FROM matches  WHERE HomeTeamID = ? AND AwayTeamID = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setInt(1, getTeamIdByName(homeTeam));
                statement.setInt(2, getTeamIdByName(awayTeam));
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getString("Date");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    protected static void updateMatchAwayGoals(String homeTeam, String awayTeam, String date, Object AwayGoal) {
        try {
            // Устанавливаем соединение с базой данных
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Выполняем запрос для обновления результата в базе данных
            String query = "UPDATE matches SET AwayGoals = ? WHERE HomeTeamID = ? AND AwayTeamID = ? AND Date = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setObject(1, AwayGoal); // Новый результат матча
            statement.setInt(2, getTeamIdByName(homeTeam)); // ID домашней команды
            statement.setInt(3, getTeamIdByName(awayTeam)); // ID гостевой команды
            statement.setString(4, date); // Дата матча
            // Выполняем запрос
            statement.executeUpdate();

            // Закрываем соединение с базой данных
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected static void updateMatchHomeGoals(String homeTeam, String awayTeam, String date, Object HomeGoal) {
        try {
            // Устанавливаем соединение с базой данных
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Выполняем запрос для обновления результата в базе данных
            String query = "UPDATE matches SET HomeGoals = ? WHERE HomeTeamID = ? AND AwayTeamID = ? AND Date = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setObject(1, HomeGoal); // Новый результат матча
            statement.setInt(2, getTeamIdByName(homeTeam)); // ID домашней команды
            statement.setInt(3, getTeamIdByName(awayTeam)); // ID гостевой команды
            statement.setString(4, date); // Дата матча
            // Выполняем запрос
            statement.executeUpdate();

            // Закрываем соединение с базой данных
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Метод для получения ID команды по её имени
    protected static int getTeamIdByName(String teamName) {
        int teamId = -1;
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement statement = connection.prepareStatement("SELECT TeamID FROM footballteams WHERE TeamName = ?");
            statement.setString(1, teamName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                teamId = resultSet.getInt("TeamID");
            }
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return teamId;
    }

    protected static String getTeamNameById(int teamId) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement statement = connection.prepareStatement("SELECT TeamName FROM footballteams WHERE TeamID = ?");
            statement.setInt(1, teamId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String teamName = resultSet.getString("TeamName");
                connection.close();
                return teamName;
            }
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null; // В случае ошибки или если команда с данным идентификатором не найдена
    }

    protected static ArrayList<Object[]> selectMatches(int teamId, String startDate, String endDate) {
        ArrayList<Object[]> arr = null;
        try {
            // Выполните запрос для получения информации о матчах выбранной команды в выбранный период времени
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String query = "SELECT * FROM matches WHERE (HomeTeamID = ? OR AwayTeamID = ?) AND Date BETWEEN ? AND ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, teamId);
            statement.setInt(2, teamId);
            statement.setString(3, startDate);
            statement.setString(4, endDate);
            arr = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int homeTeamId = resultSet.getInt("HomeTeamID");
                int awayTeamId = resultSet.getInt("AwayTeamID");
                String homeTeamName = DAO.getTeamNameById(homeTeamId);
                String awayTeamName = DAO.getTeamNameById(awayTeamId);
                String date = resultSet.getString("Date");
                String result = resultSet.getString("Result");
                int homeGoals = resultSet.getInt("HomeGoals");
                int awayGoals = resultSet.getInt("AwayGoals");
                String stadium = resultSet.getString("Stadium");
                String referee = resultSet.getString("Referee");
                // Добавляем данные в модель таблицы
                arr.add(new Object[]{homeTeamName, awayTeamName, date, result, homeGoals, awayGoals, stadium, referee});
            }

            // Закройте соединение с базой данных
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return arr;
    }
    protected static int countOfDateInMatches( String date) {
        int count=0;
        try {
            // Выполните запрос для получения информации о матчах выбранной команды в выбранный период времени
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String query = "SELECT * FROM matches WHERE   Date=?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, date);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                count++;
            }
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return count;
    }

    // Метод для поиска матчей выбранной команды в заданном временном интервале
    protected static ArrayList<Object> searchMatches() {
        ArrayList<Object> teams = new ArrayList<>();
        try {
            // Установите соединение с базой данных
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // Загрузите список команд
            PreparedStatement teamStatement = connection.prepareStatement("SELECT TeamName FROM footballteams");
            ResultSet teamResultSet = teamStatement.executeQuery();
            while (teamResultSet.next()) {
                teams.add(teamResultSet.getString("TeamName"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return teams;
    }

    protected static ArrayList<Object> searchDate() {
        ArrayList<Object> date = new ArrayList<>();
        try {
            // Установите соединение с базой данных
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            PreparedStatement dateStatement = connection.prepareStatement("SELECT DISTINCT Date FROM matches");
            ResultSet dateResultSet = dateStatement.executeQuery();
            while (dateResultSet.next()) {
                date.add(dateResultSet.getString("Date"));
                date.add(dateResultSet.getString("Date"));
            }

            // Закройте соединение с базой данных
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return date;
    }

    protected static void deleteMatch(String homeTeam, String awayTeam, String date) {

        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            // Создаем SQL-запрос для удаления записи
            String query = "DELETE FROM matches WHERE HomeTeamID = ? AND AwayTeamID = ? AND Date = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            // Устанавливаем значения параметров запроса
            statement.setString(1, String.valueOf(getTeamIdByName(homeTeam)));
            statement.setString(2, String.valueOf(getTeamIdByName(awayTeam)));
            statement.setString(3, date);
            // Выполняем запрос на удаление
            statement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected static void insertMatch(String homeTeam, String awayTeam, String date, String result, String homeGoals, String awayGoals, String stadium, String referee) throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String queryMatches = "INSERT INTO matches (HomeTeamID, AwayTeamID, Date, Result, HomeGoals, AwayGoals, Stadium, Referee) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(queryMatches);

            // Устанавливаем значения параметров запроса
            int homeTeamId = getTeamIdByName(homeTeam);
            if (homeTeamId != -1) {
                statement.setInt(1, homeTeamId);
            } else {
                // If home team does not exist, show add team form
                Main.showAddTeamForm();
                homeTeamId = getTeamIdByName(homeTeam);
                if (homeTeamId != -1) {
                    statement.setInt(1, homeTeamId);
                } else {
                    throw new SQLException("Failed to insert new home team into the database.");
                }
            }

            int awayTeamId = getTeamIdByName(awayTeam);
            if (awayTeamId != -1) {
                statement.setInt(2, awayTeamId);
            } else {
                // If away team does not exist, show add team form
                Main.showAddTeamForm();
                awayTeamId = getTeamIdByName(awayTeam);
                if (awayTeamId != -1) {
                    statement.setInt(2, awayTeamId);
                } else {
                    throw new SQLException("Failed to insert new away team into the database.");
                }
            }

            statement.setString(3, date);
            statement.setString(4, result);
            statement.setString(5, homeGoals);
            statement.setString(6, awayGoals);
            statement.setString(7, stadium);
            statement.setString(8, referee);

            // Выполняем запрос на вставку
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error inserting match: " + e.getMessage());
        }
    }


    protected static void insertTeam(String teamName, String city, String coach, String captain) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            String queryFootballTeam = "INSERT INTO footballteams (TeamName,City,Coach,Captain ) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(queryFootballTeam);
            statement.setString(1, teamName);
            statement.setString(2, city);
            statement.setString(3, coach);
            statement.setString(4, captain);
            // Выполняем запрос на вставку
            statement.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}

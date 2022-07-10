package database;

import application.Helpers;
import javafx.scene.control.Alert;
import java.sql.*;

public abstract class JDBC {
    private static final String protocol ="jdbc";
    private static final String vendor = ":sqlite:";
    private static final String location = "database.db";
    private static final String jdbcUrl = protocol + vendor + location;
    public static Connection connection;

    public static void openConnection() {
        try {
            connection = DriverManager.getConnection(jdbcUrl);

            // Need to enable foreign keys on each connection with SQLITE.
            Statement fks = connection.createStatement();
            fks.executeUpdate("PRAGMA foreign_keys = ON;");
            fks.close();
        } catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText("Error opening connection.");
            alert.setContentText(e.toString());
            alert.showAndWait();

            Helpers.log("ERROR - JDBC.openConnection - " + e);
        }
    }

    public static void closeConnection() {
        try {
            connection.close();
        } catch(Exception e) {
            Helpers.log("ERROR - JDBC.closeConnection - " + e);
        }

    }

    public static void setup() {
        try {
            JDBC.openConnection();
            initialSetup();
        } catch(SQLException e) {
            Helpers.log("ERROR - JDBC.setup - " + e);
        } finally {
            JDBC.closeConnection();
        }
    }

    private static void initialSetup() throws SQLException {
        String[] sqlStatements = {
                "CREATE TABLE IF NOT EXISTS categories (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL UNIQUE" +
                        ");",

                "CREATE TABLE IF NOT EXISTS subcategories (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL," +
                        "category_id INTEGER NOT NULL," +
                        "FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE" +
                        ");",

                "CREATE TABLE IF NOT EXISTS departments (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL UNIQUE" +
                        ");",

                "CREATE TABLE IF NOT EXISTS shifts (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "name TEXT NOT NULL UNIQUE" +
                        ");",

                "CREATE TABLE IF NOT EXISTS turnover (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "date TEXT NOT NULL," +
                        "shift_id INTEGER NOT NULL," +
                        "department_id INTEGER NOT NULL," +
                        "notes TEXT," +
                        "FOREIGN KEY(shift_id) REFERENCES shifts(id) ON DELETE CASCADE," +
                        "FOREIGN KEY(department_id) REFERENCES departments(id) ON DELETE CASCADE" +
                        ");",

                "CREATE TABLE IF NOT EXISTS issues (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "turnover_id INTEGER NOT NULL," +
                        "start_time TEXT NOT NULL," +
                        "end_time TEXT NOT NULL," +
                        "category_id INTEGER," +
                        "subcategory_id INTEGER," +
                        "notes TEXT," +
                        "FOREIGN KEY(turnover_id) REFERENCES turnover(id) ON DELETE CASCADE," +
                        "FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE SET NULL," +
                        "FOREIGN KEY(subcategory_id) REFERENCES subcategories(id) ON DELETE SET NULL" +
                        ");",

                "CREATE TABLE IF NOT EXISTS users (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "department_id INTEGER NOT NULL," +
                        "shift_id INTEGER NOT NULL," +
                        "permission_level INTEGER," +
                        "username TEXT NOT NULL UNIQUE," +
                        "hash TEXT NOT NULL," +
                        "salt TEXT NOT NULL," +
                        "password_change BOOLEAN NOT NULL," +
                        "FOREIGN KEY(shift_id) REFERENCES shifts(id) ON DELETE RESTRICT," +
                        "FOREIGN KEY(department_id) REFERENCES departments(id) ON DELETE RESTRICT" +
                        ");"
        };

        for (String sql : sqlStatements) {
            PreparedStatement statement = JDBC.connection.prepareStatement(sql);
            statement.execute();
        }

        String sql = "INSERT INTO users (department_id, shift_id, permission_level, username, hash, salt, password_change)" +
                " VALUES (NULL, NULL, ?, ?, ?, ?, ?);";
        PreparedStatement statement = JDBC.connection.prepareStatement(sql);

        sql = "INSERT INTO categories (id, name) VALUES (NULL, ?);";
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "General");
        statement.execute();

        sql = "INSERT INTO categories (id, name) VALUES (NULL, ?);";
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "Staffing");
        statement.execute();

        sql = "INSERT INTO categories (id, name) VALUES (NULL, ?);";
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "Machine");
        statement.execute();

        sql = "INSERT INTO subcategories (id, name, category_id) VALUES (NULL, ?, ?);";
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "Other");
        statement.setInt(2, 1);
        statement.execute();

        sql = "INSERT INTO subcategories (id, name, category_id) VALUES (NULL, ?, ?);";
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "Something");
        statement.setInt(2, 1);
        statement.execute();

        sql = "INSERT INTO subcategories (id, name, category_id) VALUES (NULL, ?, ?);";
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "Another Thing");
        statement.setInt(2, 1);
        statement.execute();

        sql = "INSERT INTO shifts (id, name) VALUES (NULL, ?);";
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "A - Day");
        statement.execute();
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "A - Night");
        statement.execute();
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "B - Day");
        statement.execute();
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "B - Night");
        statement.execute();
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "C - Day");
        statement.execute();
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "C - Night");
        statement.execute();
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "D - Day");
        statement.execute();
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "D - Night");
        statement.execute();
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "Weekend - Day");
        statement.execute();
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "Weekend - Night");
        statement.execute();

        sql = "INSERT INTO departments (id, name) VALUES (NULL, ?);";
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "Department A");
        statement.execute();

        sql = "INSERT INTO departments (id, name) VALUES (NULL, ?);";
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "Department B");
        statement.execute();

        sql = "INSERT INTO departments (id, name) VALUES (NULL, ?);";
        statement = JDBC.connection.prepareStatement(sql);
        statement.setString(1, "Department C");
        statement.execute();

        sql = "INSERT INTO users (department_id, shift_id, permission_level, username, hash, salt, password_change)" +
                " VALUES (1, 1, ?, ?, ?, ?, ?);";
        statement = JDBC.connection.prepareStatement(sql);
        statement.setInt(1, 1);
        statement.setString(2, "admin");
        statement.setString(3, "c5766697e61787349318ae9359f7f5647e5b695ab80535d44b8bd81dd3ea6a83"); // password is admin
        statement.setString(4, "HlVfHJ42zxLr");
        statement.setBoolean(5, false);
        statement.execute();

        statement = JDBC.connection.prepareStatement(sql);
        statement.setInt(1, 2);
        statement.setString(2, "user");
        statement.setString(3, "4669454d462505237170899e0671c6205063151b4527fde22d3bf0eded9000e2"); // password is user
        statement.setString(4, "218Dojiw");
        statement.setBoolean(5, false);
        statement.execute();

        statement.close();
    }
}

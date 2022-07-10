package database;

import application.Helpers;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import model.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

public abstract class QueryManager {

    /******************************************************************************
     * isValidLogin                                                               *
     ******************************************************************************/
    public static boolean isValidLogin(String username, String password) {
        String sqlUsers = "SELECT salt FROM users WHERE username = ?;";
        String sqlHash = "SELECT COUNT(*) AS cnt FROM users WHERE username = ? and hash = ?;";
        int count = 0;

        try {
            // Get the user's salt.
            PreparedStatement ps = JDBC.connection.prepareStatement(sqlUsers);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            String hashedPassword = "";
            if (rs.next()) {
                String salt = rs.getString("salt");

                // Build the password with the salt.
                hashedPassword = password + salt;

                // Convert to hash with SHA-256.
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(hashedPassword.getBytes(StandardCharsets.UTF_8));

                StringBuilder hexString = new StringBuilder();
                for(byte b : digest) {
                    String hex = Integer.toHexString(0xff & b);
                    if(hex.length() == 1)
                        hexString.append('0');
                    hexString.append(hex);
                }

                hashedPassword = hexString.toString();
            } else {
                return false;
            }
            ps.close();
            rs.close();

            // Check for user using the hashed password.
            ps = JDBC.connection.prepareStatement(sqlHash);
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            rs = ps.executeQuery();
            rs.next();
            count = rs.getInt("cnt");
            rs.close();
            ps.close();
        } catch(SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Unable to login");
            alert.setContentText("An error occurred while logging in.");
            alert.showAndWait();

            Helpers.log("ERROR - QueryManager.isValidLogin - " + e);
        } catch(NoSuchAlgorithmException e) {
            Helpers.log("ERROR - QueryManager.isValidLogin - Cannot find SHA256 algorithm - " + e);
        }

        return count > 0;
    }

    /******************************************************************************
     * checkPasswordChange                                                        *
     ******************************************************************************/
    public static boolean checkPasswordChange(String username, String password) {
        String sqlUsers = "SELECT salt FROM users WHERE username = ?;";
        String sqlHash = "SELECT password_change FROM users WHERE username = ? and hash = ?;";
        boolean requiresChange = false;

        try {
            // Get the user's salt.
            PreparedStatement ps = JDBC.connection.prepareStatement(sqlUsers);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            String hashedPassword = "";
            if (rs.next()) {
                String salt = rs.getString("salt");

                // Build the password with the salt.
                hashedPassword = password + salt;

                // Convert to hash with SHA-256.
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(hashedPassword.getBytes(StandardCharsets.UTF_8));

                StringBuilder hexString = new StringBuilder();
                for(byte b : digest) {
                    String hex = Integer.toHexString(0xff & b);
                    if(hex.length() == 1)
                        hexString.append('0');
                    hexString.append(hex);
                }

                hashedPassword = hexString.toString();
            } else {
                return false;
            }
            ps.close();
            rs.close();

            // Check for user using the hashed password.
            ps = JDBC.connection.prepareStatement(sqlHash);
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            rs = ps.executeQuery();
            if(rs.next()) {
                requiresChange = rs.getBoolean("password_change");
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            Helpers.displayGenericError();
            Helpers.log("ERROR - QueryManager.checkPasswordChange - " + e);
        } catch(NoSuchAlgorithmException e) {
            Helpers.log("ERROR - QueryManager.checkPasswordChange - Cannot find SHA256 algorithm - " + e);
        }

        return requiresChange;
    }

    /******************************************************************************
     * updatePassword                                                             *
     ******************************************************************************/
    public static void updatePassword(String username, String oldPassword, String newPassword) {
        String sql = "UPDATE users SET hash = ?, password_change = FALSE WHERE username = ? AND hash = ?;";
        String sqlUsers = "SELECT salt FROM users WHERE username = ?;";

        try {
            // Get the user's salt.
            PreparedStatement ps = JDBC.connection.prepareStatement(sqlUsers);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            String hashedPassword = "";
            String salt = "";
            if (rs.next()) {
                salt = rs.getString("salt");

                // Build the password with the salt.
                hashedPassword = oldPassword + salt;

                // Convert to hash with SHA-256.
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(hashedPassword.getBytes(StandardCharsets.UTF_8));

                StringBuilder hexString = new StringBuilder();
                for(byte b : digest) {
                    String hex = Integer.toHexString(0xff & b);
                    if(hex.length() == 1)
                        hexString.append('0');
                    hexString.append(hex);
                }

                hashedPassword = hexString.toString();
            } else {
                throw new SQLException("Unable to check hash");
            }
            ps.close();
            rs.close();

            String newHash = newPassword + salt;
            // Convert to hash with SHA-256.
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(newHash.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for(byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            newHash = hexString.toString();

            ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, newHash);
            ps.setString(2, username);
            ps.setString(3, hashedPassword);
            ps.executeUpdate();
            ps.close();
        } catch(SQLException e) {
            Helpers.displayError("Update Error", "An error occurred while updating the password.");
            Helpers.log("ERROR - QueryManager.updatePassword - " + e);
        } catch(NoSuchAlgorithmException e) {
            Helpers.log("ERROR - QueryManager.updatePassword - Cannot find SHA256 algorithm - " + e);
        }
    }

    /******************************************************************************
     * getTurnoverList                                                            *
     ******************************************************************************/
    public static ObservableList<Turnover> getTurnoverList() {
        String sql = "SELECT turnover.*, " +
                "shifts.id AS shift_id, " +
                "shifts.name AS shift_name, " +
                "departments.id AS department_id, " +
                "departments.name AS department_name " +
                "FROM turnover " +
                "INNER JOIN shifts ON turnover.shift_id = shifts.id " +
                "INNER JOIN departments ON turnover.department_id = departments.id " +
                "ORDER BY date;";
        ObservableList<Turnover> lstTurnover = FXCollections.observableArrayList();

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                BasicIdName shift = new BasicIdName(rs.getInt("shift_id"), rs.getString("shift_name"));
                BasicIdName department = new BasicIdName(rs.getInt("department_id"), rs.getString("department_name"));
                String notes = rs.getString("notes");
                lstTurnover.add(new Turnover(id, date, shift, department, notes));
            }
            rs.close();
            ps.close();
            return lstTurnover;
        } catch(SQLException e) {
            Helpers.displayError("Turnover Error", "An error occurred while retrieving the turnover.");
            Helpers.log("ERROR - QueryManager.getTurnoverList - " + e);
        }

        return lstTurnover;
    }

    /******************************************************************************
     * turnoverExist                                                              *
     ******************************************************************************/
    public static boolean turnoverExists(Turnover turnover) {
        String sql = "SELECT COUNT(*) AS cnt FROM turnover WHERE date = ? AND shift_id = ? AND department_id = ? AND id IS NOT ?";
        int count = 0;

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setObject(1, turnover.getDate());
            ps.setInt(2, turnover.getShift().getId());
            ps.setInt(3, turnover.getDepartment().getId());
            ps.setInt(4, turnover.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt("cnt");
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            Helpers.displayGenericError();
            Helpers.log("ERROR - QueryManager.turnoverExists - " + e);
        }

        return count > 0;
    }

    /******************************************************************************
     * getTurnoverListBetweenDates                                                *
     ******************************************************************************/
    public static ObservableList<Turnover> getTurnoverListBetweenDates(LocalDate start, LocalDate end) {
        String sql = "SELECT turnover.*, " +
                "shifts.id AS shift_id, " +
                "shifts.name AS shift_name, " +
                "departments.id AS department_id, " +
                "departments.name AS department_name " +
                "FROM turnover " +
                "INNER JOIN shifts ON turnover.shift_id = shifts.id " +
                "INNER JOIN departments ON turnover.department_id = departments.id " +
                "WHERE date >= ? AND date <= ?";
        ObservableList<Turnover> lstTurnover = FXCollections.observableArrayList();

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setObject(1, start);
            ps.setObject(2, end);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                BasicIdName shift = new BasicIdName(rs.getInt("shift_id"), rs.getString("shift_name"));
                BasicIdName department = new BasicIdName(rs.getInt("department_id"), rs.getString("department_name"));
                String notes = rs.getString("notes");
                lstTurnover.add(new Turnover(id, date, shift, department, notes));
            }
            rs.close();
            ps.close();
            return lstTurnover;
        } catch(SQLException e) {
            Helpers.displayError("Turnover Error", "An error occurred while retrieving the turnover.");
            Helpers.log("ERROR - QueryManager.getTurnoverListBetweenDates - " + e);
        }

        return lstTurnover;
    }

    /******************************************************************************
     * getCategories                                                              *
     ******************************************************************************/
    public static ObservableList<Category> getCategories() {
        String sql = "SELECT * FROM categories;";
        ObservableList<Category> lstCategories = FXCollections.observableArrayList();

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                lstCategories.add(new Category(id, name));
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            Helpers.displayError("Categories Error", "An error occurred while retrieving the categories.");
            Helpers.log("ERROR - QueryManager.getCategories - " + e);
        }

        return lstCategories;
    }

    /******************************************************************************
     * getSubcategories                                                           *
     ******************************************************************************/
    public static ObservableList<Subcategory> getSubcategories(int categoryId) {
        String sql = "SELECT * FROM subcategories WHERE category_id = ?;";
        ObservableList<Subcategory> lstSubcategories = FXCollections.observableArrayList();

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setInt(1, categoryId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                lstSubcategories.add(new Subcategory(id, name, categoryId));
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            Helpers.displayError("Subcategories Error", "An error occurred while retrieving the subcategories.");
            Helpers.log("ERROR - QueryManager.getSubcategories - " + e);
        }

        return lstSubcategories;
    }

    /******************************************************************************
     * getShifts                                                                  *
     ******************************************************************************/
    public static ObservableList<BasicIdName> getShifts() {
        String sql = "SELECT * FROM shifts;";
        ObservableList<BasicIdName> lstShifts = FXCollections.observableArrayList();

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                lstShifts.add(new BasicIdName(id, name));
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            Helpers.displayError("Shifts Error", "An error occurred while retrieving the shifts.");
            Helpers.log("ERROR - QueryManager.getShifts - " + e);
        }

        return lstShifts;
    }

    /******************************************************************************
     * getDepartments                                                             *
     ******************************************************************************/
    public static ObservableList<BasicIdName> getDepartments() {
        String sql = "SELECT * FROM departments;";
        ObservableList<BasicIdName> lstDepartments = FXCollections.observableArrayList();

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                lstDepartments.add(new BasicIdName(id, name));
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            Helpers.displayError("Departments Error", "An error occurred while retrieving the departments.");
            Helpers.log("ERROR - QueryManager.getDepartments - " + e);
        }

        return lstDepartments;
    }

    /******************************************************************************
     * getUsers                                                                   *
     ******************************************************************************/
    public static ObservableList<User> getUsers() {
        String sql = "SELECT u.id AS id, u.department_id AS department_id, u.shift_id AS shift_id, u.permission_level AS permission_level, u.username AS username, u.password_change AS password_change, " +
                "d.name AS department_name, s.name AS shift_name FROM users u LEFT JOIN departments d ON u.department_id = d.id LEFT JOIN shifts s ON u.shift_id = s.id;";
        ObservableList<User> lstUsers = FXCollections.observableArrayList();

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("id");
                BasicIdName department = new BasicIdName(rs.getInt("department_id"), rs.getString("department_name"));
                BasicIdName shift = new BasicIdName(rs.getInt("shift_id"), rs.getString("shift_name"));
                int permissionLevel = rs.getInt("permission_level");
                String username = rs.getString("username");
                boolean passwordChange = rs.getBoolean("password_change");
                lstUsers.add(new User(id, department, shift, permissionLevel, username, passwordChange));
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            Helpers.displayError("Users Error", "An error occurred while retrieving the users.");
            Helpers.log("ERROR - QueryManager.getUsers - " + e);
        }

        return lstUsers;
    }

    /******************************************************************************
     * createTurnover                                                             *
     ******************************************************************************/
    public static void createTurnover(Turnover turnover, List<Issue> issues) {
        String turnoverSql = "INSERT INTO turnover (id, date, shift_id, department_id, notes) VALUES (NULL, ?, ?, ?, ?);";
        String issueSql = "INSERT INTO issues (id, turnover_id, start_time, end_time, category_id, subcategory_id, notes) VALUES (NULL, ?, ?, ?, ?, ?, ?);";
        String categorySql = "SELECT * FROM categories WHERE name = ?;";
        String subcategorySql = "SELECT * FROM subcategories WHERE name = ? AND category_id = ?;";

        try {
            JDBC.connection.setAutoCommit(false);

            PreparedStatement ps = JDBC.connection.prepareStatement(turnoverSql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs;
            ps.setObject(1, turnover.getDate());
            ps.setInt(2, turnover.getShift().getId());
            ps.setInt(3, turnover.getDepartment().getId());
            ps.setString(4, turnover.getNotes());
            int id = 0;
            int rowsAffected = ps.executeUpdate();

            if(rowsAffected != 1) {
                JDBC.connection.rollback();
                throw new SQLException("Error while creating turnover.");
            }
            rs = ps.getGeneratedKeys();
            id = rs.getInt(1);
            rs.close();
            ps.close();

            for(Issue issue : issues) {
                int categoryId = 0;
                int subcategoryId = 0;

                ps = JDBC.connection.prepareStatement(categorySql);
                ps.setString(1, issue.getCategory().getName());
                rs = ps.executeQuery();
                if(!rs.next()) {
                    JDBC.connection.rollback();
                    throw new SQLException("Error fetching category information.");
                }
                categoryId = rs.getInt("id");
                rs.close();
                ps.close();

                ps = JDBC.connection.prepareStatement(subcategorySql);
                ps.setString(1, issue.getSubcategory().getName());
                ps.setInt(2, categoryId);
                rs = ps.executeQuery();
                if(!rs.next()) {
                    JDBC.connection.rollback();
                    throw new SQLException("Error fetching subcategory information.");
                }
                subcategoryId = rs.getInt("id");
                rs.close();
                ps.close();

                ps = JDBC.connection.prepareStatement(issueSql);
                ps.setInt(1, id);
                ps.setObject(2, issue.getStartTime());
                ps.setObject(3, issue.getEndTime());
                ps.setInt(4, categoryId);
                ps.setInt(5, subcategoryId);
                ps.setString(6, issue.getNotes());
                rowsAffected = ps.executeUpdate();
                if(rowsAffected != 1) {
                    JDBC.connection.rollback();
                    throw new SQLException("Error creating issue.");
                }
                rs.close();
                ps.close();
            }

            JDBC.connection.commit();
            JDBC.connection.setAutoCommit(true);
        } catch(SQLException e) {
            Helpers.displayError("Turnover Error", "An error occurred while creating the turnover.");
            Helpers.log("ERROR - QueryManager.createTurnover - " + e);
        }
    }

    /******************************************************************************
     * updateTurnover                                                             *
     ******************************************************************************/
    public static void updateTurnover(Turnover turnover, List<Issue> issues) {
        String turnoverSql = "UPDATE turnover SET date = ?, shift_id = ?, department_id = ?, notes = ? WHERE id = ?;";
        String delIssuesSql = "DELETE FROM issues WHERE turnover_id = ?;";
        String categorySql = "SELECT * FROM categories WHERE name = ?;";
        String subcategorySql = "SELECT * FROM subcategories WHERE name = ? AND category_id = ?;";
        String issueSql = "INSERT INTO issues (id, turnover_id, start_time, end_time, category_id, subcategory_id, notes) VALUES (NULL, ?, ?, ?, ?, ?, ?);";

        try {
            JDBC.connection.setAutoCommit(false);

            // Update the turnover
            PreparedStatement ps = JDBC.connection.prepareStatement(turnoverSql);
            ResultSet rs;
            ps.setObject(1, turnover.getDate());
            ps.setInt(2, turnover.getShift().getId());
            ps.setInt(3, turnover.getDepartment().getId());
            ps.setString(4, turnover.getNotes());
            ps.setInt(5, turnover.getId());

            int rowsAffected = ps.executeUpdate();

            if(rowsAffected != 1) {
                JDBC.connection.rollback();
                throw new SQLException("Error while updating turnover.");
            }
            ps.close();

            // Delete the issues
            ps = JDBC.connection.prepareStatement(delIssuesSql);
            ps.setInt(1, turnover.getId());
            ps.executeUpdate();
            ps.close();

            // Create the issues now that the old ones were deleted.
            for(Issue issue : issues) {
                int categoryId = 0;
                int subcategoryId = 0;

                ps = JDBC.connection.prepareStatement(categorySql);
                ps.setString(1, issue.getCategory().getName());
                rs = ps.executeQuery();
                if (!rs.next()) {
                    JDBC.connection.rollback();
                    throw new SQLException("Error fetching category information.");
                }
                categoryId = rs.getInt("id");
                rs.close();
                ps.close();


                ps = JDBC.connection.prepareStatement(subcategorySql);
                ps.setString(1, issue.getSubcategory().getName());
                ps.setInt(2, categoryId);
                rs = ps.executeQuery();
                if(!rs.next()) {
                    JDBC.connection.rollback();
                    throw new SQLException("Error fetching subcategory information.");
                }
                subcategoryId = rs.getInt("id");
                rs.close();
                ps.close();

                ps = JDBC.connection.prepareStatement(issueSql);
                ps.setInt(1, turnover.getId());
                ps.setObject(2, issue.getStartTime());
                ps.setObject(3, issue.getEndTime());
                ps.setInt(4, categoryId);
                ps.setInt(5, subcategoryId);
                ps.setString(6, issue.getNotes());
                rowsAffected = ps.executeUpdate();
                if(rowsAffected != 1) {
                    JDBC.connection.rollback();
                    throw new SQLException("Error creating issue.");
                }
                rs.close();
                ps.close();
            }

            JDBC.connection.commit();
            JDBC.connection.setAutoCommit(true);
        } catch(SQLException e) {
            Helpers.displayError("Turnover Error", "An error occurred while updating the turnover.");
            Helpers.log("ERROR - QueryManager.updateTurnover - " + e);
        }
    }


    /******************************************************************************
     * getIssuesCountForTurnoverId                                                *
     ******************************************************************************/
    public static int getIssuesCountForTurnoverId(int turnoverId) {
        String sql = "SELECT COUNT(*) AS cnt FROM issues WHERE turnover_id = ?;";
        int count = 0;

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setInt(1, turnoverId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                count = rs.getInt("cnt");
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            Helpers.displayGenericError();
            Helpers.log("ERROR - QueryManager.getIssuesCountForTurnoverid - " + e);
        }

        return count;
    }

    /******************************************************************************
     * getIssuesForTurnoverId                                                     *
     ******************************************************************************/
    public static ObservableList<Issue> getIssuesForTurnoverId(int turnoverId) {
        String sql = "SELECT issues.*, s.id AS sid, s.name as sname, c.id as cid, c.name as cname FROM issues INNER JOIN categories AS c ON issues.category_id = c.id INNER JOIN subcategories AS s ON issues.subcategory_id = s.id WHERE turnover_id = ?;";
        ObservableList<Issue> lstIssues = FXCollections.observableArrayList();

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setInt(1, turnoverId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("id");
                LocalTime start = LocalTime.parse(rs.getString("start_time"));
                LocalTime end = LocalTime.parse(rs.getString("end_time"));
                Category category = new Category(rs.getInt("cid"), rs.getString("cname"));
                Subcategory subcategory = new Subcategory(rs.getInt("sid"), rs.getString("sname"), category.getId());
                lstIssues.add(new Issue(id, start, end, category, subcategory));
            }
            rs.close();
            ps.close();
        } catch(SQLException e) {
            Helpers.displayError("Issues Error", "An error occurred while retrieving the issues.");
            Helpers.log("ERROR - QueryManager.getIssuesForTurnoverId - " + e);
        }

        return lstIssues;
    }

    /******************************************************************************
     * getPermissionLevel                                                         *
     ******************************************************************************/
    public static int getPermissionLevel(String username, String password) {
        String sql = "SELECT salt FROM users WHERE username = ?;";
        int level = 0;

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            String hashedPassword = "";
            if (rs.next()) {
                String salt = rs.getString("salt");

                // Build the password with the salt.
                hashedPassword = password + salt;

                // Convert to hash with SHA-256.
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(hashedPassword.getBytes(StandardCharsets.UTF_8));

                StringBuilder hexString = new StringBuilder();
                for(int i = 0; i < digest.length; i++) {
                    String hex = Integer.toHexString(0xff & digest[i]);
                    if(hex.length() == 1)
                        hexString.append('0');
                    hexString.append(hex);
                }

                hashedPassword = hexString.toString();
            } else {
                return 0;
            }
            ps.close();
            rs.close();

            // Check for user using the hashed password.
            sql = "SELECT permission_level FROM users WHERE username = ? and hash = ?;";
            ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, hashedPassword);
            rs = ps.executeQuery();
            if(rs.next()) {
                level = rs.getInt("permission_level");
                rs.close();
                ps.close();
            }
        } catch(SQLException e) {
            Helpers.displayGenericError();
            Helpers.log("ERROR - QueryManager.getPermissionLevel - " + e);
        } catch(NoSuchAlgorithmException e) {
            Helpers.log("ERROR - QueryManager.getPermissionLevel - Cannot find SHA256 algorithm - " + e);
        }

        return level;
    }

    /******************************************************************************
     * checkUsernameAvailable                                                     *
     ******************************************************************************/
    public static boolean checkUsernameAvailable(String username) {
        String sql = "SELECT COUNT(*) AS cnt FROM users WHERE username = ?;";
        int count = 0;

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                count = rs.getInt("cnt");
            }
            rs.close();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Username Error", "An error occurred while checking username availability.");
            Helpers.log("ERROR - QueryManager.checkUsernameAvailable - " + e);
        }

        return count == 0;
    }

    /******************************************************************************
     * checkCategoryNameAvailable                                                 *
     ******************************************************************************/
    public static boolean checkCategoryNameAvailable(String name) {
        String sql = "SELECT COUNT(*) AS cnt FROM categories WHERE name = ?;";
        int count = 0;

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                count = rs.getInt("cnt");
            }
            rs.close();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Category Error", "An error occurred while checking category name availability.");
            Helpers.log("ERROR - QueryManager.checkCategoryNameAvailable - " + e);
        }

        return count == 0;
    }

    /******************************************************************************
     * checkSubcategoryNameAvailable                                              *
     ******************************************************************************/
    public static boolean checkSubcategoryNameAvailable(String name, int categoryId) {
        String sql = "SELECT COUNT(*) AS cnt FROM subcategories WHERE name = ? AND category_id = ?;";
        int count = 0;

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, name);
            ps.setInt(2, categoryId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                count = rs.getInt("cnt");
            }
            rs.close();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Subcategory Error", "An error occurred while checking subcategory name availability.");
            Helpers.log("ERROR - QueryManager.checkSubcategoryNameAvailable - " + e);
        }

        return count == 0;
    }

    /******************************************************************************
     * checkDepartmentNameAvailable                                               *
     ******************************************************************************/
    public static boolean checkDepartmentNameAvailable(String name) {
        String sql = "SELECT COUNT(*) AS cnt FROM departments WHERE name = ?;";
        int count = 0;

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                count = rs.getInt("cnt");
            }
            rs.close();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Department Error", "An error occurred while checking department name availability.");
            Helpers.log("ERROR - QueryManager.checkDepartmentNameAvailable - " + e);
        }

        return count == 0;
    }

    /******************************************************************************
     * checkShiftNameAvailable                                                    *
     ******************************************************************************/
    public static boolean checkShiftNameAvailable(String name) {
        String sql = "SELECT COUNT(*) AS cnt FROM shifts WHERE name = ?;";
        int count = 0;

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) {
                count = rs.getInt("cnt");
            }
            rs.close();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Shift Error", "An error occurred while checking shift name availability.");
            Helpers.log("ERROR - QueryManager.checkShiftNameAvailable - " + e);
        }

        return count == 0;
    }

    /******************************************************************************
     * createCategory                                                             *
     ******************************************************************************/
    public static void createCategory(String name) {
        String sql = "INSERT INTO categories(name) VALUES (?);";
        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, name);
            ps.executeUpdate();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Category Error", "An error occurred while creating the category.");
            Helpers.log("ERROR - QueryManager.createCategory - " + e);
        }
    }

    /******************************************************************************
     * createSubcategory                                                          *
     ******************************************************************************/
    public static void createSubcategory(String name, int categoryId) {
        String sql = "INSERT INTO subcategories(name, category_id) VALUES (?, ?);";
        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, name);
            ps.setInt(2, categoryId);
            ps.executeUpdate();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Subcategory Error", "An error occurred while creating the subcategory.");
            Helpers.log("ERROR - QueryManager.createSubcategory - " + e);
        }
    }

    /******************************************************************************
     * createDepartment                                                            *
     ******************************************************************************/
    public static void createDepartment(String name) {
        String sql = "INSERT INTO departments(name) VALUES (?);";
        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, name);
            ps.executeUpdate();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Department Error", "An error occurred while creating the department.");
            Helpers.log("ERROR - QueryManager.createDepartment - " + e);
        }
    }

    /******************************************************************************
     * createShift                                                                *
     ******************************************************************************/
    public static void createShift(String name) {
        String sql = "INSERT INTO shifts(name) VALUES (?);";
        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, name);
            ps.executeUpdate();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Shift Error", "An error occurred while creating the shift.");
            Helpers.log("ERROR - QueryManager.createShift - " + e);
        }
    }

    /******************************************************************************
     * updateCategory                                                             *
     ******************************************************************************/
    public static void updateCategory(BasicIdName category) {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";
        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, category.getName());
            ps.setInt(2, category.getId());
            ps.executeUpdate();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Category Error", "An error occurred while updating the category.");
            Helpers.log("ERROR - QueryManager.updateCategory - " + e);
        }
    }

    /******************************************************************************
     * updateSubcategory                                                          *
     ******************************************************************************/
    public static void updateSubcategory(BasicIdName subcategory, int categoryId) {
        String sql = "UPDATE subcategories SET name = ? WHERE id = ? AND category_id = ?";
        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, subcategory.getName());
            ps.setInt(2, subcategory.getId());
            ps.setInt(3, categoryId);
            ps.executeUpdate();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Subcategory Error", "An error occurred while updating the subcategory.");
            Helpers.log("ERROR - QueryManager.updateSubcategory - " + e);
        }
    }

    /******************************************************************************
     * updateDepartment                                                           *
     ******************************************************************************/
    public static void updateDepartment(BasicIdName department) {
        String sql = "UPDATE departments SET name = ? WHERE id = ?";
        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, department.getName());
            ps.setInt(2, department.getId());
            ps.executeUpdate();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Department Error", "An error occurred while updating the department.");
            Helpers.log("ERROR - QueryManager.updateDepartment - " + e);
        }
    }

    /******************************************************************************
     * updateShift                                                                *
     ******************************************************************************/
    public static void updateShift(BasicIdName shift) {
        String sql = "UPDATE shifts SET name = ? WHERE id = ?";
        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setString(1, shift.getName());
            ps.setInt(2, shift.getId());
            ps.executeUpdate();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Shift Error", "An error occurred while updating the shift.");
            Helpers.log("ERROR - QueryManager.updateShift - " + e);
        }
    }

    /******************************************************************************
     * deleteCategory                                                             *
     ******************************************************************************/
    public static void deleteCategory(int id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Category Error", "An error occurred while deleting the category.");
            Helpers.log("ERROR - QueryManager.deleteCategory - " + e);
        }
    }

    /******************************************************************************
     * deleteSubcategory                                                          *
     ******************************************************************************/
    public static void deleteSubcategory(int id) {
        String sql = "DELETE FROM subcategories WHERE id = ?";
        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Subcategory Error", "An error occurred while deleting the subcategory.");
            Helpers.log("ERROR - QueryManager.deleteSubcategory - " + e);
        }
    }

    /******************************************************************************
     * deleteDepartment                                                           *
     ******************************************************************************/
    public static void deleteDepartment(int id) {
        String sql = "DELETE FROM departments WHERE id = ?";
        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Department Error", "An error occurred while deleting the department.");
            Helpers.log("ERROR - QueryManager.deleteDepartment - " + e);
        }
    }

    /******************************************************************************
     * deleteShift                                                                *
     ******************************************************************************/
    public static void deleteShift(int id) {
        String sql = "DELETE FROM shifts WHERE id = ?";
        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();

        } catch(SQLException e) {
            Helpers.displayError("Shift Error", "An error occurred while deleting the shift.");
            Helpers.log("ERROR - QueryManager.deleteShift - " + e);
        }
    }

    /******************************************************************************
     * createUser                                                                 *
     ******************************************************************************/
    public static void createUser(
            int departmentId,
            int shiftId,
            int permissionId,
            String username,
            String password,
            boolean passwordChangeRequired
    ) {
        String sql = "INSERT INTO users ("+
            "department_id, " +
            "shift_id, " +
            "permission_level, " +
            "username, " +
            "hash, " +
            "salt, " +
            "password_change" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?);";
        try {
            String hashedPassword = "";
            String salt = "";
            int charLowerLimit = 48; // 0
            int charUpperLimit = 122; // z
            int saltLength = 10;
            Random random = new Random();

            // Generate random salt.
            salt = random.ints(charLowerLimit, charUpperLimit)
                    .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                    .limit(saltLength)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();

            // Build the password with the salt.
            hashedPassword = password + salt;
            // Convert to hash with SHA-256.
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(hashedPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for(byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1)
                    hexString.append('0');
                hexString.append(hex);
            }
            hashedPassword = hexString.toString();

            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setInt(1, departmentId);
            ps.setInt(2, shiftId);
            ps.setInt(3, permissionId);
            ps.setString(4, username);
            ps.setString(5, hashedPassword);
            ps.setString(6, salt);
            ps.setBoolean(7, passwordChangeRequired);

            ps.executeUpdate();
            ps.close();
        } catch(SQLException e) {
            Helpers.displayError("User Error", "An error occurred while creating the user.");
            Helpers.log("ERROR - QueryManager.createUser - " + e);
        } catch(NoSuchAlgorithmException e) {
            Helpers.log("ERROR - QueryManager.createUser - Cannot find SHA256 algorithm - " + e);
        }
    }

    /******************************************************************************
     * updateUser                                                                 *
     ******************************************************************************/
    public static void updateUser(
            int userId,
            int departmentId,
            int shiftId,
            int permissionId,
            String password,
            boolean passwordChangeRequired
    ) {
        String sql;
        if (password.isEmpty()) {
            sql = "UPDATE users SET " +
                    "department_id = ?," +
                    "shift_id = ?," +
                    "permission_level = ?," +
                    "password_change = ?" +
                    "WHERE id = ?;";
        } else {
            sql = "UPDATE users SET " +
                    "department_id = ?," +
                    "shift_id = ?," +
                    "permission_level = ?," +
                    "password_change = ?," +
                    "hash = ?," +
                    "salt = ?" +
                    "WHERE id = ?;";
        }

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setInt(1, departmentId);
            ps.setInt(2, shiftId);
            ps.setInt(3, permissionId);
            ps.setBoolean(4, passwordChangeRequired);

            if (password.isEmpty()) {
                ps.setInt(5, userId);
            } else {
                String hashedPassword = "";
                String salt = "";
                int charLowerLimit = 48; // 0
                int charUpperLimit = 122; // z
                int saltLength = 10;
                Random random = new Random();

                // Generate random salt.
                salt = random.ints(charLowerLimit, charUpperLimit)
                        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                        .limit(saltLength)
                        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                        .toString();

                // Build the password with the salt.
                hashedPassword = password + salt;
                // Convert to hash with SHA-256.
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(hashedPassword.getBytes(StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : digest) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1)
                        hexString.append('0');
                    hexString.append(hex);
                }
                hashedPassword = hexString.toString();

                ps.setString(5, hashedPassword);
                ps.setString(6, salt);
                ps.setInt(7, userId);
            }

            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Helpers.displayError("User Error", "An error occurred while updating the user.");
            Helpers.log("ERROR - QueryManager.updateUser - " + e);
        } catch(NoSuchAlgorithmException e) {
            Helpers.log("ERROR - QueryManager.updateUser - Cannot find SHA256 algorithm - " + e);
        }
    }

    /******************************************************************************
     * deleteUser                                                                 *
     ******************************************************************************/
    public static void deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Helpers.displayError("User Error", "An error occurred while deleting the user.");
            Helpers.log("ERROR - QueryManager.deleteUser - " + e);
        }

    }

    /******************************************************************************
     * deleteTurnover                                                             *
     ******************************************************************************/
    public static boolean deleteTurnover(int turnoverId) {
        String sql = "DELETE FROM turnover WHERE id = ?";
        boolean didDelete = false;

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ps.setInt(1, turnoverId);
            didDelete = ps.executeUpdate() > 0;
            ps.close();
        } catch (SQLException e) {
            Helpers.displayError("Turnover Error", "An error occurred while deleting the turnover.");
            Helpers.log("ERROR - QueryManager.deleteTurnover - " + e);
        }

        return didDelete;
    }

    /******************************************************************************
     * generateShiftReports                                                        *
     ******************************************************************************/
    public static ObservableList<Report> generateShiftReports() {
//        String sql = "SELECT t.id, t.date, t.shift_id, s.id AS shift_id, s.name AS shift_name, COUNT(i.id) AS cnt " +
//        "FROM turnover t " +
//        "INNER JOIN issues i ON t.id=i.turnover_id " +
//        "INNER JOIN shifts s ON t.shift_id = s.id " +
//        "GROUP BY t.shift_id";
        String sql = "SELECT id, date, shift_id, shift_name, SUM(total) as cnt FROM (" +
        "    SELECT t.id," +
        "    t.date," +
        "    s.id   AS shift_id," +
        "    s.name AS shift_name," +
        "    (" +
        "        SELECT COUNT(*)" +
        "        FROM issues i" +
        "        WHERE i.turnover_id = t.id" +
        "    ) AS total" +
        "    FROM turnover t" +
        "    INNER JOIN shifts s ON t.shift_id = s.id" +
        ") GROUP BY date, shift_id;";

        ObservableList<Report> reports = FXCollections.observableArrayList();

        try {
            PreparedStatement ps = JDBC.connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                LocalDate date = LocalDate.parse(rs.getString("date"));
                BasicIdName shift = new BasicIdName(rs.getInt("shift_id"), rs.getString("shift_name"));
                int issues = rs.getInt("cnt");
                Report report = new Report(date, shift, issues);
                reports.add(report);
            }
        } catch (SQLException e) {
            Helpers.displayError("Report Error", "An error occurred while generating the report.");
            Helpers.log("ERROR - QueryManager.generateShiftReports - " + e);
        }

        return reports;
    }
}

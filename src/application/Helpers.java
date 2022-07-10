package application;

import javafx.scene.control.Alert;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Helpers {

    /******************************************************************************
     * Check if time overlaps.                                                    *
     ******************************************************************************/
    public static boolean doesTimeOverlap(LocalTime start, LocalTime end, LocalTime otherStart, LocalTime otherEnd) {
        // Check time overlap
        return ((start.isAfter(otherStart)) && (start.isBefore(otherEnd))) ||
               ((end.isAfter(otherStart)) && (end.isBefore(otherEnd))) ||
               (
                       (start.isBefore(otherStart) || start.equals(otherStart)) &&
                       (end.isAfter(otherEnd) || end.equals(otherEnd)) &&
                       (start.isBefore(otherEnd) || start.equals(otherEnd))
               );
    }

    private static void logWrite(String text) {
        try {
            FileWriter fw = new FileWriter("log.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(text);
            bw.newLine();
            bw.close();
        } catch(IOException e) {
            // Fail silently.
        }
    }

    public static void log(String text) {
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("[MM/dd/yyyy] [HH:mm:ss]");
        ZonedDateTime dt = ZonedDateTime.of(LocalDate.now(), LocalTime.now(), ZoneId.of("America/New_York"));
        logWrite(String.format("%s %s", dt.format(dtFormatter), text));
    }

    public static void logWithUser(String text, String name) {
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("[MM/dd/yyyy] [HH:mm:ss]");
        ZonedDateTime dt = ZonedDateTime.of(LocalDate.now(), LocalTime.now(), ZoneId.of("America/New_York"));
        logWrite(String.format("%s [%s] %s", dt.format(dtFormatter), name, text));
    }

    public static void displayError(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(text);
        alert.showAndWait();
    }

    public static void displayGenericError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText("Please contact an administrator if this error occurs again.");
        alert.showAndWait();
    }
}

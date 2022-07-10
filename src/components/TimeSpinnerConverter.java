package components;

import application.Helpers;
import javafx.scene.control.Alert;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

class TimeSpinnerConverter extends StringConverter<LocalTime> {
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String toString(LocalTime localTime) {
        if(localTime == null) {
            return "";
        }

        try {
            return localTime.format(dtf);
        } catch(DateTimeParseException e) {
            Helpers.displayError("Time Error", "Please enter a valid time format of HH:mm");
            return "";
        }
    }

    @Override
    public LocalTime fromString(String s) {
        if(s == null) {
            return null;
        }

        s = s.trim();

        if(s.length() < 1) {
            return null;
        }

        try {
            return LocalTime.parse(s, dtf);
        } catch(DateTimeParseException e) {
            return null;
        }

    }
}

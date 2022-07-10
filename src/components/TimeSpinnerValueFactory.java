package components;

import javafx.scene.control.Alert;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TimeSpinnerValueFactory extends SpinnerValueFactory<LocalTime> {
    {
        setValue(LocalTime.now());
        setConverter(new TimeSpinnerConverter());
    }

    @Override
    public void decrement(int i) {
        if(getValue() == null) {
            setValue(LocalTime.now());
        } else {
            LocalTime time = (LocalTime) getValue();
            setValue(time.minusMinutes(i));
        }
    }

    @Override
    public void increment(int i) {
        if(this.getValue() == null) {
            setValue(LocalTime.now());
        } else {
            LocalTime time = (LocalTime) getValue();
            setValue(time.plusMinutes(i));
        }
    }
}
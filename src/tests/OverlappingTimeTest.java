package tests;

import application.Helpers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Ensure overlapping times are detected, target = 4:00 - 8:00")
public class OverlappingTimeTest {

    private final static LocalTime START = LocalTime.of(4, 0);
    private final static LocalTime END = LocalTime.of(8, 0);

    @Test
    @DisplayName("6:00 - 10:00 (start inside, end outside)")
    public void startInsideEndOutside() {
        LocalTime start = LocalTime.of(6, 30);
        LocalTime end = LocalTime.of(5, 30);

        assertTrue(Helpers.doesTimeOverlap(start, end, START, END));
    }

    @Test
    @DisplayName("1:00 - 6:00 (start outside, end inside)")
    public void startOutsideEndInside() {
        LocalTime start = LocalTime.of(1, 0);
        LocalTime end = LocalTime.of(6, 0);

        assertTrue(Helpers.doesTimeOverlap(start, end, START, END));
    }

    @Test
    @DisplayName("6:00 - 7:00 (start inside, end inside)")
    public void startInsideEndInside() {
        LocalTime start = LocalTime.of(6, 0);
        LocalTime end = LocalTime.of(7, 0);

        assertTrue(Helpers.doesTimeOverlap(start, end, START, END));
    }

    @Test
    @DisplayName("1:00 - 10:00 (start outside, end outside)")
    public void startOutsideEndOutside() {
        LocalTime start = LocalTime.of(1, 0);
        LocalTime end = LocalTime.of(10, 0);

        assertTrue(Helpers.doesTimeOverlap(start, end, START, END));
    }

    @Test
    @DisplayName("10:00 - 1:00 (start outside, end outside, after)")
    public void startOutsideEndOutsideAfter() {
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(1, 0);

        assertFalse(Helpers.doesTimeOverlap(start, end, START, END));
    }
}

package ru.practicum.shareit.tools.matchers;

import org.mockito.ArgumentMatcher;
import java.time.LocalDateTime;

public class DateMatcher implements ArgumentMatcher<LocalDateTime> {
    private final LocalDateTime date;

    private DateMatcher(LocalDateTime date) {
        this.date = date;
    }

    public static DateMatcher near(LocalDateTime date) {
        return new DateMatcher(date);
    }

    @Override
    public boolean matches(LocalDateTime argument) {
        return argument != null && date != null
                && !argument.isBefore(date.minusSeconds(2)) && !argument.isAfter(date.plusSeconds(2));
    }
}

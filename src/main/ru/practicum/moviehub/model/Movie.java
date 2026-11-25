package ru.practicum.moviehub.model;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.practicum.moviehub.Constants.*;

public class Movie {
    private static final AtomicInteger nextId = new AtomicInteger(0);


    private final Integer id;
    private final String title;
    private final Integer year;

    public Movie(String title, Integer year) {

        validateTitle(title);
        validateYear(year);

        this.id = nextId.getAndIncrement();
        this.title = title;
        this.year = year;

    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getYear() {
        return year;
    }

    public static void resetNextId() {
        nextId.set(0);
    }


    private void validateYear(Integer year) {

        var maxYear = LocalDate.now().getYear() + 1;

        if (year < MIN_YEAR_MOVIE || year > maxYear) {
            throw new IllegalArgumentException(ERR_YEAR_RANGE + maxYear);
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(ERR_TITLE_EMPTY);
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(ERR_TITLE_TOO_LONG);
        }
    }
}

package ru.practicum.moviehub;

import java.time.LocalDateTime;

import static ru.practicum.moviehub.Constants.DATE_TIME_FORMATTER;

public class Logger {

    private static final int DEBUG = 0;
    private static final int INFO = 1;
    private static final int WARN = 2;
    private static final int ERROR = 3;

    private final int logLevel;


    public Logger(String logLevel) {

        this.logLevel = switch (logLevel) {
            case "DEBUG" -> DEBUG;
            case "INFO" -> INFO;
            case "WARN" -> WARN;
            case "ERROR" -> ERROR;
            default -> {
                System.out.print("Несуществующий уровень логирования в настройках. Используется уровень INFO.\n\n");
                yield INFO;
            }
        };
    }

    public void debug(String message) {
        if (logLevel > DEBUG) {
            return;
        }
        log("DEBUG", message);
    }

    public void info(String message) {
        if (logLevel > INFO) {
            return;
        }
        log("INFO", message);
    }

    public void warn(String message) {
        if (logLevel > WARN) {
            return;
        }
        log("WARN", message);
    }

    public void error(String message) {
        if (logLevel > ERROR) {
            return;
        }
        log("ERROR", message);
    }

    private void log(String level, String message) {

        String timestamp = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        System.out.printf("%s %s %s\n", timestamp, level, message);

    }

}

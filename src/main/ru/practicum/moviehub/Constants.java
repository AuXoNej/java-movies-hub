package ru.practicum.moviehub;

import java.time.format.DateTimeFormatter;

public class Constants {

    // Server
    public static final Integer PORT = 8080;

    public static final String API_PATH_MOVIES = "movies";
    public static final String PATH_MOVIES = "/" + API_PATH_MOVIES;

    // HTTP methods
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_DELETE = "DELETE";

    // Content-Type
    public static final String CT_JSON = "application/json; charset=UTF-8";
    public static final String CT_APPLICATION_JSON = "application/json";

    public static final int STATUS_OK = 200;
    public static final int STATUS_CREATED = 201;
    public static final int STATUS_NO_CONTENT = 204;
    public static final int STATUS_BAD_REQUEST = 400;
    public static final int STATUS_NOT_FOUND = 404;
    public static final int STATUS_METHOD_NOT_ALLOWED = 405;
    public static final int STATUS_UNSUPPORTED_MEDIA_TYPE = 415;
    public static final int STATUS_UNPROCESSABLE_ENTITY = 422;
    public static final int STATUS_INTERNAL_SERVER_ERROR = 500;

    public static final String QUERY_SEPARATOR = "&";
    public static final String KV_SEPARATOR = "=";

    public static final String FIELD_TITLE = "title";
    public static final String FIELD_YEAR = "year";

    public static final Integer MIN_YEAR_MOVIE = 1888;
    public static final int MAX_TITLE_LENGTH = 100;

    public static final String ERR_MOVIE_NOT_FOUND = "Фильм не найден";
    public static final String ERR_INCORRECT_ID = "Некорректный ID";
    public static final String ERR_VALIDATION = "Ошибка валидации";
    public static final String ERR_INCORRECT_YEAR_PARAM = "Некорректный параметр запроса — 'year'";
    public static final String ERR_BODY_MUST_BE_JSON_OBJECT = "Запрос должен быть JSON-объектом";
    public static final String ERR_MISSING_TITLE_YEAR = "Запрос должен содержать поля title и year";
    public static final String ERR_TITLE_EMPTY = "Название не должно быть пустым";
    public static final String ERR_TITLE_TOO_LONG = "Название не должно быть длиннее 100 символов";
    public static final String ERR_YEAR_RANGE = "Год должен быть между 1888 и ";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String LOG_LEVEL = "INFO";
}

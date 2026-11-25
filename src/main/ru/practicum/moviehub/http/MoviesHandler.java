package ru.practicum.moviehub.http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.Logger;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;


import static ru.practicum.moviehub.Constants.*;

public class MoviesHandler extends BaseHttpHandler {

    private final MoviesStore store;

    private static final Gson gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    private final Logger logger = new Logger(LOG_LEVEL);

    public MoviesHandler(MoviesStore store) {
        this.store = store;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {

        try {
            var method = ex.getRequestMethod();
            var path = ex.getRequestURI().getPath();
            var pathParts = path.split("/");

            if (!pathParts[1].equals(API_PATH_MOVIES)) {
                notFoundMethod(ex);
                return;
            }

            switch (method.toUpperCase()) {
                case METHOD_GET -> {
                    if (pathParts.length == 2) {

                        var queryParameters = parseQuery(ex.getRequestURI().getQuery());

                        if (queryParameters.containsKey(FIELD_YEAR)) {
                            getMoviesByYear(ex, queryParameters.get(FIELD_YEAR));
                        } else {
                            getMovies(ex);
                        }
                    } else if (pathParts.length == 3) {
                        getMovieById(ex, pathParts[2]);
                    } else {
                        notFoundMethod(ex);
                    }
                }
                case METHOD_POST -> {
                    if (pathParts.length == 2) {
                        addMovie(ex);
                    } else {
                        notFoundMethod(ex);
                    }
                }
                case METHOD_DELETE -> {
                    if (pathParts.length == 3) {
                        removeMovieById(ex, pathParts[2]);
                    } else {
                        notFoundMethod(ex);
                    }
                }
                default -> incorrectMethod(ex);
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            super.sendJson(ex, STATUS_INTERNAL_SERVER_ERROR, "");
        }
    }

    private void getMovies(HttpExchange ex) throws IOException {
        logger.info("Пришел запрос на получение списка фильмов");

        String moviesArrayJson = gson.toJson(store.getMovies());
        super.sendJson(ex, STATUS_OK, moviesArrayJson);

        logger.info("Запрос на получение списка фильмов выполнен успешно");
    }

    private void getMovieById(HttpExchange ex, String id) throws IOException {
        try {
            logger.info("Пришел запрос на получение фильма по id = " + id);

            var movieId = Integer.parseInt(id);

            var movie = store.getMovieById(movieId);
            if (movie.isEmpty()) {
                logger.warn("Фильм не найден");
                super.sendJson(ex, STATUS_NOT_FOUND,
                    gson.toJson(new ErrorResponse(ERR_MOVIE_NOT_FOUND, new ArrayList<>()))
                );
                return;
            }

            String movieJson = gson.toJson(movie.get());
            super.sendJson(ex, STATUS_OK, movieJson);
            logger.info("Запрос на получение фильма выполнен успешно");

        } catch (NumberFormatException e) {
            logger.warn("Некорректный ID");
            super.sendJson(ex, STATUS_BAD_REQUEST,
                gson.toJson(new ErrorResponse(ERR_INCORRECT_ID, new ArrayList<>()))
            );
        }
    }

    private void addMovie(HttpExchange ex) throws IOException {
        logger.info("Пришел запрос на добавление фильма");

        if (!isValidContentType(ex)) {
            logger.warn("Неверный Content-Type");
            super.sendJson(ex, STATUS_UNSUPPORTED_MEDIA_TYPE, "");
        } else {
            try {
                var jsonObject = validateAndGetRequestJson(ex);

                var title = jsonObject.get(FIELD_TITLE).getAsString();
                var year = jsonObject.get(FIELD_YEAR).getAsInt();

                super.sendJson(ex, STATUS_CREATED,
                    gson.toJson(store.addMovie(title, year)));

            } catch (IllegalArgumentException e) {
                logger.warn("Некорректный запрос: " + e.getMessage());
                super.sendJson(
                    ex, STATUS_UNPROCESSABLE_ENTITY,
                    gson.toJson(new ErrorResponse(ERR_VALIDATION, List.of(e.getMessage())))
                );
            }
        }
        logger.info("Запрос на добавление фильма выполнен");
    }

    private void getMoviesByYear(HttpExchange ex, String year) throws IOException {

        try {
            logger.info("Пришел запрос на получение списка фильмов по году");
            String moviesArrayJson = gson.toJson(store.getMoviesByYear(Integer.parseInt(year)));
            super.sendJson(ex, STATUS_OK, moviesArrayJson);

            logger.info("Запрос на получение списка фильмов по году выполнен");
        } catch (NumberFormatException e) {
            logger.warn("Некорректный параметр запроса — 'year'");
            super.sendJson(ex, STATUS_BAD_REQUEST,
                gson.toJson(new ErrorResponse(ERR_INCORRECT_YEAR_PARAM,
                    new ArrayList<>()))
            );
        }
    }

    private void removeMovieById(HttpExchange ex, String id) throws IOException {
        try {
            logger.info("Пришел запрос на удаление фильма по id = " + id);

            var movieId = Integer.parseInt(id);

            store.removeMovieById(movieId);
            logger.info("Запрос на удаление фильма выполнен");

            super.sendNoContent(ex);
        } catch (NumberFormatException e) {
            logger.warn("Некорректный ID");
            super.sendJson(ex, STATUS_BAD_REQUEST,
                gson.toJson(new ErrorResponse(ERR_INCORRECT_ID, new ArrayList<>()))
            );
        } catch (NoSuchElementException e) {
            logger.warn("Нет фильма с таким ID");
            super.sendJson(ex, STATUS_NOT_FOUND, "");
        }
    }

    private void incorrectMethod(HttpExchange ex) throws IOException {
        logger.warn("Некорректный запрос");
        super.sendJson(ex, STATUS_METHOD_NOT_ALLOWED, "");
    }

    private void notFoundMethod(HttpExchange ex) throws IOException {
        logger.warn("Путь не существует");
        super.sendJson(ex, STATUS_NOT_FOUND, "");
    }

    private boolean isValidContentType(HttpExchange ex) {

        if (ex.getRequestHeaders().get("Content-Type") == null) {
            return false;
        }
        String contentTypeHeaderValue =
            ex.getRequestHeaders().get("Content-Type").getFirst();
        return CT_APPLICATION_JSON.equals(contentTypeHeaderValue);

    }

    private JsonObject validateAndGetRequestJson(HttpExchange ex) throws IOException {

        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JsonElement jsonElement = JsonParser.parseString(body);

        if (!jsonElement.isJsonObject()) {
            throw new IllegalArgumentException(ERR_BODY_MUST_BE_JSON_OBJECT);
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (!jsonObject.has(FIELD_TITLE) || !jsonObject.has(FIELD_YEAR)) {
            throw new IllegalArgumentException(ERR_MISSING_TITLE_YEAR);
        }

        return jsonObject;
    }


    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null) return map;

        for (String s : query.split(QUERY_SEPARATOR)) {
            if (s.isEmpty()) continue;
            String[] kv = s.split(KV_SEPARATOR, 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}

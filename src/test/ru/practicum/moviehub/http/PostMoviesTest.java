package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.Constants;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.moviehub.Constants.METHOD_POST;
import static ru.practicum.moviehub.Constants.PATH_MOVIES;

public class PostMoviesTest {

    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore store;
    private static Gson gson;

    @BeforeAll
    static void beforeAll() {
        store = new MoviesStore();
        server = new MoviesServer(store, Constants.PORT);
        client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();
        gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

        server.start();
    }

    @BeforeEach
    void beforeEach() {
        Movie.resetNextId();
        store.clear();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    // Добавляет фильм при корректных данных
    @Test
    void addMovie_success() throws Exception {

        var title = "The Matrix";
        var year = 1999;
        var requestBody = "{\"title\": \"" + title + "\", \"year\": " + year + "}";
        var headers = new String[] {"Content-Type", Constants.CT_APPLICATION_JSON};

        var response = MoviesApiTestUtils.postRequest(
            PATH_MOVIES, client, HttpRequest.BodyPublishers.ofString(requestBody),headers
        );

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_CREATED, METHOD_POST + PATH_MOVIES);

        String responseBody = response.body().trim();
        Movie responseMovie = gson.fromJson(responseBody, Movie.class);

        var movies = store.getMovies();

        assertEquals(1, movies.size());
        assertEquals(responseMovie.getId(), movies.getFirst().getId());
        assertEquals(title, responseMovie.getTitle());
        assertEquals(year, responseMovie.getYear());

    }

    // Возвращает ошибку при пустом title
    @Test
    void addMovie_fail_NotTitle() throws Exception {

        var title = "";
        var year = 1999;
        var requestBody = "{\"title\": \"" + title + "\", \"year\": " + year + "}";
        var headers = new String[] {"Content-Type", Constants.CT_APPLICATION_JSON};

        var response = MoviesApiTestUtils.postRequest(
            PATH_MOVIES, client, HttpRequest.BodyPublishers.ofString(requestBody),headers
        );

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_UNPROCESSABLE_ENTITY, METHOD_POST + PATH_MOVIES);

        String responseBody = response.body().trim();
        ErrorResponse responseMovie = gson.fromJson(responseBody, ErrorResponse.class);

        var movies = store.getMovies();

        assertEquals(0, movies.size());
        assertEquals(Constants.ERR_VALIDATION, responseMovie.getError());
        assertEquals(Constants.ERR_TITLE_EMPTY, responseMovie.getDetails().getFirst());

    }

    // Возвращает ошибку при слишком длинном title (> 100 символов)
    @Test
    void addMovie_fail_lenTitleMore100() throws Exception {

        var title = "a".repeat(101);
        var year = 1999;
        var requestBody = "{\"title\": \"" + title + "\", \"year\": " + year + "}";
        var headers = new String[] {"Content-Type", Constants.CT_APPLICATION_JSON};

        var response = MoviesApiTestUtils.postRequest(
            PATH_MOVIES, client, HttpRequest.BodyPublishers.ofString(requestBody),headers
        );

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_UNPROCESSABLE_ENTITY, METHOD_POST + PATH_MOVIES);

        String responseBody = response.body().trim();
        ErrorResponse responseMovie = gson.fromJson(responseBody, ErrorResponse.class);

        var movies = store.getMovies();

        assertEquals(0, movies.size());
        assertEquals(Constants.ERR_VALIDATION, responseMovie.getError());
        assertEquals(Constants.ERR_TITLE_TOO_LONG, responseMovie.getDetails().getFirst());

    }

    // Возвращает ошибку при неверном year (меньше 1888 или больше текущего года + 1)
    @Test
    void addMovie_fail_wrongYear() throws Exception {

        var title = "The Matrix";
        var year = 1000;
        var requestBody = "{\"title\": \"" + title + "\", \"year\": " + year + "}";
        var headers = new String[] {"Content-Type", Constants.CT_APPLICATION_JSON};

        var response = MoviesApiTestUtils.postRequest(
            PATH_MOVIES, client, HttpRequest.BodyPublishers.ofString(requestBody),headers
        );

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_UNPROCESSABLE_ENTITY, METHOD_POST + PATH_MOVIES);

        String responseBody = response.body().trim();
        ErrorResponse responseMovie = gson.fromJson(responseBody, ErrorResponse.class);

        var movies = store.getMovies();

        assertEquals(0, movies.size());
        assertEquals(Constants.ERR_VALIDATION, responseMovie.getError());
        assertEquals(Constants.ERR_YEAR_RANGE + (LocalDate.now().getYear() + 1),
            responseMovie.getDetails().getFirst());

    }

    // Возвращает ошибку при неправильном Content-Type
    @Test
    void addMovie_fail_incorrectContentType() throws Exception {

        var title = "The Matrix";
        var year = 1999;
        var requestBody = "{\"title\": \"" + title + "\", \"year\": " + year + "}";
        var headers = new String[] {"Content-Type", "application/jsonsss"};

        var response = MoviesApiTestUtils.postRequest(
            PATH_MOVIES, client, HttpRequest.BodyPublishers.ofString(requestBody),headers
        );

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_UNSUPPORTED_MEDIA_TYPE, METHOD_POST + PATH_MOVIES);

        var movies = store.getMovies();

        assertEquals(0, movies.size());
    }

    // Возвращает ошибку при некорректном JSON.
    @Test
    void addMovie_fail_incorrectJson() throws Exception {

        var title = "The Matrix";
        var requestBody = "{\"title\": \"" + title + "\"}";
        var headers = new String[] {"Content-Type", Constants.CT_APPLICATION_JSON};

        var response = MoviesApiTestUtils.postRequest(
            PATH_MOVIES, client, HttpRequest.BodyPublishers.ofString(requestBody),headers
        );

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_UNPROCESSABLE_ENTITY, METHOD_POST + PATH_MOVIES);

        String responseBody = response.body().trim();
        ErrorResponse responseMovie = gson.fromJson(responseBody, ErrorResponse.class);

        var movies = store.getMovies();

        assertEquals(0, movies.size());
        assertEquals(Constants.ERR_VALIDATION, responseMovie.getError());
        assertEquals(Constants.ERR_MISSING_TITLE_YEAR,
            responseMovie.getDetails().getFirst());
    }
}

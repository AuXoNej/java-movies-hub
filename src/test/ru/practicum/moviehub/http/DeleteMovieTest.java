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
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.moviehub.Constants.METHOD_DELETE;
import static ru.practicum.moviehub.Constants.PATH_MOVIES;

public class DeleteMovieTest {

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

    // удаляет фильм по существующему id
    @Test
    void deleteMovie_success() throws Exception {

        var path = PATH_MOVIES + "/1";

        store.addMovie("title1", 2024);
        store.addMovie("title2", 2024);
        store.addMovie("title3", 2024);
        store.addMovie("title3", 2025);

        var response = MoviesApiTestUtils.deleteRequest(path, client);

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_NO_CONTENT, METHOD_DELETE + path);

        assertEquals(3, store.getMovies().size());
    }

    // возвращает ошибку, если фильм не найден
    @Test
    void deleteMovie_notFound() throws Exception {

        var path = PATH_MOVIES + "/5";

        store.addMovie("title1", 2024);
        store.addMovie("title2", 2024);
        store.addMovie("title3", 2024);
        store.addMovie("title3", 2025);

        var response = MoviesApiTestUtils.deleteRequest(path, client);

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_NOT_FOUND, METHOD_DELETE + path);

        assertEquals(4, store.getMovies().size());
    }

    // возвращает ошибку, если id не число
    @Test
    void deleteMovie_incorrectId() throws Exception {

        var path = PATH_MOVIES + "/asd";

        var response = MoviesApiTestUtils.deleteRequest(path, client);

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_BAD_REQUEST, METHOD_DELETE + path);

        String responseBody = response.body().trim();
        ErrorResponse responseMovie = gson.fromJson(responseBody, ErrorResponse.class);

        assertEquals(Constants.ERR_INCORRECT_ID, responseMovie.getError());

    }
}

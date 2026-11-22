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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.moviehub.Constants.METHOD_GET;
import static ru.practicum.moviehub.Constants.PATH_MOVIES;

public class GetMoviesTest {

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
        store.clear();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    // ==================== GET /movies ====================

    // Возвращает пустой список, если нет фильмов
    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {

        var response = MoviesApiTestUtils.getRequest(PATH_MOVIES, client);

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_OK, METHOD_GET + PATH_MOVIES);

        String body = response.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
            "Ожидается JSON-массив");
    }

    // Возвращает список с ранее добавленными фильмами.
    @Test
    void getMovies_returnsArray() throws Exception {

        var movie1 = store.addMovie("title", 2024);
        var movie2 = store.addMovie("title2", 2025);
        var movie3 = store.addMovie("title3", 2026);
        var ids = List.of(movie1.getId(), movie2.getId(), movie3.getId());

        var response = MoviesApiTestUtils.getRequest(PATH_MOVIES, client);

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_OK, METHOD_GET + PATH_MOVIES);

        String body = response.body().trim();
        List<Movie> moviesList = gson.fromJson(body, new ListOfMoviesTypeToken().getType());

        assertEquals(3, moviesList.size(),
            "Ожидается 3 фильма");
        moviesList.forEach(
            movie -> assertTrue(ids.contains(movie.getId()))
        );
    }


    // ==================== GET /movies/{id} ====================

    // Возвращает фильм по существующему id
    @Test
    void getMovieById_returnsMovie() throws Exception {
        var movie1 = store.addMovie("title", 2024);

        var response = MoviesApiTestUtils.getRequest(PATH_MOVIES + "/0", client);

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_OK, METHOD_GET + PATH_MOVIES);

        String responseBody = response.body().trim();
        Movie responseMovie = gson.fromJson(responseBody, Movie.class);

        assertEquals(responseMovie.getId(), movie1.getId());
        assertEquals(responseMovie.getTitle(), movie1.getTitle());
        assertEquals(responseMovie.getYear(), movie1.getYear());
    }

    // Возвращает ошибку, если фильм не найден
    @Test
    void getMovieById_returnNotFound() throws Exception {

        var path = PATH_MOVIES + "/0";

        var response = MoviesApiTestUtils.getRequest(path, client);

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_NOT_FOUND, METHOD_GET + path);

        String responseBody = response.body().trim();
        ErrorResponse responseMovie = gson.fromJson(responseBody, ErrorResponse.class);

        assertEquals(Constants.ERR_MOVIE_NOT_FOUND, responseMovie.getError());
    }

    // Возвращает ошибку, если id не число
    @Test
    void getMovieById_incorrectId() throws Exception {

        var path = PATH_MOVIES + "/aaa";

        var response = MoviesApiTestUtils.getRequest(path, client);

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_BAD_REQUEST, METHOD_GET + path);

        String responseBody = response.body().trim();
        ErrorResponse responseMovie = gson.fromJson(responseBody, ErrorResponse.class);

        assertEquals(Constants.ERR_INCORRECT_ID, responseMovie.getError());
    }


    // ==================== GET /movies?year=YYYY ====================

    // Возвращает фильмы указанного года
    @Test
    void getMoviesByYear_returnsMovie() throws Exception {

        var path = PATH_MOVIES + "?year=2024";

        store.addMovie("title1", 2024);
        store.addMovie("title2", 2024);
        store.addMovie("title3", 2024);
        store.addMovie("title3", 2025);

        var response = MoviesApiTestUtils.getRequest(path, client);

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_OK, METHOD_GET + path);

        String responseBody = response.body().trim();
        List<Movie> moviesList = gson.fromJson(responseBody, new ListOfMoviesTypeToken().getType());

        assertEquals(3, moviesList.size());
    }

    // Возвращает пустой список, если фильмов с таким годом нет
    @Test
    void getMoviesByYear_returnsEmptyArray() throws Exception {

        var path = PATH_MOVIES + "?year=2025";

        store.addMovie("title1", 2024);

        var response = MoviesApiTestUtils.getRequest(path, client);

        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_OK, METHOD_GET + path);

        String responseBody = response.body().trim();
        List<Movie> moviesList = gson.fromJson(responseBody, new ListOfMoviesTypeToken().getType());

        assertEquals(0, moviesList.size());

        assertTrue(responseBody.startsWith("[") && responseBody.endsWith("]"),
            "Ожидается JSON-массив");
    }

    // Возвращает ошибку, если параметр year не число
    @Test
    void getMoviesByYear_incorrectYear() throws Exception {

        var path = PATH_MOVIES + "?year=aaa";

        var response = MoviesApiTestUtils.getRequest(path, client);
        MoviesApiTestUtils.checkStatusCodeAndHeaders(
            response, Constants.STATUS_BAD_REQUEST, METHOD_GET + path);


        String responseBody = response.body().trim();
        ErrorResponse responseMovie = gson.fromJson(responseBody, ErrorResponse.class);

        assertEquals(Constants.ERR_INCORRECT_YEAR_PARAM, responseMovie.getError());
    }
}

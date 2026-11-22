package ru.practicum.moviehub.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.practicum.moviehub.http.Constants.BASE;
import static ru.practicum.moviehub.http.Constants.EXPECTED_CT_JSON;

public class MoviesApiTestUtils {

    public static HttpResponse<String> getRequest(
        String path, HttpClient client
    ) throws IOException, InterruptedException {

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE + path))
            .GET()
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public static HttpResponse<String> postRequest(
        String path, HttpClient client, HttpRequest.BodyPublisher body, String[] headers
    ) throws IOException, InterruptedException {

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE + path))
            .headers(headers)
            .POST(body)
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public static HttpResponse<String> deleteRequest(
        String path, HttpClient client
    ) throws IOException, InterruptedException {

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE + path))
            .DELETE()
            .build();
        return client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }


    public static void checkStatusCodeAndHeaders(
        HttpResponse<String> response, Integer expectedStatusCode, String url
    ) {

        assertEquals(
            expectedStatusCode, response.statusCode(),
            "%s должен вернуть %d".formatted(url, expectedStatusCode)
        );

        String contentTypeHeaderValue =
            response.headers().firstValue("Content-Type").orElse("");
        assertEquals(
            EXPECTED_CT_JSON, contentTypeHeaderValue,
            "Content-Type должен содержать формат данных и кодировку"
        );
    }
}
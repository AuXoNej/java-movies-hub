package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static ru.practicum.moviehub.Constants.CT_JSON;
import static ru.practicum.moviehub.Constants.STATUS_NO_CONTENT;

public abstract class BaseHttpHandler implements HttpHandler {

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        setJsonContentType(ex);
        ex.sendResponseHeaders(status, 0);

        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    protected void sendNoContent(HttpExchange ex) throws java.io.IOException {

        setJsonContentType(ex);
        ex.sendResponseHeaders(STATUS_NO_CONTENT, -1);
        ex.close();
    }

    protected void setJsonContentType(HttpExchange ex) {
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
    }

}

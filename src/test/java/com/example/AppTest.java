package com.example;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppTest {

    @Test
    void testAdd() {
        assertEquals(5, App.add(2, 3));
    }

    @Test
    void testHttpServer() throws Exception {

        HttpServer server = App.createServer(0);
        server.start();

        int port = server.getAddress().getPort();

        URL url = new URL("http://localhost:" + port + "/");
        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        assertEquals(200, connection.getResponseCode());

        String response = new String(
                connection.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        assertEquals(
                "Hello from GCP DevOps Pipeline - Running in Docker!",
                response
        );

        server.stop(0);
    }
}
package com.example;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class App {

    public static HttpServer createServer(int port) throws IOException {

        HttpServer server =
                HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {

            String response =
                    "Hello Siva - Running in Docker!";

            byte[] responseBytes =
                    response.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(
                    200,
                    responseBytes.length
            );

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        });

        server.setExecutor(null);

        return server;
    }

    public static int add(int a, int b) {
        return a + b;
    }
}
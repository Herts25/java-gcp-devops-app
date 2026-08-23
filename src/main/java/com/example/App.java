package com.example;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class App {

    public static void main(String[] args) throws IOException {
        HttpServer server = createServer(8080);
        server.start();
        System.out.println("Java application started on port 8080");
    }

    public static HttpServer createServer(int port) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", exchange -> {

            String response =
                    "Hello from GCP DevOps Pipeline - Running in Docker!";

            exchange.sendResponseHeaders(
                    200,
                    response.getBytes().length
            );

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        server.setExecutor(null);

        return server;
    }

    public static int add(int a, int b) {
        return a + b;
    }
}
package com.example;

import com.sun.net.httpserver.HttpServer;

public class Main {

    public static void main(String[] args) throws Exception {

        HttpServer server = App.createServer(8080);

        server.start();

        System.out.println(
                "Java application started on port 8080"
        );
    }
}

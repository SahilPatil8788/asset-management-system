package com.assetmanager.api;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Configuration class that instantiates, registers contexts on, and starts the local HttpServer.
 */
public class HttpServerConfig {
    private final int port;
    private HttpServer server;

    public HttpServerConfig(int port) {
        this.port = port;
    }

    /**
     * Initializes and starts the web server.
     */
    public void start() throws IOException {
        // Bind to localhost on specified port with default backlog
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Map root requests to the Static File Handler
        server.createContext("/", new StaticHandler());

        // Map API requests to the REST API Handler
        server.createContext("/api", new ApiHandler());

        // Use default executor (system thread routing)
        server.setExecutor(null);
        server.start();
        System.out.println("[SUCCESS] Local Web Server started at http://localhost:" + port);
    }

    /**
     * Shuts down the web server.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Web Server stopped.");
        }
    }
}

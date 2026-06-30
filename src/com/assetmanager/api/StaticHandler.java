package com.assetmanager.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * HttpHandler that serves the static SPA HTML frontend file.
 */
public class StaticHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        // Default root to index.html
        if (path.equals("/")) {
            path = "/index.html";
        }
        
        // Resolve against web/ directory
        String filePath = "web" + path;
        
        // Prevent directory traversal attacks
        if (path.contains("..")) {
            String response = "403 Forbidden";
            exchange.sendResponseHeaders(403, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        File file = new File(filePath);

        if (!file.exists() || file.isDirectory()) {
            String response = "Error: " + path + " not found on server.";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        // Set content type based on file extension
        String contentType = "text/plain";
        if (path.endsWith(".html")) {
            contentType = "text/html; charset=utf-8";
        } else if (path.endsWith(".css")) {
            contentType = "text/css; charset=utf-8";
        } else if (path.endsWith(".js")) {
            contentType = "application/javascript; charset=utf-8";
        } else if (path.endsWith(".json")) {
            contentType = "application/json; charset=utf-8";
        } else if (path.endsWith(".png")) {
            contentType = "image/png";
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (path.endsWith(".svg")) {
            contentType = "image/svg+xml";
        }

        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(200, file.length());

        // Stream file contents
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = exchange.getResponseBody()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }
}

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class UserBackend {

    // Data user (username -> password)
    private static Map<String, String> users = new HashMap<>();

    public static void main(String[] args) throws IOException {

        // Buat server di port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        // Endpoint CRUD + Validasi
        server.createContext("/register", new RegisterHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/delete", new DeleteHandler());
        server.createContext("/users", new UsersHandler());

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("Server berjalan di http://localhost:8000");
    }

    // ===== Register user =====
    static class RegisterHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, "Gunakan POST method!");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);

            String username = params.get("username");
            String password = params.get("password");

            // Validasi input
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                sendResponse(exchange, "Username dan password wajib diisi!");
                return;
            }

            if (users.containsKey(username)) {
                sendResponse(exchange, "Username sudah digunakan!");
                return;
            }

            users.put(username, password);
            sendResponse(exchange, "User berhasil didaftarkan!");
        }
    }

    // ===== Login user =====
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, "Gunakan POST method!");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);

            String username = params.get("username");
            String password = params.get("password");

            if (username == null || password == null) {
                sendResponse(exchange, "Username dan password wajib diisi!");
                return;
            }

            if (users.containsKey(username) && users.get(username).equals(password)) {
                sendResponse(exchange, "Login berhasil!");
            } else {
                sendResponse(exchange, "Username atau password salah!");
            }
        }
    }

    // ===== Delete user =====
    static class DeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("DELETE")) {
                sendResponse(exchange, "Gunakan DELETE method!");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            Map<String, String> params = queryToMap(query);
            String username = params.get("username");

            if (username == null || username.isEmpty()) {
                sendResponse(exchange, "Username wajib diisi!");
                return;
            }

            if (users.containsKey(username)) {
                users.remove(username);
                sendResponse(exchange, "User berhasil dihapus!");
            } else {
                sendResponse(exchange, "Username tidak ditemukan!");
            }
        }
    }

    // ===== List semua user =====
    static class UsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder();
            if (users.isEmpty()) {
                sb.append("Belum ada user.");
            } else {
                sb.append("Daftar user:\n");
                for (String username : users.keySet()) {
                    sb.append("- ").append(username).append("\n");
                }
            }
            sendResponse(exchange, sb.toString());
        }
    }

    // ===== Helper =====
    static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    static Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) result.put(pair[0], pair[1]);
            else result.put(pair[0], "");
        }
        return result;
    }
}

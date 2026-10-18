package com.nutriguide.server;

import com.nutriguide.service.AuthenticationService;
import com.nutriguide.service.FoodService;
import com.nutriguide.service.MealService;
import com.nutriguide.service.NutritionService;
import com.nutriguide.service.RecommendationService;
import com.nutriguide.service.ReportService;
import com.nutriguide.service.UserService;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * NutriGuideServer: Standalone localhost HTTP backend process.
 *
 * Requirements satisfied:
 * - Binds ONLY to 127.0.0.1 (never listens on external or public network interfaces).
 * - Implemented using Java SE's built-in HttpServer (zero heavy framework like Spring Boot).
 * - Owns database operations, authentication, validation, transactions, and business logic.
 * - Thread-pooled execution with clean shutdown hooks.
 */
public class NutriGuideServer {

    public static final String DEFAULT_HOST = "127.0.0.1";
    // 8080 is commonly occupied by local development tools such as Jenkins.
    public static final int DEFAULT_PORT = 8081;

    private final String host;
    private final int port;
    private HttpServer server;

    public NutriGuideServer() {
        this(DEFAULT_HOST, Integer.getInteger("server.port", DEFAULT_PORT));
    }

    public NutriGuideServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void start() throws IOException {
        if (server != null) {
            return;
        }

        // Initialize Services and in-memory Session Manager
        AuthenticationService authService = new AuthenticationService();
        UserService userService = new UserService();
        NutritionService nutritionService = new NutritionService();
        FoodService foodService = new FoodService();
        MealService mealService = new MealService();
        RecommendationService recommendationService = new RecommendationService();
        ReportService reportService = new ReportService();
        SessionManager sessionManager = new SessionManager();

        // Create server bound strictly to localhost IP
        InetSocketAddress address = new InetSocketAddress(host, port);
        server = HttpServer.create(address, 0);

        // Register REST API Route Handlers
        server.createContext("/api/health", new HealthHandler());
        server.createContext("/api/auth", new AuthHandler(authService, sessionManager));
        server.createContext("/api/profile", new ProfileHandler(userService, nutritionService, sessionManager));
        server.createContext("/api/foods", new FoodHandler(foodService, sessionManager));
        server.createContext("/api/meals", new MealHandler(mealService, sessionManager));
        server.createContext("/api/goals", new GoalHandler(nutritionService, sessionManager));
        server.createContext("/api/recommendations", new RecommendationHandler(recommendationService, sessionManager));
        server.createContext("/api/reports", new ReportHandler(reportService, sessionManager));

        // Use thread pool for concurrent request execution
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("================================================================================");
        System.out.println("      NutriGuide Localhost Backend Service Started Successfully!               ");
        System.out.println("================================================================================");
        System.out.println(" Host Binding:     " + host + " (Strictly localhost loopback only)");
        System.out.println(" Listening Port:   " + port);
        System.out.println(" Base API URL:     http://" + host + ":" + port + "/api");
        System.out.println(" Health Check:     http://" + host + ":" + port + "/api/health");
        System.out.println(" Technology:       Java 8 HttpServer + MySQL JDBC + PBKDF2 Hashing");
        System.out.println(" Press Ctrl+C in this terminal to shut down the server gracefully.");
        System.out.println("================================================================================");

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                NutriGuideServer.this.stop();
            }
        }));
    }

    public synchronized void stop() {
        if (server != null) {
            System.out.println("Stopping NutriGuide Localhost Server...");
            server.stop(0);
            server = null;
            System.out.println("NutriGuide Localhost Server stopped.");
        }
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }

        try {
            NutriGuideServer server = new NutriGuideServer(DEFAULT_HOST, port);
            server.start();
        } catch (Exception e) {
            System.err.println("CRITICAL: Failed to start NutriGuide Localhost Server on port " + port);
            e.printStackTrace();
            System.exit(1);
        }
    }
}

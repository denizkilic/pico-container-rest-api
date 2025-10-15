package org.user.microservice;

import org.user.microservice.config.ApplicationConfig;
import org.user.microservice.controller.UserController;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.picocontainer.MutablePicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    private static final int DEFAULT_PORT = 8080;

    private final MutablePicoContainer container;
    private final Server server;

    public Application() {
        this(DEFAULT_PORT);
    }

    public Application(int port) {
        logger.info("Starting PicoContainer Microservice on port {}", port);

        // Initialize PicoContainer
        this.container = ApplicationConfig.createContainer();

        // Create Jetty server
        this.server = new Server(port);
        setupServlets();
    }

    private void setupServlets() {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // Get UserController from PicoContainer
        UserController userController = container.getComponent(UserController.class);

        // Register user controller servlet
        ServletHolder userServletHolder = new ServletHolder(userController);
        context.addServlet(userServletHolder, "/api/users/*");

        // Add health check endpoint
        context.addServlet(new ServletHolder(new HealthController()), "/health");

        server.setHandler(context);

        logger.info("Servlets configured successfully");
    }

    public void start() throws Exception {
        server.start();
        logger.info("Server started successfully on port {}", server.getURI().getPort());
        logger.info("API endpoints:");
        logger.info("  GET    /api/users      - Get all users");
        logger.info("  GET    /api/users/{id} - Get user by ID");
        logger.info("  POST   /api/users      - Create new user");
        logger.info("  PUT    /api/users/{id} - Update user");
        logger.info("  DELETE /api/users/{id} - Delete user");
        logger.info("  GET    /health         - Health check");
    }

    public void stop() throws Exception {
        logger.info("Stopping server...");
        server.stop();
        ApplicationConfig.shutdownContainer(container);
        logger.info("Server stopped successfully");
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        // Parse port from command line arguments
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.warn("Invalid port number '{}', using default port {}", args[0], DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        Application app = new Application(port);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                app.stop();
            } catch (Exception e) {
                logger.error("Error during shutdown", e);
            }
        }));

        try {
            app.start();
            app.join();
        } catch (Exception e) {
            logger.error("Failed to start application", e);
            System.exit(1);
        }
    }
}
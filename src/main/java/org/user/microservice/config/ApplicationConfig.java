package org.user.microservice.config;

import org.user.microservice.controller.UserController;
import org.user.microservice.repository.UserRepositoryImpl;
import org.user.microservice.repository.UserRepository;
import org.user.microservice.service.UserService;
import org.user.microservice.service.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ApplicationConfig {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationConfig.class);

    private static final String DB_URL = "jdbc:h2:mem:userdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "";

    public static MutablePicoContainer createContainer() {
        logger.info("Initializing PicoContainer...");

        MutablePicoContainer container = new DefaultPicoContainer();

        try {
            // Register database connection
            Connection connection = createDatabaseConnection();
            container.addComponent(Connection.class, connection);
            logger.info("Database connection registered");

            // Register ObjectMapper with JSR310 module for LocalDateTime support
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            container.addComponent(ObjectMapper.class, objectMapper);
            logger.info("ObjectMapper registered");

            // Register repository
            container.addComponent(UserRepository.class, UserRepositoryImpl.class);
            logger.info("UserRepository registered");

            // Register service
            container.addComponent(UserService.class, UserServiceImpl.class);
            logger.info("UserService registered");

            // Register controller
            container.addComponent(UserController.class, UserController.class);
            logger.info("UserController registered");

            logger.info("PicoContainer initialized successfully");

        } catch (Exception e) {
            logger.error("Failed to initialize PicoContainer", e);
            throw new RuntimeException("Failed to initialize application", e);
        }

        return container;
    }

    private static Connection createDatabaseConnection() throws SQLException {
        logger.info("Creating H2 database connection...");

        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 driver not found", e);
        }

        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        logger.info("H2 database connection created successfully");

        return connection;
    }

    public static void shutdownContainer(MutablePicoContainer container) {
        logger.info("Shutting down application...");

        try {
            // Close database connection
            Connection connection = container.getComponent(Connection.class);
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            logger.error("Error closing database connection", e);
        }

        container.dispose();
        logger.info("PicoContainer disposed");
    }
}
package org.user.microservice.controller;

import org.user.microservice.model.User;
import org.user.microservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class UserController extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserController(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                getAllUsers(response);
            } else {
                String[] pathParts = pathInfo.split("/");
                if (pathParts.length == 2) {
                    Long userId = Long.parseLong(pathParts[1]);
                    getUserById(userId, response);
                } else {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                }
            }
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (Exception e) {
            logger.error("Error handling GET request", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            User user = objectMapper.readValue(request.getReader(), User.class);
            User createdUser = userService.createUser(user);

            response.setStatus(HttpServletResponse.SC_CREATED);
            sendJsonResponse(response, createdUser);

        } catch (IllegalArgumentException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating user", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "User ID is required");
                return;
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length != 2) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                return;
            }

            Long userId = Long.parseLong(pathParts[1]);
            User user = objectMapper.readValue(request.getReader(), User.class);

            User updatedUser = userService.updateUser(userId, user);
            sendJsonResponse(response, updatedUser);

        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating user", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "User ID is required");
                return;
            }

            String[] pathParts = pathInfo.split("/");
            if (pathParts.length != 2) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
                return;
            }

            Long userId = Long.parseLong(pathParts[1]);
            userService.deleteUser(userId);

            response.setStatus(HttpServletResponse.SC_NO_CONTENT);

        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID format");
        } catch (IllegalArgumentException e) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting user", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    private void getAllUsers(HttpServletResponse response) throws IOException {
        List<User> users = userService.getAllUsers();
        sendJsonResponse(response, users);
    }

    private void getUserById(Long userId, HttpServletResponse response) throws IOException {
        Optional<User> user = userService.getUserById(userId);

        if (user.isPresent()) {
            sendJsonResponse(response, user.get());
        } else {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
        }
    }

    private void sendJsonResponse(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), data);
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(message, statusCode);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private static class ErrorResponse {
        private final String message;
        private final int status;
        private final long timestamp;

        public ErrorResponse(String message, int status) {
            this.message = message;
            this.status = status;
            this.timestamp = System.currentTimeMillis();
        }

        public String getMessage() { return message; }
        public int getStatus() { return status; }
        public long getTimestamp() { return timestamp; }
    }
}

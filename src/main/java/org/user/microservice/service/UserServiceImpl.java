package org.user.microservice.service;

import org.user.microservice.model.User;
import org.user.microservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User createUser(User user) {
        logger.info("Creating new user with email: {}", user.getEmail());

        validateUser(user);

        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }

        User savedUser = userRepository.save(user);
        logger.info("User created successfully with ID: {}", savedUser.getId());

        return savedUser;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        logger.debug("Fetching user with ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }

        return userRepository.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, User user) {
        logger.info("Updating user with ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }

        validateUser(user);

        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }

        Optional<User> userWithSameEmail = userRepository.findByEmail(user.getEmail());
        if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(id)) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }

        User userToUpdate = existingUser.get();
        userToUpdate.setFirstName(user.getFirstName());
        userToUpdate.setLastName(user.getLastName());
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setAge(user.getAge());

        User updatedUser = userRepository.save(userToUpdate);
        logger.info("User updated successfully with ID: {}", updatedUser.getId());

        return updatedUser;
    }

    @Override
    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number");
        }

        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found with ID: " + id);
        }

        userRepository.deleteById(id);
        logger.info("User deleted successfully with ID: {}", id);
    }

    @Override
    public boolean userExists(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        return userRepository.existsById(id);
    }

    @Override
    public long getUserCount() {
        return userRepository.count();
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }

        if (user.getAge() < 0 || user.getAge() > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
    }

    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }
}

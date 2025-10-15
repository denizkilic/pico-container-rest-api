package org.user.microservice.repository;

import org.user.microservice.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    Optional<User> findByEmail(String email);
    void deleteById(Long id);
    boolean existsById(Long id);
    long count();
}
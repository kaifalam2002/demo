package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmailAndPasswordAndActive(String email, String password, boolean isActive);
}

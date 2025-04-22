package com.cheeza.Cheeza.repository;

import com.cheeza.Cheeza.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

//    default void updateUser(User user) {
//        save(user);
//    }
}

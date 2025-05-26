
package com.cheeza.Cheeza.repository;

import com.cheeza.Cheeza.model.Notification;
import com.cheeza.Cheeza.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n WHERE n.user = ?1 AND n.read = false")
    List<Notification> findByUserAndReadFalse(User user);
}

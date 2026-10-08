package com.codementor.api.repository;

import com.codementor.api.entity.MockInterview;
import com.codementor.api.entity.MockInterviewStatus;
import com.codementor.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockInterviewRepository extends JpaRepository<MockInterview, Long> {
    List<MockInterview> findByUserOrderByStartedAtDesc(User user);
    Optional<MockInterview> findByIdAndUser(Long id, User user);
    List<MockInterview> findByUserAndStatusOrderByCompletedAtDesc(User user, MockInterviewStatus status);
}

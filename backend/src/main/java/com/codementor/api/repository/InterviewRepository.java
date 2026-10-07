package com.codementor.api.repository;

import com.codementor.api.entity.Interview;
import com.codementor.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByUserOrderByStartedAtDesc(User user);
    List<Interview> findByUser_IdOrderByStartedAtDesc(Long userId);
    Optional<Interview> findByIdAndUser(Long id, User user);
    List<Interview> findAllByOrderByStartedAtDesc();
}

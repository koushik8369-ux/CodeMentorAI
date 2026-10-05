package com.codementor.api.repository;

import com.codementor.api.entity.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    Optional<Problem> findBySlug(String slug);
    List<Problem> findByDifficulty(String difficulty);
    List<Problem> findByTopic(String topic);
}

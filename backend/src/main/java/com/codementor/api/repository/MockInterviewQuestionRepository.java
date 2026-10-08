package com.codementor.api.repository;

import com.codementor.api.entity.MockInterview;
import com.codementor.api.entity.MockInterviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockInterviewQuestionRepository extends JpaRepository<MockInterviewQuestion, Long> {
    List<MockInterviewQuestion> findByMockInterviewOrderByRoundNumberAsc(MockInterview mockInterview);
    Optional<MockInterviewQuestion> findByMockInterviewAndRoundNumber(MockInterview mockInterview, int roundNumber);
}

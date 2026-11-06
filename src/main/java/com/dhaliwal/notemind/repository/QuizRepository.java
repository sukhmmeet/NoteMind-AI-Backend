package com.dhaliwal.notemind.repository;

import com.dhaliwal.notemind.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    @Query("SELECT COUNT(q) FROM Quiz q WHERE q.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}

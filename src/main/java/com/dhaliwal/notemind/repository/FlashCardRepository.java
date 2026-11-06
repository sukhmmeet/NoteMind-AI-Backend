package com.dhaliwal.notemind.repository;

import com.dhaliwal.notemind.entity.FlashCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlashCardRepository extends JpaRepository<FlashCard, Long> {

    @Query("SELECT COUNT(f) FROM FlashCard f WHERE f.note.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);
}
package com.dhaliwal.notemind.repository;

import com.dhaliwal.notemind.entity.Note;
import com.dhaliwal.notemind.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    long countByUserId(Long userId);

    @EntityGraph(attributePaths = {"tags", "folder"})
    @Query("SELECT n FROM Note n JOIN FETCH n.user u WHERE u.id = :id")
    List<Note> findAllByUserId(@Param("id") Long id);

        @EntityGraph(attributePaths = {"tags", "folder"})
        @Query(value = "SELECT n FROM Note n JOIN FETCH n.user u WHERE u.id = :id",
            countQuery = "SELECT COUNT(n) FROM Note n WHERE n.user.id = :id")
        Page<Note> findAllByUserId(@Param("id") Long id, Pageable pageable);

    @Query(value = """
            SELECT *
            FROM note
            WHERE user_id = :userId
            AND search_vector @@ plainto_tsquery('english', :query)
            ORDER BY ts_rank(search_vector,
                             plainto_tsquery('english', :query)) DESC
            """,
            nativeQuery = true)
    List<Note> searchNotes(Long userId, String query);

    @Query("""
    SELECT n
    FROM Note n
    WHERE n.user.id = :userId
      AND n.folder IS NULL
""")
    List<Note> findNotesWithoutFolderByUserId(@Param("userId") Long userId);
}
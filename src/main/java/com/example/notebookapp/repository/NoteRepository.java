package com.example.notebookapp.repository;

import com.example.notebookapp.model.Note;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findAllByUserId(Long userId);

    Optional<Note> findByIdAndUserId(Long id, Long userId);

    // REQUIRED raw SQL with prepared statement
    @Query(
            value = "SELECT * FROM notes WHERE user_id = :userId AND title ILIKE %:keyword%",
            nativeQuery = true
    )
    List<Note> searchByTitle(
            @Param("userId") Long userId,
            @Param("keyword") String keyword
    );
}

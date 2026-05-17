package com.notesapp.repository;

import com.notesapp.entity.Note;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<Note, Long> {

    @Query(
            """
            SELECT DISTINCT n FROM Note n
            LEFT JOIN NoteShare ns ON ns.note = n
            WHERE n.deletedAt IS NULL
              AND (n.owner.id = :userId OR ns.sharedWithUser.id = :userId)
              AND (:search IS NULL OR :search = '' OR
                   LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(n.content) LIKE LOWER(CONCAT('%', :search, '%')))
              AND (:archived IS NULL OR n.archived = :archived)
            """)
    Page<Note> findAccessibleNotes(
            @Param("userId") Long userId,
            @Param("search") String search,
            @Param("archived") Boolean archived,
            Pageable pageable);

    @Query(
            """
            SELECT n FROM Note n
            WHERE n.id = :noteId AND n.deletedAt IS NULL
              AND (n.owner.id = :userId OR EXISTS (
                  SELECT 1 FROM NoteShare ns
                  WHERE ns.note = n AND ns.sharedWithUser.id = :userId))
            """)
    Optional<Note> findAccessibleById(@Param("noteId") Long noteId, @Param("userId") Long userId);

    @Query(
            """
            SELECT n FROM Note n
            WHERE n.id = :noteId AND n.deletedAt IS NULL AND n.owner.id = :ownerId
            """)
    Optional<Note> findOwnedById(@Param("noteId") Long noteId, @Param("ownerId") Long ownerId);
}

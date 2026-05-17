package com.notesapp.repository;

import com.notesapp.entity.NoteShare;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteShareRepository extends JpaRepository<NoteShare, Long> {

    boolean existsByNoteIdAndSharedWithUserId(Long noteId, Long sharedWithUserId);
}

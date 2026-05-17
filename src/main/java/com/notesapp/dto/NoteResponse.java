package com.notesapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.notesapp.entity.Note;
import java.time.Instant;

public record NoteResponse(
        Long id,
        String title,
        String content,
        @JsonProperty("owner_id") Long ownerId,
        boolean archived,
        @JsonProperty("is_owner") boolean isOwner,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt) {

    public static NoteResponse from(Note note, Long currentUserId) {
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getOwner().getId(),
                note.isArchived(),
                note.getOwner().getId().equals(currentUserId),
                note.getCreatedAt(),
                note.getUpdatedAt());
    }
}

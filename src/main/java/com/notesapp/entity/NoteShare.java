package com.notesapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(
        name = "note_shares",
        uniqueConstraints = @UniqueConstraint(columnNames = {"note_id", "shared_with_user_id"}))
public class NoteShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_with_user_id", nullable = false)
    private User sharedWithUser;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected NoteShare() {}

    public NoteShare(Note note, User sharedWithUser) {
        this.note = note;
        this.sharedWithUser = sharedWithUser;
    }

    public Long getId() {
        return id;
    }

    public Note getNote() {
        return note;
    }

    public User getSharedWithUser() {
        return sharedWithUser;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

package com.notesapp.service;

import com.notesapp.dto.NoteRequest;
import com.notesapp.dto.NoteResponse;
import com.notesapp.dto.PagedNotesResponse;
import com.notesapp.dto.ShareNoteRequest;
import com.notesapp.entity.Note;
import com.notesapp.entity.NoteShare;
import com.notesapp.entity.User;
import com.notesapp.exception.ApiException;
import com.notesapp.repository.NoteRepository;
import com.notesapp.repository.NoteShareRepository;
import com.notesapp.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteService {

    private static final int MAX_PAGE_SIZE = 50;

    private final NoteRepository noteRepository;
    private final NoteShareRepository noteShareRepository;
    private final UserRepository userRepository;

    public NoteService(
            NoteRepository noteRepository,
            NoteShareRepository noteShareRepository,
            UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.noteShareRepository = noteShareRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotes(Long userId) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "updatedAt"));
        return noteRepository
                .findAccessibleNotes(userId, null, null, pageable)
                .map(note -> NoteResponse.from(note, userId))
                .getContent();
    }

    @Transactional(readOnly = true)
    public PagedNotesResponse getNotes(
            Long userId, String search, Boolean archived, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int safePage = Math.max(page, 0);
        Pageable pageable =
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Note> result = noteRepository.findAccessibleNotes(userId, search, archived, pageable);
        List<NoteResponse> notes =
                result.getContent().stream().map(n -> NoteResponse.from(n, userId)).toList();
        return new PagedNotesResponse(
                notes, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public NoteResponse getNote(Long userId, Long noteId) {
        Note note = noteRepository
                .findAccessibleById(noteId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Note not found"));
        return NoteResponse.from(note, userId);
    }

    @Transactional
    public NoteResponse createNote(Long userId, NoteRequest request) {
        User owner = userRepository
                .findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        Note note = noteRepository.save(new Note(request.title().trim(), request.content().trim(), owner));
        return NoteResponse.from(note, userId);
    }

    @Transactional
    public NoteResponse updateNote(Long userId, Long noteId, NoteRequest request) {
        Note note = noteRepository
                .findOwnedById(noteId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Note not found"));
        note.setTitle(request.title().trim());
        note.setContent(request.content().trim());
        note.touch();
        return NoteResponse.from(note, userId);
    }

    @Transactional
    public void deleteNote(Long userId, Long noteId) {
        Note note = noteRepository
                .findOwnedById(noteId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Note not found"));
        note.setDeletedAt(Instant.now());
        note.touch();
    }

    @Transactional
    public void shareNote(Long userId, Long noteId, ShareNoteRequest request) {
        Note note = noteRepository
                .findOwnedById(noteId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Note not found"));

        String targetEmail = request.shareWithEmail().trim().toLowerCase();
        if (targetEmail.equals(note.getOwner().getEmail())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot share a note with yourself");
        }

        User targetUser = userRepository
                .findByEmailIgnoreCase(targetEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User to share with not found"));

        if (noteShareRepository.existsByNoteIdAndSharedWithUserId(noteId, targetUser.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "Note is already shared with this user");
        }

        noteShareRepository.save(new NoteShare(note, targetUser));
    }

    @Transactional
    public NoteResponse archiveNote(Long userId, Long noteId, boolean archived) {
        Note note = noteRepository
                .findOwnedById(noteId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Note not found"));
        note.setArchived(archived);
        note.touch();
        return NoteResponse.from(note, userId);
    }
}

package com.notesapp.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PagedNotesResponse(
        List<NoteResponse> notes,
        @JsonProperty("page") int pageNumber,
        @JsonProperty("size") int pageSize,
        @JsonProperty("total_elements") long totalElements,
        @JsonProperty("total_pages") int totalPages) {}

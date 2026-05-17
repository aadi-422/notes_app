package com.notesapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShareNoteRequest(@NotBlank @Email @Size(max = 320) String shareWithEmail) {}

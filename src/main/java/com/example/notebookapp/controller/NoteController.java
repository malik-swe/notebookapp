package com.example.notebookapp.controller;

import com.example.notebookapp.dto.CreateNoteRequest;
import com.example.notebookapp.model.Note;
import com.example.notebookapp.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    public ResponseEntity<Note> create(@Valid @RequestBody CreateNoteRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(noteService.create(request.getTitle(), request.getContent()));
    }

    @GetMapping
    public List<Note> getAll() {
        return noteService.getAll();
    }

    @GetMapping("/{id}")
    public Note get(@PathVariable("id") Long id) {
        return noteService.getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.example.notebookapp.service;

import com.example.notebookapp.exception.ForbiddenException;
import com.example.notebookapp.exception.ResourceNotFoundException;
import com.example.notebookapp.model.Note;
import com.example.notebookapp.model.User;
import com.example.notebookapp.repository.NoteRepository;
import com.example.notebookapp.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    public Note create(String title, String content) {
        User user = getCurrentUser();
        return noteRepository.save(new Note(title, content, user.getId()));
    }

    public List<Note> getAll() {
        User user = getCurrentUser();
        return noteRepository.findAllByUserId(user.getId());
    }

    public Note getById(Long id) {
        User user = getCurrentUser();

        // check if the note exists at all
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note", id));

        // check if the current user owns it
        if (!note.getUserId().equals(user.getId())) {
            throw new ForbiddenException("Note", id);
        }

        return note;
    }

    public void delete(Long id) {
        Note note = getById(id);
        noteRepository.delete(note);
    }
}
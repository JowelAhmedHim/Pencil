package com.example.pencil.listeners;

import com.example.pencil.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}

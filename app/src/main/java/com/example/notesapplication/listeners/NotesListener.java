package com.example.notesapplication.listeners;

import com.example.notesapplication.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
